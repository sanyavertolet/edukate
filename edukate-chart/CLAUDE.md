# edukate-chart — Helm Chart Guide

Helm chart for the Edukate platform. Deploys all microservices, infrastructure dependencies, and
the LGTM observability stack into a single Kubernetes namespace.

---

## Directory layout

```
edukate-chart/
├── Chart.yaml              # chart metadata + sub-chart dependencies (LGTM stack)
├── values.yaml             # defaults — safe to deploy locally / in staging
├── values-prod.yaml        # production overrides — layered on top of values.yaml
└── templates/
    ├── _helpers.tpl        # shared template helpers (labels, annotations, spring-boot.common)
    ├── _services.tpl       # service metadata/ports/selectors helpers
    ├── backend.yaml        # Deployment + ConfigMap + Service for edukate-backend
    ├── gateway.yaml        # …edukate-gateway (entry point, port 5810)
    ├── notifier.yaml       # …edukate-notifier (async notifications)
    ├── checker.yaml        # …edukate-checker (AI grading)
    ├── frontend.yaml       # …edukate-frontend (React SPA)
    ├── mongo.yaml          # MongoDB StatefulSet/Service
    ├── rabbitmq.yaml       # RabbitMQ StatefulSet/Service
    ├── s3.yaml             # MinIO (S3-compatible) StatefulSet/Service
    ├── ingress.yaml        # Ingress resource with optional TLS
    ├── cert-issuer.yaml    # cert-manager ClusterIssuer for Let's Encrypt
    └── edukate-namespace.yaml
```

---

## Common commands

```bash
# Add required Helm repos (one-time)
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

# Download sub-chart tarballs into charts/ (required after editing Chart.yaml dependencies)
helm dependency update edukate-chart/

# Lint the chart
helm lint edukate-chart/

# Render templates locally for review (no cluster needed)
helm template edukate edukate-chart/ -f edukate-chart/values-prod.yaml | less

# Render with LGTM enabled to verify observability resources
helm template edukate edukate-chart/ -f edukate-chart/values-prod.yaml \
  --set lgtm.enabled=true | grep -E "^kind:|^  name:"

# Deploy / upgrade (user runs this manually)
helm upgrade --install edukate edukate-chart/ \
  -f edukate-chart/values-prod.yaml \
  --set 'lgtm.kube-prometheus-stack.grafana.adminPassword=<pw>'
```

---

## Pre-install secrets

These Kubernetes Secrets must exist in the target namespace before `helm install`:

| Secret name                      | Keys                                               | Used by               |
|----------------------------------|----------------------------------------------------|-----------------------|
| `s3-secrets`                     | `region`, `access-key`, `secret-key`, `bucket`, `endpoint` | backend, checker |
| `mongodb-secrets`                | `connectionString`, `notifierConnectionString`     | backend, notifier     |
| `jwt`                            | `key`                                              | gateway               |
| `edukate-rabbit-default-user`    | `connection_string`                                | backend, notifier, checker |

---

## Helper templates

### `_helpers.tpl`

| Helper | Purpose |
|--------|---------|
| `common.labels` | Deployment-level labels: `io.kompose.service`, `app.kubernetes.io/name`, `app.kubernetes.io/part-of: edukate`, `version`, `env` |
| `pod.common.labels` | Pod template labels: `io.kompose.service`, `app.kubernetes.io/name`, `app.kubernetes.io/part-of: edukate`, `version` |
| `pod.common.annotations` | Prometheus scrape annotations — sets `scrape: "true"`, `path: /actuator/prometheus`, `port: <managementPort>` |
| `spring-boot.common` | Container `image`, `imagePullPolicy`, `ports`, `resources` |
| `spring-boot.common.env` | `SPRING_PROFILES_ACTIVE` env var |
| `spring-boot.management` | `startupProbe`, `livenessProbe`, `readinessProbe`, `lifecycle.preStop` |
| `spring-boot.config-volume[-mount]` | Mounts `<service>-config` ConfigMap at `/home/cnb/config/application.properties` |
| `cnb.securityContext` | `runAsUser/Group/fsGroup: 1000` for Paketo-built images |
| `rabbitmq.addresses.env` | Injects `RABBITMQ_ADDRESSES` from the RabbitMQ operator Secret |
| `s3.configuration.env` | Injects S3 env vars from `s3-secrets` (when `s3.enabled=true`) |

