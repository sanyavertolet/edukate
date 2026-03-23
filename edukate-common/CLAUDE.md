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

Used by `edukate-backend` to send to `edukate-notifier` via RabbitMQ:

| Class                              | Purpose           |
|------------------------------------|-------------------|
| `BaseNotificationCreateRequest`    | Sealed parent     |
| `SimpleNotificationCreateRequest`  | Plain text        |
| `InviteNotificationCreateRequest`  | Bundle invitation |
| `CheckedNotificationCreateRequest` | Check result      |

### Security Configuration (Java)

- `WebSecurityConfig` — base security config (override per-service)
- `RoleHierarchyConfiguration` — `ADMIN > MODERATOR > USER` hierarchy
- `NoopWebSecurityConfig` — permits all; use in tests via `@Import(NoopWebSecurityConfig.class)`

### Notifier Abstraction (Java)

- `Notifier` interface: `Mono<String> notify(BaseNotificationCreateRequest)`
- `RabbitNotifier` — publishes to RabbitMQ; activated via `notifier` profile
- `NoopNotifier` — silently discards; used in tests and when notifier is disabled

### OpenAPI

- `OpenApiConfiguration` — shared Swagger/OpenAPI bean

## Testing Notes

- No application context needed for most unit tests — pure model/utility tests
- Test `EdukateUserDetails` role hierarchy via Spring Security's `RoleHierarchyVoter`
- `NoopWebSecurityConfig` is the standard way to disable security in service-layer tests
- `NoopNotifier` is the standard mock for `Notifier` in tests
