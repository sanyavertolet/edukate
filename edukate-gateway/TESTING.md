# edukate-gateway: Testing

Unit and controller slice tests are implemented. `WebSecurityConfigTest` is a full-stack integration test that requires
the backend, MongoDB, and RabbitMQ to be running — it is not part of the routine CI test run. This document covers
both the implemented tests and their specifications.

## Infrastructure

- **Unit tests**: `StepVerifier` from `reactor-test` for reactive chains. Mock dependencies with MockK (
  `io.mockk:mockk`).
- **Controller slice tests**: `@WebFluxTest` + `@Import(NoopWebSecurityConfig::class)` + `@MockkBean` from
  `com.ninja-squad:springmockk`. No Spring Security filter chain in these tests.
- **Filter unit tests**: Construct `MockServerWebExchange` manually; no Spring context needed.
- **Security integration tests**: `@SpringBootTest` with `secure` profile + `WebTestClient`.
- **Authentication**: No MongoDB in gateway — no `@DataMongoTest` or Flapdoodle needed.
- **Test naming**: Use backtick function names (`` `signIn returns 204 when credentials are valid` ``).

### Test Dependencies to add to `build.gradle.kts`

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.projectreactor:reactor-test")
testImplementation(libs.mockk)
testImplementation(libs.springmockk)
```

---

## Fixtures: `GatewayFixtures`

Location: `src/test/kotlin/io/github/sanyavertolet/edukate/gateway/GatewayFixtures.kt`

A single `object` following the same pattern as `NotificationFixtures` in `edukate-notifier`. Factory functions use
default parameters for easy customization per test.

| Function                                                               | Returns                                                                                                     |
|------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `signInRequest(username, password)`                                    | `SignInRequest`                                                                                             |
| `signUpRequest(username, password, email)`                             | `SignUpRequest`                                                                                             |
| `userCredentials(id, username, encodedPassword, email, roles, status)` | `UserCredentials`                                                                                           |
| `edukateUserDetails(id, username, roles, status, token)`               | `EdukateUserDetails`                                                                                        |
| `mockAuthentication(id, username)`                                     | `PreAuthenticatedAuthenticationToken` via `edukateUserDetails(...).toPreAuthenticatedAuthenticationToken()` |

---

## Unit Tests

### `AuthServiceTest`

No Spring context. Construct `AuthService` directly with `mockk()` collaborators.

```kotlin
private val userDetailsService: UserDetailsService = mockk()
private val passwordEncoder: PasswordEncoder = mockk()
private val jwtTokenService: JwtTokenService = mockk()
private val authService = AuthService(userDetailsService, passwordEncoder, jwtTokenService)
```

#### `signIn`

| Method                                                   | What it tests                                                                                                                              |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `signIn returns JWT when credentials are valid`          | `passwordEncoder.matches()` returns `true`; `jwtTokenService.generateToken()` returns `"jwt-token"`; `StepVerifier` receives `"jwt-token"` |
| `signIn returns empty Mono when password does not match` | `passwordEncoder.matches()` returns `false`; `StepVerifier` verifies complete without emission                                             |
| `signIn returns empty Mono when user is not found`       | `userDetailsService.findEdukateUserDetailsByUsername()` returns `Mono.empty()`; verifies complete without emission                         |

#### `signUp`

| Method                                                              | What it tests                                                                                                |
|---------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `signUp returns JWT when username is available and user is created` | `isNotUserPresent()` returns `Mono.just(true)`; `userDetailsService.create()` returns user; JWT returned     |
| `signUp emits CONFLICT error when username already exists`          | `isNotUserPresent()` returns `Mono.just(false)`; `StepVerifier` receives `ResponseStatusException(CONFLICT)` |
| `signUp encodes password before persisting`                         | `verify(exactly = 1) { passwordEncoder.encode(any()) }`                                                      |

---

### `BackendServiceTest`

No Spring context. Inject a `MockWebServer` (OkHttp) or use `WebClient.Builder` with `ExchangeFunction` mock.

#### `saveUser`

| Method                                                                        | What it tests                                       |
|-------------------------------------------------------------------------------|-----------------------------------------------------|
| `saveUser sends POST to internal users endpoint and returns UserCredentials`  | Response body maps correctly to `UserCredentials`   |
| `saveUser propagates WebClientResponseException on 5xx`                       | Backend returns 500 → `StepVerifier` receives error |

#### `getUserByName`

| Method                                                                   | What it tests                           |
|--------------------------------------------------------------------------|-----------------------------------------|
| `getUserByName sends GET to by-name endpoint with correct path variable` | Response body maps to `UserCredentials` |
| `getUserByName returns empty Mono when backend returns 404`              | 404 response → `Mono.empty()`           |

#### `getUserById`

| Method                                                               | What it tests                           |
|----------------------------------------------------------------------|-----------------------------------------|
| `getUserById sends GET to by-id endpoint with correct path variable` | Response body maps to `UserCredentials` |
| `getUserById returns empty Mono when backend returns 404`            | 404 response → `Mono.empty()`           |

---

### `UserDetailsServiceTest`

No Spring context. Construct `UserDetailsService` directly with a `mockk()` `BackendService` and `PasswordEncoder`.

| Method                                                                               | What it tests                                                                                                    |
|--------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `findByUsername delegates to BackendService and returns UserDetails`                 | `backendService.getUserByName("alice")` called; result is a `UserDetails` with matching username                 |
| `findEdukateUserDetailsByUsername returns EdukateUserDetails with all fields mapped` | `id`, `roles`, `status`, `token` all correctly populated from `UserCredentials`                                  |
| `findById maps UserCredentials to EdukateUserDetails`                                | `backendService.getUserById("user-id-1")` called; fields mapped correctly                                        |
| `isNotUserPresent returns true when getUserByName emits empty`                       | `backendService.getUserByName()` returns `Mono.empty()` → `Mono.just(true)`                                      |
| `isNotUserPresent returns false when user exists`                                    | `backendService.getUserByName()` returns `Mono.just(credentials)` → `Mono.just(false)`                           |
| `create calls BackendService saveUser with new user credentials`                     | `backendService.saveUser()` called exactly once; returned `EdukateUserDetails` has correct `id`                  |

---

### `JwtAuthenticationFilterTest`

No Spring context. Build `MockServerWebExchange` from `MockServerHttpRequest`; assert mutations to the exchange's
request headers and security context.

| Method                                                                           | What it tests                                                                                                                                                                                                      |
|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `filter adds user headers and security context when valid JWT present in cookie` | `authCookieService.ejectToken()` returns `"jwt"`; `jwtTokenService.parseToken()` returns `EdukateUserDetails`; `userDetailsService.findById()` returns full user; forwarded headers are present on mutated request |
| `filter calls chain without modification when X-Auth cookie is absent`           | `authCookieService.ejectToken()` returns `Mono.empty()`; chain is called with original exchange; no user headers added                                                                                             |
| `filter calls chain without modification when JWT is invalid`                    | `jwtTokenService.parseToken()` returns `null`; chain called with original exchange                                                                                                                                 |
| `filter calls chain without modification when user not found in backend`         | `userDetailsService.findById()` returns `Mono.empty()`; chain called with original exchange                                                                                                                        |

---

## Integration Tests

### `AuthControllerTest`

```kotlin
// @ActiveProfiles("test") is required: application.yml sets spring.profiles.default=prod,secure,
// which activates "secure" and prevents NoopWebSecurityConfig (@Profile("!secure")) from loading.
// "test" profile overrides the defaults so NoopWebSecurityConfig is loaded correctly.
// JwtAuthenticationFilter is a @Component WebFilter loaded by @WebFluxTest — mock it to pass through.
@WebFluxTest(AuthController::class)
@Import(NoopWebSecurityConfig::class)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient
    @MockkBean
    lateinit var authService: AuthService
    @MockkBean
    lateinit var authCookieService: AuthCookieService
    @MockkBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
}
```

#### POST /api/v1/auth/sign-in

| Method                                                                | What it tests                                                                                                                                         |
|-----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `signIn returns 204 and Set-Cookie header when credentials are valid` | `authService.signIn()` → `Mono.just("jwt")`; `authCookieService.respondWithToken("jwt")` → 204 response with `Set-Cookie`; status 204, header present |
| `signIn returns 403 when authService returns empty Mono`              | `authService.signIn()` → `Mono.empty()`; status 403                                                                                                   |
| `signIn returns 400 when username is blank`                           | Request body `{"username":"","password":"secret"}`; status 400                                                                                        |
| `signIn returns 400 when password is blank`                           | Request body `{"username":"alice","password":""}`; status 400                                                                                         |
| `signIn returns 400 when body is missing`                             | No request body; status 400                                                                                                                           |

#### POST /api/v1/auth/sign-up

| Method                                                                | What it tests                                                                        |
|-----------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `signUp returns 204 and Set-Cookie header on successful registration` | `authService.signUp()` → `Mono.just("jwt")`; cookie header present; status 204       |
| `signUp returns 403 when authService returns empty Mono`              | `authService.signUp()` → `Mono.empty()`; status 403                                  |
| `signUp returns 409 when username already exists`                     | `authService.signUp()` → `Mono.error(ResponseStatusException(CONFLICT))`; status 409 |
| `signUp returns 400 when username is blank`                           | `{"username":"","password":"p","email":"a@b.com"}`; status 400                       |
| `signUp returns 400 when email is invalid`                            | `{"username":"alice","password":"p","email":"not-an-email"}`; status 400             |
| `signUp returns 400 when body is missing`                             | No request body; status 400                                                          |

#### POST /api/v1/auth/sign-out

| Method                                                     | What it tests                                                                                         |
|------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `signOut returns 204 and clears cookie with Max-Age=0`     | `authCookieService.respondWithExpiredToken()` → 204 with `Set-Cookie: X-Auth=; Max-Age=0`; status 204 |
| `signOut returns 204 when no cookie is present in request` | No `X-Auth` cookie; `authCookieService.respondWithExpiredToken()` still called once; status 204       |

---

### `WebSecurityConfigTest`

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev", "secure")
class WebSecurityConfigTest {
    @Autowired
    lateinit var webTestClient: WebTestClient
}
```

