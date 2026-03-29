# Tech Debt & Improvement Notes — edukate-backend

This module has 41 Java files pending Kotlin migration. Issues specific to the Java→Kotlin
conversion are noted separately from general design debt.

---

## Critical — Production Risk

### 1. `.block()` in a reactive RabbitMQ listener

**File:** `listeners/CheckResultMessageListener.java`

```java
.block(); // Blocks thread pool on RabbitMQ listener
```

Blocking the RabbitMQ listener thread prevents concurrent message processing. Under load, the
thread pool is exhausted and no new check results are processed. Replace with `.subscribe()` using
explicit error handling, or return the reactive chain from the listener method and let Spring AMQP
handle it.

### 2. Fire-and-forget `.subscribe()` in save listener

**File:** `savelisteners/SubmissionAfterSaveListener.java`

```java
.subscribe(); // No error handling, fire-and-forget
// TODO: implement the duplicate-key retry
```

Errors are logged but the reactive chain is detached: the listener returns before the upsert
completes. If the MongoDB upsert fails, the `UserProblemStatus` collection is left inconsistent.
Use `.block()` (acceptable in a `ReactiveBeforeSaveEvent` context) or restructure to a reactive
listener that returns `Mono<Void>`.

---

## Correctness

### 3. Content-type hardcoded to `IMAGE_JPEG` for all temp uploads

**File:** `controllers/files/TempFileController.java`

```java
// TODO: detect content type???
MediaType.IMAGE_JPEG // hardcoded for all uploads
```

Every uploaded file is stored in S3 with `image/jpeg` regardless of its actual content. PDFs,
PNGs, and other types are stored with wrong metadata, causing incorrect `Content-Type` headers
when served. Parse the multipart `ContentType` header from the upload request.

### 4. Share code uniqueness is not guaranteed

**File:** `services/ShareCodeGenerator.java`

The generator produces a random 10-character code but does not check if the code already exists
in the database before returning it. Collisions are improbable but not impossible, and no retry
loop is in place.

### 5. `CheckResult.kt` hardcodes trust level for self-checks

**File:** `entities/CheckResult.kt`

```kotlin
trustLevel = 0.01f // for self-checked results
```

This magic value should be configurable or at least a named constant.

### 6. `BundlePermissionEvaluator` returns `Boolean?` instead of `Boolean`

**File:** `permissions/BundlePermissionEvaluator.java`

Spring Security expects `boolean`; returning the boxed `Boolean` creates a potential
`NullPointerException` if the method returns `null`. Change return types to primitive `boolean`.

---

## Design

### 7. `SubmissionAfterSaveListener` has too many responsibilities

**File:** `savelisteners/SubmissionAfterSaveListener.java`

The listener handles MongoDB event subscription, builds an aggregation pipeline (90+ lines),
performs an upsert, and dispatches notifications. The aggregation pipeline belongs in
`SubmissionRepository` or a dedicated `UserProblemStatusService`.

### 8. `FileManager` mixes storage and metadata concerns

**File:** `services/files/FileManager.java`

A single class handles S3 upload, S3 deletion, presigned URL generation, database metadata
persistence, and batch existence checks. Split into a `StorageService` (S3 operations) and a
`FileMetadataService` (DB operations) with clear boundaries.

### 9. `Result` entity conflates persistence and presentation

**File:** `dtos/Result.kt`

```kotlin
// TODO: will be removed when fully migrated to kotlin
// refactor Result entity to split persistent result from human-readable (with correct pics)
```

Presigned image URLs are embedded in a persistence-layer object. URLs expire; storing them in the
DB is incorrect. Separate persistent state (`rawKeys`) from computed presentation state (signed
URLs), and generate the latter at query time.

### 10. Permission evaluators rely on `UserRole` enum declaration order

**File:** `permissions/BundlePermissionEvaluator.java`, `permissions/SubmissionPermissionEvaluator.java`

Role comparison uses `.compareTo()`, which is positional. Inserting a new role between existing
ones changes implicit permission levels silently. Use an explicit `weight: Int` property on
`UserRole` or a `when` expression that names each allowed role.

---

## Observability

### 11. No metrics on core operations

No counters or timers on:
- Submission creation / rejection rate
- Check result processing latency
- File upload / download success rate
- Permission evaluator decisions

Add Micrometer instrumentation at service boundaries.

### 12. No distributed trace context in RabbitMQ messages

Check requests and results flow through RabbitMQ with no correlation ID. A submission cannot be
traced end-to-end from creation through checking to result delivery in logs.

---

## Missing Validation

### 13. Temp file upload has no size or type enforcement

**File:** `controllers/files/TempFileController.java`

No maximum file size is enforced server-side (beyond the global Spring codec limit). No MIME type
whitelist. Accepting arbitrary content at this endpoint is a risk.

### 14. `changeProblems()` does not validate problem IDs exist

**File:** `services/BundleService.java`

Non-existent problem IDs can be added to a bundle silently. Add a repository existence check
before persisting the change.

---

## Nice-to-Have

- **Pagination metadata**: controllers return `Flux<T>` with no total count — the client cannot
  know if more pages exist without fetching until empty
- **Soft deletes**: deleted problems, bundles, and submissions leave no audit trail and cannot be
  recovered
- **Filtering and sorting on problem list**: currently only sorted by semver; no tag, status, or
  text filter
- **Caching for frequently-read problems**: every API request fetches images and prepares the DTO
  from scratch; a short-lived cache would reduce S3 and DB pressure
