# Tech Debt & Improvement Notes — edukate-auth

---

## Security Issues

### 1. JWT validation only catches `ExpiredJwtException`

**File:** `services/JwtTokenService.kt`

```kotlin
} catch (_: ExpiredJwtException) {
    null
}
```

`MalformedJwtException`, `SignatureException`, `UnsupportedJwtException`, and `IllegalArgumentException`
are **not caught** and bubble up as unchecked exceptions. Callers see an inconsistent contract:
expired tokens return `null`, but tampered or malformed tokens throw. All JWT parse exceptions should
be caught and treated as invalid tokens.

### 2. Secret key length not validated

**File:** `services/JwtTokenService.kt`

HMAC-SHA-256 requires at least 32 bytes. If the configured secret is shorter, JJWT will throw at
runtime, not at startup, and the error message is cryptic. A `@PostConstruct` check with a clear
message would surface this early.

### 3. JWT claim keys are magic strings

**File:** `services/JwtTokenService.kt`

`"name"`, `"roles"`, and `"status"` are referenced as raw strings in both getter and setter paths.
A typo silently creates a `null` claim. Extract to `companion object` constants.

---

## Correctness

### 4. `expirationTimeSeconds` not validated

**File:** `services/JwtTokenService.kt`, `services/AuthCookieService.kt`

A zero or negative value produces an already-expired token with no error. Add a `require(value > 0)`
check on injection.

### 5. `hostname` not validated in `AuthCookieService`

**File:** `services/AuthCookieService.kt`

`@param:Value("\${auth.cookie.hostname}")` is not null-checked. A blank hostname makes cookies
domain-less (browser-wide), which is a security risk.

---

## Observability

### 6. No logging when token parsing fails

**File:** `services/JwtTokenService.kt`

The `catch` block is silent. A `log.debug("JWT token rejected: ...", ex.message)` would make
production debugging possible without leaking sensitive data to logs.

### 7. No metrics for token generation / validation

No counters for tokens issued, validated, or rejected. Add Micrometer counters to track auth
pressure and anomalies.

---

## Nice-to-Have

- **Token revocation list**: tokens remain valid until natural expiry; no way to invalidate a
  compromised token
- **Refresh tokens**: users must re-login when the access token expires; a refresh endpoint would
  improve UX
- **Audit logging**: no record of who generated or validated a token and when
