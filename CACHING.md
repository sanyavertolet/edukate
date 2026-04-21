# Caching Guide

Caching strategy for the Edukate platform. Provider: **Caffeine** (in-process, single-instance — no horizontal
scaling planned).

---

## Performance context

Two confirmed hotspots that caching directly addresses:

- **`/problems` is slow** — every request runs a complex MongoDB `$lookup` aggregation across `problems` +
  `problem_status`, then calls MinIO once per image to generate presigned URLs.
- **Auth is ~10 s** — every authenticated request triggers: JWT filter → `UserDetailsService.findByUsername()`
  → HTTP call from gateway to backend → `UserRepository.findByName()` (MongoDB). This is a full network round-trip
  on the hot path of every single request.

---

## Provider: Caffeine

Caffeine stores cached values in the **JVM heap** of the pod that owns the cache. This means:

- Zero network latency — lookups are nanoseconds
- No external infrastructure needed
- Cache is local per pod — safe for single-instance deployments; would require Redis for multi-pod

### Reactive requirement

Spring Boot's `@Cacheable` must be configured in async mode for `Mono`-returning methods. Without
`setAsyncCacheMode(true)`, Spring caches the `Mono` object itself rather than the resolved value — a silent bug
that is hard to detect.

### Memory overhead

Cluster specs: **8 GiB RAM, 2 nodes**. Current workloads consume approximately:

| Workload                       | Memory limit                 |
|--------------------------------|------------------------------|
| backend                        | 512 Mi                       |
| gateway                        | 512 Mi                       |
| notifier                       | 512 Mi                       |
| checker                        | 1024 Mi                      |
| frontend                       | ~64 Mi                       |
| Prometheus                     | 1024 Mi                      |
| Grafana                        | 512 Mi                       |
| Loki                           | 512 Mi                       |
| Tempo                          | 512 Mi                       |
| Promtail (× 2 nodes)           | ~128 Mi                      |
| kube-prometheus-stack operator | ~100 Mi                      |
| MongoDB (no limit)             | ~300 Mi                      |
| MinIO (no limit)               | ~128 Mi                      |
| RabbitMQ (no limit)            | ~200 Mi                      |
| Kubernetes system (2 nodes)    | ~750 Mi                      |
| **Total**                      | **~5.8 GiB (~73% of 8 GiB)** |

Backend heap: 512 Mi limit → Paketo sets ~200–280 Mi heap → 75% in use → ~50–80 Mi free.

Projected Caffeine overhead at the proposed `maximumSize` limits:

| Cache | Size estimate |
|---|---|
| `problems` (500 × ~2 KB) | ~1 MB |
| `users-by-id` + `users-by-name` (500 × ~500 B each) | ~500 KB |
| `bundles` (200 × ~1 KB) | ~200 KB |
| `presigned-urls` (1000 × ~200 B) | ~200 KB |
| `user-details` in gateway (500 × ~1 KB) | ~500 KB |
| **Total across both pods** | **~2.5 MB** |

This is negligible. The risk is not the data size — it is **unbounded growth if `maximumSize` is omitted**.
Every cache in this project must declare both `expireAfterWrite` (TTL) and `maximumSize`. Monitor JVM heap
metrics in Grafana (JVM dashboard) after enabling caches. If heap climbs above 85%, reduce `maximumSize` or
increase the pod memory limit.

---

## Entity analysis

### `Problem` — ✅ Cache

**Why:** Problems are read on every page of `/problems`, on every submission context build (checker fetches
problem images and text), and on every DTO preparation inside `BundleService`. They are rarely modified — only
by admins uploading content.

**What to cache:**

| Method | Cache | Key | TTL | Max |
|---|---|---|---|---|
| `ProblemService.findProblemById(id)` | `problems` | `#id` | 24 h | 500 |

**What NOT to cache:**

- `findProblemsByIds(ids)` — list as a cache key is unsafe (order-sensitive, difficult to evict partially);
  let it warm naturally through `findProblemById` calls.
- `getFilteredProblems(filter, auth)` — per-user result that depends on `UserProblemStatus`, which changes on
  every submission. Caching this would show a user a stale "solved/failed" state.

**Evict on:** `updateProblem(problem)` (key: `problem.id`), `deleteProblemById(id)` (key: `id`)

---

### `User` — ✅ Cache

**Why:** `findUserName(userId)` is called on every `SubmissionDto` preparation and on every entry in every
bundle user list. It is currently a MongoDB round-trip each time. User profiles are effectively immutable
during a session — name and roles rarely change.

**What to cache:**

| Method | Cache | Key | TTL | Max |
|---|---|---|---|---|
| `UserService.findUserById(userId)` | `users-by-id` | `#userId` | 10 min | 500 |
| `UserService.findUserByName(name)` | `users-by-name` | `#name` | 10 min | 500 |

`findUserName(userId)` delegates to `findUserById` internally, so it benefits automatically.

**Evict on:** `saveUser(user)` (evict both caches by `user.id` and `user.name`),
`deleteUserById(id)` (evict `users-by-id` by id; evict `users-by-name` requires looking up the name first, or
use `allEntries = true` on a targeted eviction)

---

### `Bundle` — ⚠️ Cache with care

**Why:** Bundles are fetched on nearly every bundle endpoint for permission evaluation
(`BundlePermissionEvaluator.hasRole`). However, they are mutable: members join, leave, get invited,
change roles.

**What to cache:**

| Method | Cache | Key | TTL | Max |
|---|---|---|---|---|
| `BundleService.findBundleByShareCode(shareCode)` | `bundles` | `#shareCode` | 5 min | 200 |

Short TTL (5 min) acts as a safety net even if an eviction is accidentally missed.

