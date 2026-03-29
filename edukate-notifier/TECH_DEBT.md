# Tech Debt & Improvement Notes — edukate-notifier

---

## Correctness

### 1. RabbitMQ listener errors are silently swallowed

**File:** `services/NotificationListener.kt`

```kotlin
// TODO: avoid manual subscribe() in listener; wire explicit reactive lifecycle/error handling instead.
notificationService.saveIfAbsent(createRequest).subscribe()
```

`subscribe()` with no `onError` handler discards all exceptions. If `saveIfAbsent` fails (DB
timeout, validation error, etc.), the message is ACKed by RabbitMQ and permanently lost. Fix:
use `.subscribe(onNext = {}, onError = { log.error("...", it) })` at minimum, or restructure to
return a blocking result so RabbitMQ sees a NACK on failure.

### 2. `saveIfAbsent` has a race condition

**File:** `services/NotificationService.kt`

```kotlin
// TODO: this find-then-save flow is race-prone under concurrent same-UUID writes
```

Two concurrent deliveries of the same UUID can both pass the `findByUuid` check and both attempt
a save. The second save hits a MongoDB duplicate-key error, which is currently unhandled. Replace
with an atomic upsert (`findOneAndUpdate` with `upsert=true`) or a `try { save } catch
(DuplicateKeyException) { /* idempotent — already saved */ }` pattern.

### 3. `createdAt` null check happens too late

**File:** `entities/BaseNotification.kt`

`requireNotNull(createdAt)` is called inside `toDto()`. If MongoDB auditing fails to set
`@CreatedDate`, the error surfaces at DTO conversion time — far from the root cause. The field
should be `val createdAt: Instant` (non-nullable) since `@CreatedDate` guarantees it after
insertion. The nullable declaration is a workaround for the Spring Data lifecycle; document why
it is nullable if this cannot be changed.

---

## Reliability

### 4. Pagination size not server-side enforced

**File:** `controllers/NotificationController.kt`

The OpenAPI schema documents a max size of 100 but there is no `@Max(100)` annotation or
server-side check. A client can request `size=1000000` and retrieve the entire collection in one
call.

### 5. Notifications never expire

**File:** `entities/BaseNotification.kt`

There is no TTL index on the notifications collection. Notifications accumulate indefinitely.
Add a MongoDB TTL index (e.g. 90-day retention) or a configurable cleanup job.

---

## Design

### 6. Parallel sealed hierarchies with duplicated Jackson annotations

`edukate-common` has `BaseNotificationCreateRequest` with `@JsonSubTypes`; `edukate-notifier` has
`BaseNotificationDto` with the same annotations. Both hierarchies must be kept in sync manually.
Consider a shared DTO or a converter interface that maps one to the other without duplicating
Jackson polymorphism configuration.

### 7. `markAsRead()` is verbose and fragile for new subtypes

**File:** `entities/BaseNotification.kt`

`markAsRead()` uses a `when` expression with explicit `copy()` per subclass. Adding a new
notification type requires updating this method. A better pattern: make `markAsRead()` abstract in
the sealed class, letting each subclass handle its own copy.

---

## Observability

### 8. No metrics

No counters for notifications saved, delivered, or read. No timer on listener processing. No
queue-depth monitoring. Add Micrometer instrumentation to `NotificationListener` and
`NotificationService`.

### 9. No `@LastModifiedDate` on notifications

Marking a notification as read does not update any timestamp. Cannot determine when a notification
was read or audit the timeline.

---

## Nice-to-Have

- **Delete endpoint**: users cannot delete individual notifications, only mark them as read
- **Filtering by type and date range**: the controller only filters by read/unread; no type or
  date range filter is exposed
- **Server-Sent Events for live delivery**: the frontend polls for new notifications; SSE or
  WebSocket would allow push delivery without polling
- **Dead Letter Queue for the notify queue**: failed messages have no fallback and are lost
