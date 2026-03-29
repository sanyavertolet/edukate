# Tech Debt & Improvement Notes — edukate-gateway

---

## Correctness

### 1. Cookie max-age hardcoded independently of JWT expiration

**File:** `configs/WebSecurityConfig.kt`

```kotlin
private const val COOKIE_MAX_AGE = 3600L
```

The cookie lifetime is hardcoded to 3600 s and is not derived from `auth.jwt.expirationSeconds`.
If the two values diverge, either the cookie expires before the token (user gets logged out with a
valid token) or the token expires while the cookie is still live (client retries with an expired
token and gets 401 with no clear reason).

Fix: inject `expirationSeconds` from the JWT config and use it in both places.

### 2. `signUp` returns 403 on failure instead of 409

**File:** `controllers/AuthController.kt`

`signIn` returns 403 on `switchIfEmpty`. `signUp` reuses the same error handling but the empty
Mono means "username already taken", which is semantically 409 Conflict. The client cannot
distinguish auth failure from duplicate username.

### 3. `BackendService` uses `lateinit` instead of safe initialization

**File:** `services/BackendService.kt`

```kotlin
private lateinit var webClient: WebClient
```

Initialized in `@PostConstruct`. Any failure in the injection phase leaves the field uninitialized,
causing `UninitializedPropertyAccessException` at call time — not at startup where it is easier to
diagnose. Use `by lazy { }` or a `@Bean` factory method instead.

---

## Reliability

### 4. No timeout or circuit breaker on backend calls

**File:** `services/BackendService.kt`

Raw `WebClient` calls with no `timeout()`, retry, or circuit breaker. A slow or unresponsive
backend stalls all gateway requests indefinitely.

### 5. Per-request user lookup — no caching

**File:** `filters/JwtAuthenticationFilter.kt`

Every request fetches fresh user details from the backend over HTTP. Under load this multiplies
backend traffic by the number of authenticated endpoints. Even a short-lived cache (e.g. 10 s)
would reduce backend pressure significantly.

---

## Security

### 6. Dev JWT secret committed to VCS

**File:** `src/main/resources/application-dev.yml`

The dev JWT signing secret is in source control. While marked dev-only, secrets in VCS are a
known anti-pattern that tends to leak into production configs over time. Use environment variables
or a secrets manager even for dev.

### 7. CORS `allowedHeaders` includes unused `api_key`

**File:** `configs/WebSecurityConfig.kt`

`"api_key"` is listed in `allowedHeaders` but is never used by any controller or filter. Dead
config that widens the attack surface without purpose.

### 8. No rate limiting on auth endpoints

`/auth/sign-in` and `/auth/sign-up` have no rate limiting. Both are vulnerable to brute-force and
enumeration attacks.

---

## Observability

### 9. No request correlation IDs

No `X-Trace-ID` or `X-Correlation-ID` propagation. A request flowing through gateway → backend
→ notifier cannot be traced end-to-end in logs.

### 10. No audit logging for authentication events

Successful logins, failed logins, and sign-ups are not logged. Required for any security audit
trail.

---

## Nice-to-Have

- **Email verification on sign-up**: users can register with non-existent email addresses
- **Password reset flow**: no forgotten-password mechanism
- **Active session list**: users cannot see or revoke their own sessions
