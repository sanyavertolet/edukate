# edukate-auth

Shared library for JWT token generation/validation and HTTP cookie management. Used by `edukate-gateway` (issues tokens, validates via filter) and depended on by other services.

## Key Classes (Kotlin)

### `JwtTokenService`

- `generateToken(EdukateUserDetails): String` — creates a signed JWT with claims: subject (user ID), name, roles, status, expiration
- `getUserDetailsFromToken(String): EdukateUserDetails?` — parses and validates JWT; returns `null` if expired, throws `JwtException` on invalid/tampered tokens
- Signature: HMAC-SHA with key derived from `auth.jwt.secret`
- Key is initialized eagerly at construction time (no `@PostConstruct`)

### `AuthCookieService`

- `ejectToken(ServerWebExchange): Mono<String>` — extracts JWT string from `X-Auth` cookie; returns empty `Mono` if cookie is absent
- `respondWithToken(String): Mono<ResponseEntity<Void>>` — 204 response with `Set-Cookie` header for the auth cookie
- `respondWithExpiredToken(): Mono<ResponseEntity<Void>>` — 204 response clearing the auth cookie (maxAge=0)
- Cookie flags: `HttpOnly=true`, `Secure` (disabled in `dev` profile), `SameSite=Strict`, `Path=/`, domain-scoped

## Configuration Properties

```
auth.jwt.secret=<min 32 chars for HMAC-SHA>
auth.jwt.expirationSeconds=3600
auth.hostname=<cookie domain>
```

## Testing Notes

- Both services can be instantiated directly in tests — no Spring context needed
- `JwtTokenService`: pass secret and expiration via constructor; use `expirationSeconds=0` to test expired-token handling
- `AuthCookieService`: mock `Environment` with MockK (`every { env.matchesProfiles("dev") } returns ...`)
- Use `StepVerifier` from `reactor-test` for asserting `Mono` values
- Use `AssertJ` for header and status assertions on `ResponseEntity`
- Test patterns from `edukate-notifier` tests apply: `mockk()`, backtick function names, fixture object

## Testing Structure

```
src/test/kotlin/.../auth/
├── AuthFixtures.kt            # shared test data builders
└── services/
    ├── JwtTokenServiceTest.kt # round-trips, expiry, tampered/wrong-key rejection
    └── AuthCookieServiceTest.kt # cookie extraction, all cookie attribute flags
```