### `_services.tpl`

| Helper | Purpose |
|--------|---------|
| `service.common.metadata` | Service `name` |
| `service.common.ports` | Single port: `80 → containerPort` |
| `service.common.selectors` | Selector on `io.kompose.service` |

---

## Adding a new service

1. Add a values block in `values.yaml`:
   ```yaml
   myservice:
     name: myservice
     imageName: edukate-myservice
     pullPolicy: Always
     containerPort: 5840
     managementPort: 5841   # required to get Prometheus annotations for free
     clusterIP: null
     applicationProperties:
   ```

2. Create `templates/myservice.yaml` following the pattern in `backend.yaml`:
   - `Deployment` using `spring-boot.common`, `spring-boot.common.env`, `spring-boot.management`
   - `ConfigMap` named `<service>-config` with `application.properties` key
   - `Service` using `service.common.*` helpers

3. Add `managementPort` — `pod.common.annotations` in `_helpers.tpl:12` automatically adds
   `prometheus.io/port` so Prometheus picks it up without further changes.

---

## LGTM observability stack

Controlled by `lgtm.enabled` (default `false`). When enabled, four sub-charts are deployed in the
same namespace:

| Component | Sub-chart | Default DNS | Purpose |
|-----------|-----------|-------------|---------|
| Prometheus + Grafana | `kube-prometheus-stack` | `grafana:80` | Metrics + dashboards |
| Loki | `loki` | `loki:3100` | Log aggregation |
| Tempo | `tempo` | `tempo:3100` (API), `:4318` (OTLP HTTP), `:4317` (OTLP gRPC) | Distributed tracing |
| Promtail | `promtail` | DaemonSet (no service) | Ships pod logs → Loki |

### How scraping works
All Spring Boot pods already carry `prometheus.io/scrape: "true"` and
`prometheus.io/port: <managementPort>` annotations (from `pod.common.annotations` in `_helpers.tpl`).
The `additionalScrapeConfigs` in `values.yaml` configures Prometheus to discover and scrape these
pods automatically — no `ServiceMonitor` CRDs required.

### Trace → log correlation
Grafana datasources are pre-wired: Tempo links out to Loki by `traceId`, and Loki extracts
`traceId` from JSON log lines. This requires:
- `logback-spring.xml` emitting JSON with `traceId`/`spanId` fields (see OBSERVABILITY.md §4.1)
- `spring-boot-starter-opentelemetry` on each service classpath — activates `management.opentelemetry.*` autoconfiguration

### Enabling for production
```yaml
# values-prod.yaml already has:
lgtm:
  enabled: true
  kube-prometheus-stack:
    grafana:
      adminPassword: ""   # pass via --set or existingSecret
```

Pass the Grafana password at deploy time:
```bash
helm upgrade --install edukate edukate-chart/ \
  -f edukate-chart/values-prod.yaml \
  --set 'lgtm.kube-prometheus-stack.grafana.adminPassword=<pw>'
```

---

## TLS and Ingress

- `ingress.tls.enabled: true` enables TLS via cert-manager
- `ingress.tls.profile`: `"staging"` uses Let's Encrypt staging CA; `"prod"` uses production CA
- `ingress.host` sets the hostname for the Ingress rule
- `cert-issuer.yaml` creates a `ClusterIssuer` pointing to Let's Encrypt ACME

---

## Key values reference

| Key | Default | Description |
|-----|---------|-------------|
| `namespace` | `default` | Kubernetes namespace for all resources |
| `imageRegistry` | `ghcr.io/sanyavertolet` | Container image registry prefix |
| `dockerTag` | `latest` | Image tag applied to all services |
| `springBootResources` | 256Mi/512Mi, 100m/500m | Default resource requests/limits for Spring Boot pods |
| `lgtm.enabled` | `false` | Toggle the entire LGTM stack on/off |
| `backend.managementPort` | `5801` | Spring Actuator port (Prometheus, health probes) |
| `gateway.managementPort` | `5811` | Same for gateway |
| `notifier.managementPort` | `5821` | Same for notifier |
| `checker.managementPort` | `5831` | Same for checker |
