# Tech Debt & Improvement Notes — edukate-storage

---

## Correctness

### 1. `delete()` swallows all errors silently

**File:** `AbstractStorage.kt`

```kotlin
// fixme: need something better than this
.onErrorResume { Mono.just(false) }
```

Any S3 error — transient network failure, permission denied, bucket not found — returns `false`
without logging. The caller cannot distinguish "file did not exist" from "S3 is down". At minimum,
log the error before returning false. For non-404 errors, consider re-throwing.

### 2. `move()` is not atomic

**File:** `AbstractStorage.kt`

`move()` is implemented as copy-then-delete. If the delete step fails, both the source and the
copy exist in S3. If the copy fails mid-stream, a partial object may be left. There is no
compensation or cleanup logic. Document this limitation and consider adding a cleanup step on copy
failure.

### 3. `deleteAll()` reports partial failures as total failure

**File:** `AbstractStorage.kt`

`!response.hasErrors()` returns `false` for any error, even if only one out of a hundred objects
failed. The caller has no way to know which objects were deleted and which were not. Consider
returning a result type that distinguishes partial and total failures.

### 4. `FileKey.equals()` does not include `fileName`

**File:** `ProblemFileKey.kt`, `ResultFileKey.kt`, `TempFileKey.kt`

All three implement `equals()` based only on the ID field, ignoring `fileName`. Two keys with the
same ID but different filenames are considered equal, which breaks `Set`/`Map` semantics when
storing multiple files per entity. `SubmissionFileKey` does this correctly; align the others.

### 5. `S3Presigner` and `S3AsyncClient` are never closed

**File:** `configs/S3Config.kt`

Both beans are created but neither has a `@PreDestroy` or `DisposableBean` teardown. Resources
are leaked on application shutdown. Add `@PreDestroy fun close()` to the config class.

---

## Design

### 6. Content size is materialized before upload

**File:** `Storage.kt`

The default `upload(key, contentType, content)` overload collects the full `Flux<ByteBuffer>` into
memory via `buffers.sumOf { it.remaining() }` to compute size. For large files this causes an OOM.
Callers with a known size should always use the explicit `upload(key, length, contentType, content)`
overload; the default overload should document this limitation.

### 7. `of()` path parsing is fragile

**File:** `FileKey.kt`

Path segments are parsed by splitting on `/` and matching on segment count and prefix. A
single-char prefix query would match paths up to arbitrary depth. Adding a new key subclass with
overlapping segment counts would silently mismatch existing keys. Add explicit format validation
and document the expected path formats in `FileKey`.

---

## Observability

### 8. No metrics on storage operations

Upload size, download count, delete rate, and presigned URL generation are uninstrumented. Add
Micrometer timers/counters on `upload`, `getContent`, `delete`, and `generatePresignedUrl`.

### 9. Minimal logging on object operations

`AbstractReadOnlyStorage` logs content type at debug. `AbstractStorage` logs nothing on upload or
move. Structured log entries (key, size, operation, duration) would help debug S3 issues in
production.

---

## Nice-to-Have

- **Retry on transient S3 errors**: network blips cause permanent failures; add `.retry()` with
  exponential backoff on `getContent` and `upload`
- **In-memory test double**: no `Storage` stub is provided for unit tests; every test that touches
  storage needs to mock the interface or spin up a real/testcontainer MinIO instance
