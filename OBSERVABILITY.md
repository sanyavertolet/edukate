# Observability Guide

Full observability for Edukate consists of three pillars: **metrics** (Prometheus + Grafana),
**distributed tracing** (OpenTelemetry + Tempo), and **structured logs** (Loki). This document
covers how to wire up each one and what to monitor.

---

## Current state

All four services (`gateway`, `backend`, `notifier`, `checker`) already have:
- `spring-boot-starter-actuator` on the classpath
- A dedicated management port (`x8x1`)
- Health and info endpoints exposed

What is **not yet done**: Prometheus metrics endpoint, tracing export, structured JSON logs.

---

## 1. Suppress actuator probe noise (quick win, do first)

Kubernetes probes hit `/actuator/health` every few seconds. With `logging.level.org.springframework=DEBUG`
this generates a wall of log lines per probe.

**Fix 1 — move DEBUG logging to dev profile only.**

In each `application.properties`, remove:
```properties
logging.level.org.springframework=DEBUG
logging.level.io.github.sanyavertolet=DEBUG
```
Add them only to `application-dev.properties` (or `application-dev.yml`). Prod stays at INFO.

**Fix 2 — silence Reactor Netty access log for actuator paths.**

Reactor Netty logs every HTTP request. Add to `application.properties`:
```properties
# Disable Reactor Netty's built-in access log (we'll add a selective one later)
reactor.netty.http.server.access-logs=false
logging.level.reactor.netty.http.server=WARN
```

If you want access logs for application routes but not for `/actuator/**`, add a WebFilter:
```kotlin
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
fun accessLogFilter(): WebFilter = WebFilter { exchange, chain ->
    val path = exchange.request.path.value()
    if (!path.startsWith("/actuator")) {
        log.info("{} {}", exchange.request.method, path)
    }
    chain.filter(exchange)
}
```

---

## 2. Prometheus metrics

### 2.1 Add dependencies

In `gradle/libs.versions.toml`, add:
```toml
micrometer-registry-prometheus = { module = "io.micrometer:micrometer-registry-prometheus" }
```

Add to every service's `build.gradle.kts`:
```kotlin
implementation(libs.micrometer.registry.prometheus)
```

`spring-boot-starter-actuator` already pulls in `micrometer-core`; the Prometheus registry just adds
the `/actuator/prometheus` scrape endpoint.

### 2.2 Expose the endpoint

Add to each `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.access=unrestricted
```

> The management port (`x8x1`) is not behind the gateway, so exposing prometheus there is safe.
> Never expose `/actuator/prometheus` on the main application port.

### 2.3 Prometheus scrape config (Kubernetes)

Add annotations to each pod/deployment:
```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "5801"   # management port per service
  prometheus.io/path: "/actuator/prometheus"
```

Or use a `ServiceMonitor` if you're running the Prometheus Operator:
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: edukate-backend
spec:
  selector:
    matchLabels:
      app: edukate-backend
  endpoints:
    - port: management
      path: /actuator/prometheus
      interval: 15s