| Method                                                                | What it tests                                                                                                                                                                              |
|-----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `sign-in endpoint is accessible without authentication`               | `POST /api/v1/auth/sign-in` with no cookie → not 401 (may be 400 for missing body, but not 401)                                                                                            |
| `sign-up endpoint is accessible without authentication`               | `POST /api/v1/auth/sign-up` with no cookie → not 401                                                                                                                                       |
| `protected API endpoint returns 401 without token`                    | `GET /api/v1/some-protected-path` → 401                                                                                                                                                    |
| `internal endpoints are not accessible from outside`                  | `GET /internal/users` → 404 (not 403): `/internal/**` is in `PublicEndpoints.asMatcher()` so `@Order(1)` `permitAll()` fires before `@Order(2)` `denyAll()`; no gateway route exists → 404 |
| `cross-origin request to public endpoint is not rejected by security` | `POST /api/v1/auth/sign-in` with `Origin: http://localhost:3000` header → response is not 401                                                                                              |

---

## Coverage Targets

| Class                     | Target                                      |
|---------------------------|---------------------------------------------|
| `GatewayFixtures`         | N/A — test helper                           |
| `AuthController`          | 100% (all 3 endpoints, all status codes)    |
| `AuthService`             | 90%+                                        |
| `BackendService`          | 90%+                                        |
| `UserDetailsService`      | 90%+                                        |
| `JwtAuthenticationFilter` | 90%+                                        |
| `WebSecurityConfig`       | All filter chains and CORS config exercised |
| `GatewayProperties`       | Covered implicitly by context tests         |
