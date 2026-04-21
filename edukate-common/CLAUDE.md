# edukate-common

Shared domain models, enums, security configuration, and utilities used across all services.

## Key Packages

### Domain Enums

| Class              | Values                                                         |
|--------------------|----------------------------------------------------------------|
| `UserRole`         | `USER`, `MODERATOR`, `ADMIN` — integrates with Spring Security |
| `UserStatus`       | User account status                                            |
| `SubmissionStatus` | `PENDING`, `ACCEPTED`, `REJECTED`                              |
| `CheckStatus`      | Check operation state                                          |
| `CheckErrorType`   | Classification of check failures                               |

### User / Auth Models

- `UserCredentials` — authentication model
- `EdukateUserDetails` — Spring Security `UserDetails` implementation; carries user ID, name, roles, status
- `AuthUtils` — extracts user ID from reactive security context (`monoId()`)
- `HttpHeadersUtils`, `AuthHeaders` — header name constants for forwarded user context
- `PublicEndpoints` — list of endpoints that bypass security

### Notification Create Requests (sealed hierarchy)

Used by `edukate-backend` to send to `edukate-notifier` via RabbitMQ.
All `targetUserId` fields are `Long` (PostgreSQL user IDs). `CheckedNotificationCreateRequest` uses
`problemKey: String` (composite key like `savchenko/1.1.1`) and `submissionId: Long`.
`InviteNotificationCreateRequest` uses `problemSetName` and `problemSetShareCode` (not "bundle").

| Class                              | Purpose                |
|------------------------------------|------------------------|
| `BaseNotificationCreateRequest`    | Sealed parent          |
| `SimpleNotificationCreateRequest`  | Plain text             |
| `InviteNotificationCreateRequest`  | Problem set invitation |
| `CheckedNotificationCreateRequest` | Check result           |

### Check Models

- `CheckResultMessage` — RabbitMQ message from checker to backend; `submissionId: Long`
- `CheckResultInfo` — lightweight check result reference; `id: Long`
- `SubmissionContext` — payload sent to checker; `submissionId: Long`, `problemId: Long`

### Security Configuration

- `WebSecurityConfig` — base security config (override per-service)
- `RoleHierarchyConfiguration` — `ADMIN > MODERATOR > USER` hierarchy
- `NoopWebSecurityConfig` — permits all; use in tests via `@Import(NoopWebSecurityConfig::class)`

### Notifier Abstraction

- `Notifier` interface: `Mono<String> notify(BaseNotificationCreateRequest)`
- `RabbitNotifier` — publishes to RabbitMQ; activated via `notifier` profile
- `NoopNotifier` — silently discards; used in tests and when notifier is disabled

### OpenAPI

- `OpenApiConfiguration` — shared Swagger/OpenAPI bean; sets server URL (from `gateway.url`), `cookieAuth` security scheme, and AGPL v3 license info

## Testing Notes

- No application context needed for most unit tests — pure model/utility tests
- `NoopWebSecurityConfig` is the standard way to disable security in service-layer tests
- `NoopNotifier` is the standard mock for `Notifier` in tests

## Test Suite

Run tests: `./gradlew :edukate-common:test`

```
src/test/kotlin/.../common/
├── CommonFixtures.kt           — shared test data builders
├── SubmissionStatusTest.kt     — from() and best() logic
├── users/
│   ├── UserRoleTest.kt         — asSpringSecurityRole, asGrantedAuthority, fromString, listToString, anyRole
│   ├── UserCredentialsTest.kt  — newUser() factory, toString() hides encodedPassword
│   └── EdukateUserDetailsTest.kt — authorities, isEnabled, eraseCredentials, toPreAuthenticatedAuthenticationToken
└── utils/
    ├── AuthUtilsTest.kt        — id() and monoId() with null/valid authentication
    └── HttpHeadersUtilsTest.kt — populateHeaders, toEdukateUserDetails round-trip and null-header handling
```

### Key patterns

- Instantiate classes directly — no Spring context required
- MockK for `Authentication` mock in `AuthUtilsTest`
- `StepVerifier` from `reactor-test` for `Mono` assertions
- Group related assertions in one test method (one test per concept, not one per assertion)