```

### 2.4 Key metrics to watch

| Metric                                                | What it tells you                        |
|-------------------------------------------------------|------------------------------------------|
| `http_server_requests_seconds` (histogram)            | Latency per endpoint; spot slow handlers |
| `http_server_requests_seconds_count`                  | Request rate per endpoint                |
| `http_server_requests_seconds_max`                    | Worst-case latency (p100)                |
| `reactor_netty_connections_active`                    | Active WebFlux connections               |
| `mongodb_driver_commands_seconds`                     | MongoDB query latency                    |
| `rabbitmq_published_total`, `rabbitmq_consumed_total` | Message throughput                       |
| `jvm_memory_used_bytes` / `jvm_gc_pause_seconds`      | GC pressure                              |
| `process_cpu_usage`                                   | CPU hotspots per service                 |
| `executor_pool_size` / `executor_active_threads`      | Thread pool saturation                   |

For `edukate-checker` specifically:
```properties
# Track how long OpenAI calls take — add a custom Timer
management.metrics.tags.service=edukate-checker
```
```kotlin
@Timed("openai.call", description = "OpenAI API call duration")
fun callModel(ctx: RequestContext): ModelResponse { ... }
```

---

## 3. Distributed tracing (OpenTelemetry + Tempo)

### 3.1 Add dependencies

```toml
# libs.versions.toml
micrometer-tracing-bridge-otel = { module = "io.micrometer:micrometer-tracing-bridge-otel" }
opentelemetry-exporter-otlp = { module = "io.opentelemetry:opentelemetry-exporter-otlp" }
```

```kotlin
// every service's build.gradle.kts
implementation(libs.micrometer.tracing.bridge.otel)
implementation(libs.opentelemetry.exporter.otlp)
```

Spring Boot 4 autoconfigures tracing when these are on the classpath.

### 3.2 Configure the OTLP exporter

```properties
# application.properties (all services)
management.tracing.sampling.probability=1.0   # 100% in dev; use 0.1 in prod
management.otlp.tracing.endpoint=http://otel-collector:4318/v1/traces
spring.application.name=edukate-backend       # already set — becomes the service name in traces
```

### 3.3 What gets traced automatically

Spring Boot 4 + Micrometer auto-instruments:
- All WebFlux HTTP requests (inbound + outbound WebClient calls)
- MongoDB reactive driver operations
- RabbitMQ message publishing and consumption
- Spring AI `ChatClient` calls (if using the `ObservationRegistry`)

The trace flows gateway → backend → checker across service boundaries via propagated
`traceparent` headers (W3C Trace Context).

### 3.4 Trace a full check request

A single submission check spans:
1. `POST /api/v1/submissions` (gateway → backend)
2. RabbitMQ publish to `edukate.check.schedule.v1`
3. Checker consumes, calls S3, calls OpenAI
4. Checker publishes to `backend.check.result.v1.q`
5. Backend consumes, updates submission status

With tracing enabled you'll see the entire chain as a single trace in Tempo/Jaeger, with each step
as a span. The OpenAI call span will show exactly how long the AI takes.

### 3.5 Correlate traces with logs

Add the Logback/SLF4J MDC bridge so every log line carries `traceId` and `spanId`:

```kotlin
implementation("io.micrometer:micrometer-tracing")  // already transitive
```

Update log pattern (or use structured JSON — see section 4):
```properties
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId} spanId=%X{spanId}] - %msg%n
```

---

## 4. Structured logs → Loki

### 4.1 Add JSON encoder

```toml
logstash-logback-encoder = { module = "net.logstash.logback:logstash-logback-encoder", version = "8.0" }
```

```kotlin
implementation(libs.logstash.logback.encoder)
```

Create `src/main/resources/logback-spring.xml` (the `-spring` suffix lets Spring process `<springProfile>`):
```xml
<configuration>
    <springProfile name="!dev">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON" />
        </root>
    </springProfile>

    <springProfile name="dev">
        <!-- Human-readable in dev -->
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} %highlight(%-5level) %cyan(%logger{36}) [%X{traceId}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE" />
        </root>
    </springProfile>
</configuration>
```

Loki then scrapes pod stdout, parses the JSON, and Grafana's LogQL lets you do:
```logql
{app="edukate-backend"} | json | traceId="abc123"
```
— jump from a Tempo trace span directly to all log lines for that request.

### 4.2 Promtail / Grafana Alloy scrape config

```yaml
# promtail config or Alloy `loki.source.kubernetes` block
- job_name: edukate
  kubernetes_sd_configs:
    - role: pod
  relabel_configs:
    - source_labels: [__meta_kubernetes_pod_label_app]
      target_label: app
  pipeline_stages:
    - json:
        expressions:
          level: level
          traceId: traceId
    - labels:
        level:
        traceId:
```

---

## 5. Grafana dashboard setup

### Recommended data sources

| Data source | Purpose                                 |
|-------------|-----------------------------------------|
| Prometheus  | Metrics (latency, rates, JVM, RabbitMQ) |
| Tempo       | Distributed traces                      |
| Loki        | Log aggregation                         |

### Useful panels

**Service health overview**
- Request rate per service: `rate(http_server_requests_seconds_count{job="edukate-backend"}[1m])`
- Error rate: `rate(http_server_requests_seconds_count{status=~"5.."}[1m])`
- p99 latency: `histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))`

**Finding bottlenecks**
- Slowest endpoints: sort by `http_server_requests_seconds_max` per `uri`
- MongoDB hotspots: `histogram_quantile(0.95, rate(mongodb_driver_commands_seconds_bucket[5m]))` per `command`
- Thread pool saturation: `executor_active_threads / executor_pool_size` > 0.8 is a warning

**AI checker**
- OpenAI call duration: custom `openai.call_seconds` histogram (see §2.4)
- Queue depth: `rabbitmq_queue_messages{queue="checker.check.schedule.v1.q"}` — if growing, the checker is falling behind
- Error rate from checker: `rate(rabbitmq_published_total{exchange="edukate.check.result.v1"}[1m])` where result status = INTERNAL_ERROR

**Alerts to set up**
- p99 latency > 2s for any endpoint
- Error rate > 1% for 5 minutes
- RabbitMQ queue depth > 50 for 2 minutes (checker backlog)
- JVM heap > 80% for 10 minutes

---

## 6. Local Docker Compose setup

Add to `docker-compose.yml` for local development:
```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
    ports:
      - "3000:3000"
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning

  tempo:
    image: grafana/tempo:latest
    command: ["-config.file=/etc/tempo.yaml"]
    volumes:
      - ./monitoring/tempo.yaml:/etc/tempo.yaml
    ports:
      - "4318:4318"  # OTLP HTTP

  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
```

`monitoring/prometheus.yml`:
```yaml
scrape_configs:
  - job_name: edukate-backend
    static_configs:
      - targets: ["host.docker.internal:5801"]
    metrics_path: /actuator/prometheus

  - job_name: edukate-gateway
    static_configs:
      - targets: ["host.docker.internal:5811"]
    metrics_path: /actuator/prometheus

  - job_name: edukate-notifier
    static_configs:
      - targets: ["host.docker.internal:5821"]
    metrics_path: /actuator/prometheus

  - job_name: edukate-checker
    static_configs:
      - targets: ["host.docker.internal:5831"]
    metrics_path: /actuator/prometheus
```

Point `management.otlp.tracing.endpoint=http://localhost:4318/v1/traces` in `application-dev.properties`
and open Grafana at `http://localhost:3000`.
