# edukate-messaging

Shared library that defines the RabbitMQ topology (exchange, routing keys, queues) used across all services.

## Topology Constants (`RabbitTopology`)

| Constant | Value | Direction |
|---|---|---|
| Exchange | `edukate.exchange` | All messages |
| `edukate.check.schedule.v1` | routing key | Backend → Checker |
| `edukate.check.result.v1` | routing key | Checker → Backend |
| `edukate.notify.v1` | routing key | Backend → Notifier |
| `checker.check.schedule.v1.q` | queue | Checker consumer |
| `backend.check.result.v1.q` | queue | Backend consumer |
| `notifier.notify.v1.q` | queue | Notifier consumer |

## Auto-Configuration

`RabbitAutoConfiguration` — Spring Boot auto-configuration; declares exchanges, queues, and bindings automatically when the library is on the classpath.

## Testing Notes

- Use Testcontainers RabbitMQ or an embedded broker for integration tests that involve messaging
- In unit tests, mock the `RabbitTemplate` / `AmqpTemplate` — do not rely on a real broker
- When testing listener classes in `edukate-notifier` or `edukate-checker`, inject messages directly into the listener method rather than going through RabbitMQ
