# Tech Debt & Improvement Notes — edukate-messaging

---

## Correctness

### 1. No Dead Letter Queue configured

**File:** `RabbitAutoConfiguration.kt`

The `TopicExchange` and `Jackson2JsonMessageConverter` beans are declared but no Dead Letter
Exchange (DLX) or Dead Letter Queue (DLQ) is configured. Messages that fail to deserialize or
cause listener exceptions are simply dropped. Any transient processing failure results in silent
message loss.

### 2. Queue declarations are absent from auto-configuration

**File:** `RabbitAutoConfiguration.kt`

The auto-configuration declares the exchange but not the queues — queues are declared separately
in each consuming service (`edukate-checker/configs/RabbitConfig.kt`, etc.). If a service is
deployed without its queue having been created yet, the exchange silently drops messages routed to
that queue. The auto-configuration should declare all known queues with their bindings and DLX
configuration.

### 3. No acknowledgement strategy defined

`RabbitAutoConfiguration` sets up the message converter but does not configure whether listeners
use `AUTO`, `MANUAL`, or `NONE` acknowledgement. Individual listeners inherit the default
(`AUTO`), which means a message is ACKed as soon as the listener method returns — even if the
reactive chain hasn't completed yet (see `edukate-backend`'s `.block()` issue).

---

## Maintainability

### 4. Queue naming is inconsistent

**File:** `RabbitTopology.kt`

| Constant             | Name                          |
|----------------------|-------------------------------|
| `Q.SCHEDULE_CHECKER` | `edukate.check.schedule.v1.q` |
| `Q.RESULT_BACKEND`   | `edukate.check.result.v1.q`   |
| `Q.NOTIFY`           | `edukate.notify.v1.q`         |

The first two follow a `service.action.version.q` pattern; the third skips the service prefix.
Consistent naming prevents confusion when browsing the RabbitMQ management UI.

### 5. No versioning strategy for routing keys

Routing keys include `.v1` suffixes but there is no documented strategy for introducing `.v2`
keys. Services must handle both old and new formats or be deployed simultaneously — neither is
currently possible without a migration plan.

---

## Observability

### 6. No message-level metrics

No queue-depth monitoring, throughput counters, or latency histograms are exported. Add a
`RabbitListenerMetricsCollector` or Micrometer RabbitMQ binder to get visibility into message
processing.

### 7. No correlation ID propagation

Messages carry no trace context. A submission that flows through backend → checker → backend
cannot be traced end-to-end in distributed logs.

---

## Nice-to-Have

- **DLQ with alerting**: failed messages should land in a known DLQ with an alert so they can be
  replayed rather than silently lost
- **Message TTL**: define a maximum lifetime for unprocessed messages so stale check requests do
  not block resources indefinitely
