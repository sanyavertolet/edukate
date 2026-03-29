# Tech Debt & Improvement Notes — edukate-common

---

## Correctness

### 1. Role hierarchy configuration is non-functional

**File:** `configs/RoleHierarchyConfiguration.kt`

```kotlin
// todo: this does not seem to work at all
```

The hierarchy `ADMIN > MODERATOR > USER` is declared but does not take effect. Access-control
decisions fall back to exact role matching, making ADMIN unable to access MODERATOR-only endpoints
without being explicitly granted both roles. Either fix the hierarchy or remove the configuration
and the comment, so the behavior is explicit.

### 2. Unsafe cast in `AuthUtils`

**File:** `utils/AuthUtils.kt`

```kotlin
authentication?.principal as EdukateUserDetails
```

If the principal is not an `EdukateUserDetails` (e.g. during tests or with a different
authentication provider), this throws `ClassCastException`. Use `as?` with a null fallback or an
`is` guard.

### 3. `SubmissionStatus.best()` relies on declaration order

**File:** `SubmissionStatus.kt`

The "best" status is found via `<` on enum ordinals. Inserting or reordering enum values silently
breaks the comparison. Use an explicit `weight` property or a `when` expression instead.

### 4. `HttpHeadersUtils` uses `!!` after a null check

**File:** `utils/HttpHeadersUtils.kt`

The code checks `any { it == null }` and then uses `!!` on the same values a few lines later.
This pattern is fragile: if the logic diverges, the `!!` will throw. Use `requireNotNull` with a
descriptive message or restructure to avoid the double check.

---

## Design

### 5. `PublicEndpoints` is not extensible

**File:** `utils/PublicEndpoints.kt`

The list of public endpoints is a hardcoded `val` in a library module. Consuming services cannot
add their own public paths without modifying the shared library. Extract to a configuration
property or use a `@ConditionalOnProperty` contribution pattern.

### 6. `RabbitNotifier` is fire-and-forget with no error handling

**File:** `notifiers/RabbitNotifier.kt`

`rabbitTemplate.convertAndSend()` throws on failure but is not wrapped in any error-handling
logic. A failed notification delivery silently disappears. Add a try-catch with logging or migrate
to `Mono.fromRunnable { }.retry().subscribeOn(boundedElastic)` pattern (same as
`RabbitResultPublisher` in edukate-checker).

### 7. `UserRole` serialization uses comma-delimited strings

**File:** `UserRole.kt`

`listToString()` joins roles with `,`. If a future role name ever contains a comma, parsing
breaks silently. Use a different delimiter (e.g. `|`) or switch to a JSON array representation in
the JWT claim.

---

## Observability

### 8. `NoopNotifier` logs at WARN level

**File:** `notifiers/NoopNotifier.kt`

Stub behavior is expected in dev/test environments and should be at DEBUG level. Warn-level noise
in dev logs masks real warnings.

---

## Nice-to-Have

- **`@Validated` on `ConfigurationProperties` classes**: property binding errors are caught at
  startup rather than at first use
- **Structured constants for HTTP header names**: `AuthHeaders` is an enum but header name strings
  are also scattered in `HttpHeadersUtils`; consolidate to one source of truth
