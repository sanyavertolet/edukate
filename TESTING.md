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
- **`CommonFixtures`** — shared builders for `UserCredentials`, `EdukateUserDetails`, and notification create requests; used across `edukate-common`, `edukate-auth`, and `edukate-notifier`.

## Module Test Status

| Module              | Tests | Status     | Scope                                                 |
|:--------------------|------:|:-----------|:------------------------------------------------------|
| `edukate-auth`      |    22 | ✅ Done     | Unit — services, cookie flags, JWT round-trips        |
| `edukate-notifier`  |    74 | ✅ Done     | Unit + slice + repository                             |
| `edukate-common`    |    25 | ✅ Done     | Unit — domain models, enums, security utils           |
| `edukate-storage`   |     7 | ✅ Done     | Unit — `FileKey` parsing, serialization, equality     |
| `edukate-backend`   |     0 | 🔲 Planned | Full suite planned; mirrors `edukate-notifier` layers |
| `edukate-gateway`   |     0 | 🔲 Planned | Test plan in `edukate-gateway/TESTING.md`             |
| `edukate-checker`   |     0 | 🔲 Planned | Test plan in `edukate-checker/TESTING.md`             |
| `edukate-messaging` |     0 | —          | Topology constants only; tested via consumers         |

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

## edukate-common

Pure unit tests — no Spring context loaded.

| File                        | Tests | What it covers                                                                                                                                                     |
|:----------------------------|------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CommonFixtures.kt`         |     — | Shared builders: `userCredentials()`, `userDetails()`, notification create-request factories                                                                       |
| `SubmissionStatusTest.kt`   |     2 | `from(CheckStatus)` mapping, `best()` comparator across all status combinations                                                                                    |
| `UserRoleTest.kt`           |     6 | `asSpringSecurityRole`, `asGrantedAuthority`, `listToString`/`fromString` CSV round-trip, `anyRole`                                                                |
| `UserCredentialsTest.kt`    |     2 | `newUser()` factory defaults, `toString()` hides `encodedPassword`                                                                                                 |
| `EdukateUserDetailsTest.kt` |     7 | Constructor from credentials, null-id guard, authorities mapping, `isEnabled`, `eraseCredentials`, `toPreAuthenticatedAuthenticationToken`, `toString` hides token |
| `AuthUtilsTest.kt`          |     3 | `id(Authentication)` extraction and null handling, `monoId()` reactive wrapper                                                                                     |
| `HttpHeadersUtilsTest.kt`   |     5 | `populateHeaders` sets 4 X-Auth headers, `toEdukateUserDetails` round-trip, missing-header nulls, multi-value last-wins                                            |

**Patterns:** Direct instantiation; MockK for `Authentication`; `StepVerifier` for `Mono` assertions.

---

## edukate-storage

Pure unit tests — no Spring context, no real S3.

| File             | Tests | What it covers                                                                                                                                                                                             |
|:-----------------|------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FileKeyTest.kt` |     7 | `FileKey.of()` valid/invalid paths, leading-slash normalisation, `toString()` round-trips, `prefix()`, `typeOf()`/`ownerOf()`, `equals`/`hashCode` identity fields, Jackson `_type` polymorphic round-trip |

**Patterns:** Direct instantiation; `ObjectMapper` with `registerKotlinModule()`; AssertJ `extracting`, `satisfies`, `hasSameHashCodeAs`.

---

## edukate-backend

No tests. Full suite is planned. Intended layers mirror `edukate-notifier`:

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

No tests yet. Kotlin migration complete. Full test plan is in `edukate-checker/TESTING.md`.

Planned layers:

| File                               | Strategy  | What it covers                                                          |
|------------------------------------|-----------|-------------------------------------------------------------------------|
| `CheckerFixtures.kt`               | —         | Shared builders for all test files                                      |
| `RequestContextTest.kt`            | Pure unit | `init` validation — blank text, empty images                            |
| `ModelResponseTest.kt`             | Pure unit | Default values, Jackson round-trip, `@JsonPropertyDescription` presence |
| `CheckResultMessageUtilsTest.kt`   | Pure unit | Trust clamping, `errorType` enforcement on `SUCCESS`                    |
| `NoopChatServiceTest.kt`           | Pure unit | Stub response values, `@PostConstruct` log                              |
| `RabbitConfigTest.kt`              | Pure unit | Queue name, durability, binding routing key                             |
| `CheckerServiceTest.kt`            | MockK     | Orchestration, error fallback via `onErrorReturn`                       |
| `SpringAiChatServiceTest.kt`       | MockK     | `ChatClient` builder chain, null entity, `boundedElastic`               |
| `RabbitResultPublisherTest.kt`     | MockK     | `convertAndSend` args, retry on transient failure                       |
| `MediaContentResolverTest.kt`      | MockK     | Byte assembly, `MediaType` propagation, S3 error                        |
| `SubmissionContextListenerTest.kt` | MockK     | Delegation, `.block()` completes, error does not throw                  |

**Patterns:** `StepVerifier` for all `Mono`/`Flux` assertions; MockK for all dependencies;
no Spring context in unit tests; no real OpenAI or S3 calls.

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