**Evict on every mutation** (all operate by `shareCode`):
`joinUser`, `removeUser`, `inviteUser`, `expireInvite`, `changeUserRole`, `changeVisibility`, `changeProblems`

**What NOT to cache:**

- `prepareDto` / `prepareMetadata` — these call `UserService` and `ProblemService`, whose results are already
  cached. Double-caching the assembled DTO creates a second stale-risk layer with no additional benefit.

---

### `Submission` — ❌ Do not cache

**Why:** Submission status transitions (PENDING → ACCEPTED / REJECTED) must be immediately consistent.
Users poll for their submission result; caching would serve a stale PENDING status after the checker
has already written the final verdict. Write rate is also high.

---

### `CheckResult` — ❌ Do not cache

**Why:** Append-only (never mutated once written), so no stale-write risk. However, read frequency is low —
check results are only fetched when a user opens a specific submission detail view. The collection is indexed
on `submissionId`; the query is already fast. The complexity of adding cache eviction logic outweighs any gain.

---

### `UserProblemStatus` — ❌ Do not cache

**Why:** Updated on every submission. It drives the MongoDB `$lookup` aggregation in
`ProblemService.getFilteredProblems` that shows which problems a user has solved or failed. Caching this
would immediately stale the problem list after any submission.

---

## Gateway: UserDetails cache (fixes the 10 s auth hotspot)

Every authenticated HTTP request currently triggers this chain synchronously:

```
JWT filter
  → UserDetailsService.findByUsername(username)
    → BackendService.getUserByName(name)      ← HTTP call to edukate-backend
      → UserRepository.findByName()           ← MongoDB query
```

After the first successful login, the user's credentials do not change on a per-request basis. Caching
`EdukateUserDetails` in the gateway eliminates the HTTP + DB round-trip entirely for subsequent requests.

**What to cache** (in `edukate-gateway`):

| Method | Cache | Key | TTL | Max |
|---|---|---|---|---|
| `UserDetailsService.findEdukateUserDetailsByUsername(username)` | `user-details` | `#username` | 5 min | 500 |
| `UserDetailsService.findById(id)` | `user-details` | `#id` | 5 min | 500 |

TTL of 5 min is short enough to pick up role changes within a session without manual eviction.

**Evict on:** `UserDetailsService.create(username, ...)` — new user registration (key: `username`)

**Not needed:** `updatePassword` is a no-op stub that simply returns the user unchanged.

---

## Presigned URLs (fixes `/problems` image latency)

`FileManager.getPresignedUrl(key)` is called for every file object in:

- `SubmissionService.collectFileUrls` — N files per submission DTO
- `ResultService.updateImagesInResult` — result images
- `ProblemService.problemImageDownloadUrls` — problem images on every `/problems` page load

Each call hits MinIO to cryptographically sign a URL. S3 signature duration is configured at **1 hour**
(`s3.signature-duration=1h`).

Cache presigned URLs with a TTL of **30 minutes** — half the signature lifetime — so cached URLs are
always valid when served:

| Method | Cache | Key | TTL | Max |
|---|---|---|---|---|
| `FileManager.getPresignedUrl(key)` | `presigned-urls` | `#key.toString()` | 30 min | 1000 |

**Evict on:** `FileManager.deleteFile(key)` (key: `key.toString()`),
`FileManager.moveFile(oldKey, newKey)` (evict `oldKey.toString()`)

---

## Quick-reference table

| Cache name | Location | Key | TTL | Max entries | Evict on |
|---|---|---|---|---|---|
| `problems` | backend | `#id` | 24 h | 500 | updateProblem, deleteProblemById |
| `users-by-id` | backend | `#userId` | 10 min | 500 | saveUser, deleteUserById |
| `users-by-name` | backend | `#name` | 10 min | 500 | saveUser, deleteUserById |
| `bundles` | backend | `#shareCode` | 5 min | 200 | all Bundle mutation methods |
| `presigned-urls` | backend | `#key.toString()` | 30 min | 1000 | deleteFile, moveFile |
| `user-details` | gateway | `#username` / `#id` | 5 min | 500 | create |

---

## Implementation checklist

**Dependencies** — add to `gradle/libs.versions.toml`:
```toml
spring-boot-starter-cache = { module = "org.springframework.boot:spring-boot-starter-cache" }
caffeine = { module = "com.github.ben-manes.caffeine:caffeine" }
```

Add to `edukate-backend/build.gradle.kts` and `edukate-gateway/build.gradle.kts`:
```kotlin
implementation(libs.spring.boot.starter.cache)
implementation(libs.caffeine)
```

**Application class** — add `@EnableCaching` to `EdukateBackendApplication` and `EdukateGatewayApplication`.

**`CaffeineCacheManager` bean** — declare in each app's config, with `setAsyncCacheMode(true)` and
per-cache specs:
```kotlin
@Bean
fun cacheManager(): CacheManager =
    CaffeineCacheManager().apply {
        setAsyncCacheMode(true)
        setCacheSpecification("maximumSize=500,expireAfterWrite=10m")  // default
        setCaffeineSpec(CaffeineSpec.parse("maximumSize=500,expireAfterWrite=10m"))
    }
```

For per-cache TTL overrides, register individual named caches via `registerCustomCache(name, cache)`.

**Annotations** — use `@CacheConfig(cacheNames = [...])` at class level to avoid repeating the cache name.
Use `unless = "#result == null"` on `@Cacheable` to avoid storing empty Mono completions.
Annotate service methods only — never repository methods.

**Monitoring** — the JVM Micrometer dashboard in Grafana shows heap usage. After deploying, confirm heap does
not trend upward. If it does, reduce `maximumSize` values or increase the pod memory limit in
`edukate-chart/values-prod.yaml`.
