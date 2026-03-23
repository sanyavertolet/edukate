# edukate-gateway

API Gateway — a single entry point for all client requests. Handles authentication (sign-in, sign-up, sign-out) and routes traffic to downstream services.

## Responsibilities

- JWT issuance and cookie management (sign-in/sign-up/sign-out endpoints live here)
- Request routing via Spring Cloud Gateway
- Swagger aggregation from all services at `/swagger`
- Security filter chain — downstream services trust forwarded user context via `X-Authorization-*` headers

## Key Classes

| Class                            | Package       | Role                                                                                        |
|----------------------------------|---------------|---------------------------------------------------------------------------------------------|
| `EdukateGatewayApplication`      | root          | Spring Boot entry point                                                                     |
| `GatewayProperties`              | `configs`     | `@ConfigurationProperties(prefix="gateway")` — backend/notifier URLs                        |
| `AuthController`                 | `controllers` | Sign-in, sign-up, sign-out endpoints (all return 204)                                       |
| `JwtAuthenticationFilter`        | `filters`     | `WebFilter` — extracts JWT from `X-Auth` cookie, validates, sets forwarded headers          |
| `WebSecurityConfig`              | `security`    | Reactive security chains (`@Profile("secure")`); registers filter at `AUTHENTICATION` order |
| `AuthService`                    | `services`    | signIn/signUp business logic                                                                |
| `BackendService`                 | `services`    | WebClient wrapper for `/internal/users/**` on `edukate-backend`                             |
| `UserDetailsService`             | `services`    | Implements `ReactiveUserDetailsService`; bridges backend user data to Spring Security       |
| `SignInRequest`, `SignUpRequest` | `dtos`        | Validated request DTOs (already in Kotlin)                                                  |

## Shared Dependencies

- **`edukate-auth`**: `JwtTokenService` (generate/parse JWT), `AuthCookieService` (set/clear `X-Auth` cookie)
- **`edukate-common`**: `EdukateUserDetails`, `UserCredentials`, `HttpHeadersUtils`, `PublicEndpoints`, `NoopWebSecurityConfig` (test utility)

## Configuration

- `application.yml` / `application-dev.yml`
- Port: 5810 (main), 5811 (management)
- Prod CORS: `https://edukate.mooo.com`; Dev CORS: `http://localhost:[*]`
- Profiles: `dev`, `secure`
- JWT secret from `${JWT_SECRET}` env var (prod) or hardcoded (dev)

## Documentation

| File           | Purpose                                                                                     |
|----------------|---------------------------------------------------------------------------------------------|
| `ROUTING.md`   | Full routing config — routes, security chains, header forwarding, CORS, Swagger aggregation |
| `TESTING.md`   | Test plan — per-class test cases, fixtures, coverage targets (write tests after migration)  |
| `MIGRATION.md` | Java → Kotlin migration plan — 8 phases, idioms, pitfalls, verification steps               |

## Testing Notes

- Controller slice tests: `@WebFluxTest` + `@Import(NoopWebSecurityConfig::class)` + `@MockkBean`
- Service unit tests: plain `mockk()` + `StepVerifier`, no Spring context
- Filter unit tests: `MockServerWebExchange` — no Spring context needed
- Security integration: `@SpringBootTest` with `secure` profile + `WebTestClient`
- No MongoDB in gateway — no Flapdoodle or Testcontainers needed
- See `TESTING.md` for the full test plan

## Keeping Documentation in Sync

**After any code change, update the relevant `.md` files:**

| Changed file                              | Update                        |
|-------------------------------------------|-------------------------------|
| `application.yml` routes, CORS, URLs      | `ROUTING.md`                  |
| `WebSecurityConfig` security rules        | `ROUTING.md`                  |
| `JwtAuthenticationFilter` header logic    | `ROUTING.md`                  |
| `PublicEndpoints` (in `edukate-common`)   | `ROUTING.md`                  |
| New class added / class renamed           | `CLAUDE.md` Key Classes table |
| Test cases added or changed               | `TESTING.md`                  |
| Migration phase completed or notes change | `MIGRATION.md`                |
