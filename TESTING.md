# Testing

## Global Test Stack

| Library                     | Version | Purpose                                      |
|:----------------------------|:--------|:---------------------------------------------|
| JUnit 5                     | via BOM | Test runner                                  |
| AssertJ                     | via BOM | Fluent assertions                            |
| MockK                       | 1.14.2  | Kotlin-native mocking                        |
| spring-mockk                | 4.0.2   | `@MockkBean` / `@SpykBean` for Spring slices |
| reactor-test (StepVerifier) | via BOM | Asserting `Mono` / `Flux` streams            |
| Flapdoodle embedded MongoDB | 4.24.0  | In-process MongoDB for repository tests      |

## Shared Test Infrastructure (edukate-common)

- **`NoopWebSecurityConfig`** — disables security for `@WebFluxTest` slices; import it in controller tests.
- **`NoopNotifier`** — no-op `Notifier` stub; used when a service under test triggers notifications.

## Module Test Status

| Module              | Tests | Status     | Scope                                                |
|:--------------------|------:|:-----------|:-----------------------------------------------------|
| `edukate-auth`      |    22 | ✅ Done     | Unit — services, cookie flags, JWT round-trips       |
| `edukate-notifier`  |    74 | ✅ Done     | Unit + slice + repository                            |
| `edukate-backend`   |     1 | 🔲 Minimal | Smoke test only — full suite planned                 |
| `edukate-gateway`   |     0 | 🔲 Planned | Test plan in `edukate-gateway/TESTING.md`            |
| `edukate-checker`   |     0 | 🔲 Planned | Pending Kotlin migration                             |
| `edukate-common`    |     0 | —          | Shared library; tested via consumers                 |
| `edukate-messaging` |     0 | —          | Topology constants only; tested via consumers        |
| `edukate-storage`   |     0 | —          | Interfaces; integration-tested via `edukate-backend` |

---

## edukate-auth

Pure unit tests — no Spring context loaded.

| File                       | Tests | What it covers                                                                                   |
|:---------------------------|------:|:-------------------------------------------------------------------------------------------------|
| `AuthFixtures.kt`          |     — | Shared builders: `userDetails()`, secret, hostname constants                                     |
| `JwtTokenServiceTest.kt`   |     9 | Token generation, parsing, expiry, tampered-key rejection, roles                                 |
| `AuthCookieServiceTest.kt` |    13 | Cookie extraction from exchange, `Set-Cookie` flags (HttpOnly, Secure, SameSite, MaxAge), logout |

**Patterns:** Direct constructor instantiation, MockK for `Environment`, `StepVerifier` for
`Mono<ResponseEntity<Void>>`.

---

## edukate-notifier

Layered tests: pure unit → Spring slice → embedded MongoDB.

| File                               | Tests | What it covers                                                           |
|:-----------------------------------|------:|:-------------------------------------------------------------------------|
| `NotificationFixtures.kt`          |     — | Builders for create-requests, entities, DTOs; `mockAuthentication()`     |
| `BaseNotificationTest.kt`          |    15 | `fromCreationRequest` polymorphism, `markAsRead`, `toDto`, null auditing |
| `NotificationSerializationTest.kt` |    15 | Jackson `_type` discriminator, create-request / entity / DTO round-trips |
| `NotificationControllerTest.kt`    |     7 | `GET /notifications`, `POST /mark-as-read`, `GET /count` — WebFlux slice |
| `NotificationRepositoryTest.kt`    |    19 | All repository queries, pagination, polymorphic persistence, auditing    |
| `NotificationServiceTest.kt`       |    14 | `saveIfAbsent` idempotency, filtering, paging, auth checks               |
| `NotificationListenerTest.kt`      |     4 | RabbitMQ listener delegates to `saveIfAbsent`; error-swallowing behavior |

**Patterns:**

- Entity/serialization tests: no Spring context, direct instantiation.
- Controller tests: `@WebFluxTest` + `@Import(NoopWebSecurityConfig::class)` + `@MockkBean`.
- Repository tests: `@DataMongoTest` + `@Import(MongoConfig::class)` + Flapdoodle embedded MongoDB.
- Service tests: plain MockK, no context.

---

## edukate-backend

| File                           | Tests | What it covers       |
|:-------------------------------|------:|:---------------------|
| `EdukateApplicationTests.java` |     1 | Spring context loads |

Full test suite is planned. Intended layers mirror `edukate-notifier`:

- Repository tests with Flapdoodle embedded MongoDB
- Service tests with MockK
- Controller tests with `@WebFluxTest` + `WebTestClient`

---

## edukate-gateway

No tests yet. Test plan is documented in `edukate-gateway/TESTING.md`. Planned coverage:

- `JwtAuthenticationFilter` — valid/expired/missing token paths
- `AuthController` — sign-in, sign-up, sign-out flows
- Security integration via `@SpringBootTest` + `WebTestClient`

---

## edukate-checker

No tests. Awaiting Kotlin migration. Test plan documented in `edukate-checker/` (post-migration).
Planned: MockK for `ChatService`, `ResultPublisher`; `@WebFluxTest` for REST endpoints.

---

## Running Tests

```bash
# All modules
./gradlew test

# Single module
./gradlew :edukate-notifier:test

# Single class
./gradlew :edukate-auth:test --tests "*.JwtTokenServiceTest"
```
