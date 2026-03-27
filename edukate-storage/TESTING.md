# edukate-storage — Testing Plan

## Testability Assessment

| Class | Unit-testable? | Notes |
|---|---|---|
| `FileKey` + subclasses | ✅ Yes | Pure logic — path parsing, formatting, equality, Jackson |
| `ReadOnlyStorage` / `Storage` | N/A | Interfaces only |
| `S3Properties` | N/A | Spring configuration binding — tested at app startup |
| `S3Config` | N/A | `@Bean` wiring — tested by Spring context |
| `AbstractReadOnlyStorage` | ⚠️ Complex | Requires mocking `S3AsyncClient`; see note below |
| `AbstractStorage` | ⚠️ Complex | Same as above |

**`AbstractReadOnlyStorage` / `AbstractStorage` note:** These classes wrap AWS SDK
`CompletableFuture` calls. Testing them with `mockk` is feasible but produces fragile
tests tied to SDK builder chains. Recommended approach: integration tests against a
local MinIO instance via Testcontainers. This is out of scope for the initial test suite
and can be added when the module needs regression coverage for storage behaviour.

---

## Unit Test Suite — `FileKey` Hierarchy

### Test file

```
src/test/kotlin/io/github/sanyavertolet/edukate/storage/keys/FileKeyTest.kt
```

**Status: ✅ Implemented** — 8 tests, all passing.

No Spring context needed — all pure unit tests using JUnit 5 and AssertJ.
Jackson serialization tests use `ObjectMapper().registerKotlinModule()` directly.

> Note: `@JsonIgnoreProperties(ignoreUnknown = true)` was added to `FileKey` so that the
> `_type` discriminator field (kept `visible = true` for runtime access) is silently ignored
> when Jackson maps JSON properties back onto the concrete subclass constructor.

---

### Region 1 — `FileKey.of()` — valid paths

Each test constructs a key via `FileKey.of(rawPath)` and asserts the correct subtype
and field values.

| Test | Input | Expected type | Expected fields |
|---|---|---|---|
| TempFileKey path | `"users/u1/tmp/file.png"` | `TempFileKey` | `userId="u1"`, `fileName="file.png"` |
| SubmissionFileKey path | `"users/u1/submissions/p1/s1/img.png"` | `SubmissionFileKey` | `userId`, `problemId`, `submissionId`, `fileName` all set |
| ProblemFileKey path | `"problems/p1/diagram.png"` | `ProblemFileKey` | `problemId="p1"`, `fileName="diagram.png"` |
| ResultFileKey path | `"results/p1/result.json"` | `ResultFileKey` | `problemId="p1"`, `fileName="result.json"` |
| Leading slash | `"/users/u1/tmp/f.png"` | `TempFileKey` | normalised correctly |
| Double slashes | `"users//u1//tmp//f.png"` | `TempFileKey` | normalised correctly |

---

### Region 2 — `FileKey.of()` — invalid inputs

All cases must throw `IllegalArgumentException`.

| Test | Input | Reason |
|---|---|---|
| Null | `null` | null check |
| Blank | `"   "` | blank check |
| Too few segments | `"users/u1"` | no matching pattern |
| Unknown prefix | `"uploads/u1/file.png"` | no matching pattern |
| Wrong segment count for tmp | `"users/u1/tmp"` | 3 segments, not 4 |

---

### Region 3 — `toString()` path formatting

Each key subclass must produce the correct S3 object key string.

| Key | Expected `toString()` |
|---|---|
| `TempFileKey("u1", "f.png")` | `"users/u1/tmp/f.png"` |
| `ProblemFileKey("p1", "d.png")` | `"problems/p1/d.png"` |
| `ResultFileKey("p1", "r.json")` | `"results/p1/r.json"` |
| `SubmissionFileKey("u1", "p1", "s1", "i.png")` | `"users/u1/submissions/p1/s1/i.png"` |

Also test the round-trip: `FileKey.of(key.toString())` must return an equal key.

---

### Region 4 — `prefix()` path formatting

Each companion's `prefix()` method must produce the path prefix used for S3 listing.

| Call | Expected result |
|---|---|
| `TempFileKey.prefix("u1")` | `"users/u1/tmp/"` |
| `ProblemFileKey.prefix("p1")` | `"problems/p1/"` |
| `ResultFileKey.prefix("p1")` | `"results/p1/"` |
| `SubmissionFileKey.prefix("u1", "p1", "s1")` | `"users/u1/submissions/p1/s1/"` |

---

### Region 5 — `FileKey.typeOf()` and `FileKey.ownerOf()`

| Input | `typeOf` | `ownerOf` |
|---|---|---|
| `TempFileKey("u1", "f")` | `"tmp"` | `"u1"` |
| `SubmissionFileKey("u1", ...)` | `"submission"` | `"u1"` |
| `ProblemFileKey("p1", "f")` | `"problem"` | `null` |
| `ResultFileKey("p1", "f")` | `"result"` | `null` |

---

### Region 6 — `equals` and `hashCode`

Tests document the intended equality semantics (identity fields only, `fileName` excluded).

| Test | Assertion |
|---|---|
| Two `ProblemFileKey`s same `problemId`, different `fileName` | equal |
| Two `TempFileKey`s same `userId`, different `fileName` | equal |
| Two `SubmissionFileKey`s same `userId/problemId/submissionId`, different `fileName` | equal |
| Two `ProblemFileKey`s different `problemId` | not equal |
| `hashCode` is consistent with `equals` | `a == b` implies `a.hashCode() == b.hashCode()` |

---

### Region 7 — Jackson polymorphic serialization

Uses `ObjectMapper().registerKotlinModule()`. No Spring context needed.

| Test | Assertion |
|---|---|
| `TempFileKey` serializes | JSON contains `"_type":"tmp"` |
| `ProblemFileKey` serializes | JSON contains `"_type":"problem"` |
| `ResultFileKey` serializes | JSON contains `"_type":"result"` |
| `SubmissionFileKey` serializes | JSON contains `"_type":"submission"` |
| Each subtype round-trips via `FileKey` | `objectMapper.readValue(json, FileKey::class.java)` produces equal key |

---

## Running the Tests

```bash
# Format first
./gradlew :edukate-storage:ktfmtFormat

# Run tests
./gradlew :edukate-storage:test

# Static analysis
./gradlew :edukate-storage:detekt
```
