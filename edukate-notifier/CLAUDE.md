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

## Testing

Test dependencies: `spring-boot-starter-test`, `reactor-test`, `mockk`, `springmockk`, `flapdoodle-embed-mongo`.

Run tests:
```bash
./gradlew :edukate-notifier:test
```

### Test layout

```
src/test/kotlin/.../notifier/
├── NotificationFixtures.kt              — shared test data builders + mockAuthentication()
├── entities/
│   ├── BaseNotificationTest.kt          — fromCreationRequest, markAsRead, toDto (pure unit)
│   └── NotificationSerializationTest.kt — Jackson polymorphic round-trips (pure unit)
├── services/
│   ├── NotificationServiceTest.kt       — business logic (MockK repository)
│   └── NotificationListenerTest.kt      — RabbitMQ listener delegation (MockK service)
├── controllers/
│   └── NotificationControllerTest.kt    — @WebFluxTest + WebTestClient
└── repositories/
    └── NotificationRepositoryTest.kt    — @DataMongoTest + Flapdoodle embedded MongoDB
```

### Key testing patterns

- **Unit tests** (`BaseNotificationTest`, `NotificationSerializationTest`): no Spring context, JUnit 5 + AssertJ only.
- **Service tests** (`NotificationServiceTest`): `mockk()` for repository, `StepVerifier` for reactive assertions.
- **Listener tests** (`NotificationListenerTest`): `mockk()` for service; the bare `.subscribe()` swallowing errors is explicitly documented as a known issue in the test.
- **Controller tests** (`NotificationControllerTest`): `@WebFluxTest(NotificationController::class)` + `@Import(NoopWebSecurityConfig::class)` + `@MockkBean` (from `springmockk`).
- **Repository tests** (`NotificationRepositoryTest`): `@DataMongoTest` + `@Import(MongoConfig::class)` for `@EnableReactiveMongoAuditing`; `@BeforeEach` deletes all documents for isolation.
