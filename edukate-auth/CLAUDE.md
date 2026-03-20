# edukate-auth

Shared library for JWT token generation/validation and HTTP cookie management. Used by `edukate-gateway` (issues tokens) and `edukate-backend` (validates tokens).

## Key Classes (all Java)

### `JwtTokenService`

- `generateToken(EdukateUserDetails)` → signed JWT string
- `getUserDetailsFromToken(String)` → parsed `EdukateUserDetails`
- Claims stored: subject (user ID), name, roles, status, expiration
- Signature: HMAC-SHA with configurable secret key
- Expiration: `auth.jwt.expirationSeconds` (default 3600s)

### `AuthCookieService`

- `ejectToken(ServerWebExchange)` → extracts JWT string from cookie
- `respondWithToken(token)` → `ServerResponse` with `Set-Cookie` header
- `respondWithExpiredToken()` → clears auth cookie
- Cookie flags: `HttpOnly`, `Secure` (disabled in dev), `SameSite=STRICT`, domain-scoped

## Configuration Properties

```
auth.jwt.secret=...
auth.jwt.expirationSeconds=3600
```

## Testing Notes

- Unit test `JwtTokenService` directly — no Spring context needed
- Test round-trip: `generateToken` → `getUserDetailsFromToken` preserves all claims
- Test token expiry: verify expired tokens are rejected
- Test `AuthCookieService` cookie flags (Secure off in dev profile)
- No repositories or external I/O — pure unit tests with JUnit + AssertJ
