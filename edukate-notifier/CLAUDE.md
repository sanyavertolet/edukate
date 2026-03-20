# edukate-notifier

Async notification delivery service. Consumes events from RabbitMQ, persists them in MongoDB, and exposes a REST API for users to read/manage their notifications.

## Port

5820 (main), 5821 (management)

## Domain

### Notification types (sealed class hierarchy)

| Type | Description |
|---|---|
| `SimpleNotification` | Plain text notification |
| `InviteNotification` | Bundle invitation |
| `CheckedNotification` | Problem checking result |

Polymorphic serialization via Jackson `@JsonTypeInfo`.

### REST Endpoints (`NotificationController`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/notifications` | Paginated list with optional filtering |
| POST | `/api/v1/notifications/mark-as-read` | Mark specific notifications as read |
| POST | `/api/v1/notifications/mark-all-as-read` | Mark all as read |
| GET | `/api/v1/notifications/count` | Read/unread statistics |

### Key Services

- `NotificationService` — business logic: persist, retrieve, mark as read, statistics
- `NotificationListener` — RabbitMQ consumer on `notifier.notify.v1.q`; receives `BaseNotificationCreateRequest`, calls `saveIfAbsent()`

### Idempotency

`saveIfAbsent()` uses UUID-based deduplication — safe to re-deliver messages.

## Dependencies

`edukate-common`, `edukate-auth`, `edukate-messaging`

## Configuration

- `application.properties` / `application-dev.properties`
- MongoDB URI, RabbitMQ settings
- OpenAPI docs at `/swagger/notifier/api-docs`
- Profiles: `dev`, `secure`

## Testing Notes

- Use `WebTestClient` for controller integration tests
- Test `NotificationListener` by publishing to an embedded RabbitMQ or mock the listener directly
- Test `saveIfAbsent()` idempotency: duplicate messages should not create duplicate documents
- Test pagination and filtering in `getUserNotifications()`
- Use `StepVerifier` for reactive service-layer unit tests
- Test polymorphic serialization — all three notification subtypes must survive a round-trip through MongoDB
