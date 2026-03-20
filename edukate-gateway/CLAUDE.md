# edukate-gateway

API Gateway — single entry point for all client requests. Handles authentication (sign-in, sign-up, sign-out) and routes traffic to downstream services.

## Responsibilities

- JWT issuance and cookie management (sign-in/sign-up/sign-out endpoints live here)
- Request routing via Spring Cloud Gateway
- Swagger aggregation from all services at `/swagger`
- Security filter chain — downstream services trust forwarded user context

## Routing Rules

| Pattern | Destination |
|---|---|
| `/api/*/auth/**` | Handled in gateway (no forwarding) |
| `/api/*/notifications/**` | `edukate-notifier` (port 5820) |
| `/api/**` | `edukate-backend` (port 5800) |

## Key Classes

- `SignUpRequest`, `SignInRequest` — request DTOs in `io.github.sanyavertolet.edukate.gateway.dtos`
- Auth controller handles sign-in/sign-up using `JwtTokenService` and `AuthCookieService` from `edukate-auth`

## Configuration

- `application.yml` / `application-dev.yml`
- Port: 5810 (main), 5811 (management)
- CORS: `https://edukate.mooo.com`
- Profiles: `dev`, `secure`

## Testing Notes

- Tests should cover routing logic and auth endpoints
- Use `WebTestClient` for reactive gateway testing
- Mock downstream services — gateway tests should not require backend/notifier running
- Test security: unauthenticated access to protected routes returns 401, sign-in sets the auth cookie correctly
- `NoopWebSecurityConfig` from `edukate-common` is available for disabling security in tests
