# Tech Debt & Improvement Notes — edukate-checker

Observations made during the Java → Kotlin migration. These are not blockers but are worth addressing in follow-up work.

---

## Design Issues

### 1. `MediaContentResolver` — S3 metadata fetched separately from content

**File:** `services/MediaContentResolver.kt:23`

```kotlin
// fixme: storage should return metadata from ReadOnlyStorage#getContent
private fun resolveMedia(rawKey: String): Mono<Media> =
    toByteArray(storage.getContent(rawKey)).zipWith(storage.metadata(rawKey), ::mediaWithByteArray)
```

Each resolved media key causes **two** S3 calls: one for `getContent` and one for `metadata` (HEAD request).
The `AbstractReadOnlyStorage.getContent` already receives an S3 `GetObjectResponse` that contains the content type —
but the current `ReadOnlyStorage` interface doesn't expose it. Fixing this requires changing the `ReadOnlyStorage`
interface in `edukate-storage` to return content together with its metadata, which is a cross-module change.

---

## Reactive Patterns

### 2. `SubmissionContextListener` — `.block()` on a reactive chain

**File:** `services/SubmissionContextListener.kt:26`

```kotlin
// RabbitMQ listener is synchronous; .block() bridges the reactive chain to the blocking listener thread
.block()
```

`.block()` is intentional and necessary here — Spring's `@RabbitListener` is a blocking callback. However, this means
the checker's RabbitMQ thread pool is the throughput ceiling: one message is fully processed before the next is picked
up. A potential improvement is to use `@RabbitListener` with a manual acknowledgement and a bounded concurrency
mechanism (e.g., `Schedulers.newBoundedElastic`), but this would complicate error handling and is likely fine at current load.

---

## Observability

### 3. `CheckerService` — no metrics or tracing around AI calls

**File:** `services/CheckerService.kt`

The AI call (`chatService.makeRequest`) has no timing instrumentation. Given that OpenAI calls can take seconds,
adding a Micrometer timer or distributed trace span around the call would help in production diagnosis.

### 4. `RabbitResultPublisher` — silent retry with no logging

**File:** `services/impl/RabbitResultPublisher.kt`

`.retry(PUBLISH_RETRY_COUNT)` retries transient RabbitMQ failures silently. A `doOnError` between the runnable and the
retry would log each failed attempt and make debugging easier:

```kotlin
Mono.fromRunnable<Void> { ... }
    .doOnError { ex -> log.warn("Publish failed, retrying...", ex) }
    .retry(PUBLISH_RETRY_COUNT)
```

---

## Code Quality

### 5. `NoopChatService` — stub explanation is not descriptive

**File:** `services/impl/NoopChatService.kt:22`

The stub `ModelResponse` uses `"stub"` as the explanation string. A more descriptive value like
`"[NoopChatService: silent profile active]"` would make it immediately clear in logs that this is a stub response, not
a real AI output.

---

## Configuration

### 6. `PUBLISH_RETRY_COUNT` and `STUB_TRUST_LEVEL` are not configurable

**Files:** `RabbitResultPublisher.kt`, `NoopChatService.kt`

These constants are hardcoded. In production it may be useful to tune the retry count without redeploying. Consider
moving them to `application.properties` and injecting via `@Value`.
