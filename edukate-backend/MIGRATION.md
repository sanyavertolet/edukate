# edukate-backend: Java → Kotlin Migration Plan

## Overview

The backend is a mixed Java/Kotlin project. Entities and DTOs are already in Kotlin. This plan covers the migration of
39 remaining Java files to Kotlin across configs, repositories, permissions, save listeners, message listeners,
services, and controllers.

**Already in Kotlin** (no action needed):

- `entities/` — all 8 files
- `dtos/` — all 12 files

**To migrate** — 39 Java files across 9 packages.

---

## Migration Order

Migrate bottom-up: leaf dependencies first, then consumers of those. This ensures each layer compiles cleanly before its
dependents are touched.

```
Phase 1 — Utilities & infrastructure (no Spring deps, pure logic)
Phase 2 — Configs (Spring config classes with no business logic)
Phase 3 — Repositories (interfaces; thin layer over Spring Data)
Phase 4 — Permissions (pure logic helpers)
Phase 5 — Storage (thin adapter)
Phase 6 — Save Listeners (event hooks; depend on entities + utils)
Phase 7 — Services: leaf services (no inter-service dependencies)
Phase 8 — Services: dependent services (depend on Phase 7 services)
Phase 9 — Services: orchestrators (depend on Phase 8)
Phase 10 — Message Listener (depends on services + messaging)
Phase 11 — Controllers: internal (simpler; depend only on services)
Phase 12 — Controllers: public-facing (complex auth + permission logic)
Phase 13 — Application entry point
```

---

## Detailed Phase Breakdown

### Phase 1 — Utilities

| File                     | Notes                                                                                                                                                                                                |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `utils/SemVerUtils.java` | Replace `@UtilityClass` with a Kotlin `object`. Static fields become `const val`. `Tuple3` return can be replaced with a simple Kotlin `Triple` or a local data class `SemVer(major, minor, patch)`. |
| `utils/Sorts.java`       | Replace `@UtilityClass` with `object`. One-liner method maps naturally to a Kotlin top-level function.                                                                                               |

### Phase 2 — Configs

| File                        | Notes                                                                             |
|-----------------------------|-----------------------------------------------------------------------------------|
| `configs/MongoConfig.java`  | Empty class with annotations. Becomes a `@Configuration` Kotlin class, one-liner. |
| `configs/RabbitConfig.java` | Queue/exchange/binding bean declarations. Straightforward conversion.             |

### Phase 3 — Repositories

All are Java interfaces extending `ReactiveMongoRepository`. In Kotlin these become `interface` with the same signature;
`@Query` annotations carry over unchanged. Remove Lombok `@NonNull`; use Kotlin's type system instead.

| File                                            | Notes                                                                         |
|-------------------------------------------------|-------------------------------------------------------------------------------|
| `repositories/ReactiveReadOnlyRepository.java`  | Simple generic interface. Kotlin interface with type parameters.              |
| `repositories/UserRepository.java`              | Two derived query methods. Trivial.                                           |
| `repositories/ProblemRepository.java`           | Has `@Aggregation` annotations with pipeline JSON strings — preserve exactly. |
| `repositories/BundleRepository.java`            | Has `@Query` with `$elemMatch` — preserve JSON strings.                       |
| `repositories/SubmissionRepository.java`        | Derived queries + pageable. Trivial.                                          |
| `repositories/CheckResultRepository.java`       | One `@Query` method. Trivial.                                                 |
| `repositories/UserProblemStatusRepository.java` | Extends `ReactiveReadOnlyRepository`. Trivial.                                |
| `repositories/FileObjectRepository.java`        | Includes `deleteByKeyPath` returning `Mono<Long>` — keep return type.         |

### Phase 4 — Permissions

| File                                             | Notes                                                                                                                                                                                      |
|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `permissions/BundlePermissionEvaluator.java`     | Convert methods to functions. `Boolean` return type becomes `Boolean` (non-null) in Kotlin. The `compareTo` usage works the same as `UserRole` is already Kotlin enum in `edukate-common`. |
| `permissions/SubmissionPermissionEvaluator.java` | Single method, trivial.                                                                                                                                                                    |

### Phase 5 — Storage

| File                          | Notes                                                                                                                                                             |
|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `storage/FileKeyStorage.java` | Thin subclass of `AbstractStorage<FileKey, FileObjectMetadata>` from `edukate-storage`. Verify `AbstractStorage` is Kotlin-compatible; convert to Kotlin `class`. |

### Phase 6 — Save Listeners

| File                                             | Notes                                                                                                                                                                                                                                             |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `savelisteners/ProblemBeforeSaveListener.java`   | Extend `AbstractMongoEventListener<Problem>`. The `SemVerUtils.parse()` call will use Kotlin `object` after Phase 1. Replace `Tuple3` extraction with Kotlin destructuring.                                                                       |
| `savelisteners/SubmissionAfterSaveListener.java` | Complex MongoDB aggregation pipeline built with `Document` — keep the raw BSON logic as-is but in Kotlin syntax. Replace `subscribe()` fire-and-forget with structured coroutines or `subscribeOn(Schedulers.boundedElastic())` — see Pitfall #3. |

### Phase 7 — Services: Leaf (no inter-service deps)

| File                                         | Notes                                                                                                                                |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `services/ShareCodeGenerator.java`           | `@Service` with one method. Convert to Kotlin class, use `('A'..'Z') + ('a'..'z') + ('0'..'9')` idiom.                               |
| `services/ProblemStatusDecisionManager.java` | Depends only on `UserProblemStatusRepository` and `UserProblemStatus`. Straightforward.                                              |
| `services/UserService.java`                  | Depends on `UserRepository` and `Notifier`. The null-safe `findUserName` with `.defaultIfEmpty("UNKNOWN")` maps to Kotlin naturally. |

### Phase 8 — Services: Mid-layer

| File                                        | Notes                                                                                                                                                                                                              |
|---------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `services/ResultService.java`               | Depends on `ProblemService` + `FileManager`. Straightforward.                                                                                                                                                      |
| `services/files/SubmissionFileService.java` | Depends on `FileManager`. Straightforward.                                                                                                                                                                         |
| `services/files/FileManager.java`           | Depends on `FileObjectRepository` + `FileKeyStorage`. Heavy use of `@NonNull` Lombok annotations — replace with Kotlin non-nullable types. The `saveOrUpdateByKeyPath` private method should become `private fun`. |
| `services/CheckResultService.java`          | Depends on `SubmissionService` — migrate after `SubmissionService`.                                                                                                                                                |

### Phase 9 — Services: Orchestrators

| File                                    | Notes                                                                                                                                                                                                                                                                                                   |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `services/ProblemService.java`          | Depends on `ProblemRepository`, `FileManager`, `ProblemStatusDecisionManager`.                                                                                                                                                                                                                          |
| `services/SubmissionService.java`       | Depends on many: `SubmissionRepository`, `FileManager`, `SubmissionFileService`, `UserService`, `FileObjectRepository`, `ProblemService`, `SubmissionPermissionEvaluator`. The `@Nullable String problemId` parameter maps to `problemId: String?` in Kotlin — use `if (problemId != null)` or `?.let`. |
| `services/BundleService.java`           | Depends on `BundleRepository`, `ShareCodeGenerator`, `ProblemService`, `BundlePermissionEvaluator`, `UserService`. The `Objects.requireNonNull(AuthUtils.id(authentication))` calls in `declineInvite` should become `requireNotNull(AuthUtils.id(authentication))`.                                    |
| `services/CheckerSchedulerService.java` | Depends on `SubmissionService`, `RabbitTemplate`.                                                                                                                                                                                                                                                       |

### Phase 10 — Message Listener

| File                                        | Notes                                                                                                |
|---------------------------------------------|------------------------------------------------------------------------------------------------------|
| `listeners/CheckResultMessageListener.java` | Depends on `CheckResultService` + `Notifier`. The `.block()` call is a known issue — see Pitfall #3. |

### Phase 11 — Internal Controllers

| File                                                  | Notes |
|-------------------------------------------------------|-------|
| `controllers/internal/ProblemInternalController.java` |       |
| `controllers/internal/CheckerInternalController.java` |       |
| `controllers/internal/ResultInternalController.java`  |       |
| `controllers/internal/UserInternalController.java`    |       |

### Phase 12 — Public Controllers

| File                                        | Notes                                                                               |
|---------------------------------------------|-------------------------------------------------------------------------------------|
| `controllers/UserController.java`           |                                                                                     |
| `controllers/ResultController.java`         |                                                                                     |
| `controllers/files/TempFileController.java` | Large multipart file streaming — verify `Flux<ByteBuffer>` / `DataBuffer` handling. |
| `controllers/CheckerController.java`        |                                                                                     |
| `controllers/SubmissionController.java`     |                                                                                     |
| `controllers/BundleController.java`         | Largest controller, most complex permission logic.                                  |
| `controllers/ProblemController.java`        |                                                                                     |

### Phase 13 — Application Entry Point

| File                             | Notes                                                                     |
|----------------------------------|---------------------------------------------------------------------------|
| `EdukateBackendApplication.java` | Becomes `fun main(args: Array<String>)` + `@SpringBootApplication` class. |

---

## Build Configuration Changes

After migrating all Java files, remove the `java` source set from `build.gradle.kts`:

```kotlin
// Remove this block entirely when all Java files are migrated:
// sourceSets { main { java { ... } } }
```

Also remove the `java` plugin from the toolchain configuration if it is declared separately. The `kotlin("jvm")` plugin
alone is sufficient.

Remove Lombok dependency from `build.gradle.kts` once it is no longer referenced. Verify `detekt.yml` rules are
appropriate for Kotlin idioms (e.g., `MagicNumber`, `LongMethod`).

---

## Pitfalls & Required Improvements

### Pitfall 1 — `@Transactional` on Reactive Methods

**Problem**: `@Transactional` from Spring is not reactive-aware by default. MongoDB's reactive transactions require
`ReactiveTransactionManager`. The current Java code uses `@Transactional` on methods returning `Mono`/`Flux` which may
silently not work unless configured.

**Action**: Verify `ReactiveTransactionManager` is configured. Consider replacing `@Transactional` with explicit
`transactionalOperator.transactional(mono)` at the call site, or add `@Transactional` from
`org.springframework.transaction.annotation` and configure a proper `ReactiveMongoTransactionManager` bean. This applies
equally in Kotlin.

### Pitfall 2 — `Tuple3` / `Tuple2` from Reactor

**Problem**: `SemVerUtils.parse` returns `reactor.util.function.Tuple3<Integer, Integer, Integer>`, which is verbose.
`ProblemBeforeSaveListener` uses destructuring on it.

**Action**: Replace with a Kotlin `data class SemVer(val major: Int, val minor: Int, val patch: Int)` or Kotlin's
`Triple`. Destructuring with `val (major, minor, patch) = SemVerUtils.parse(...)` becomes natural.

### Pitfall 3 — `.block()` in `CheckResultMessageListener`

**Problem**: `CheckResultMessageListener.onCheckResultMessage` calls `.block()` on a `Mono`. This is required because
`@RabbitListener` methods are synchronous JVM methods, and there is no built-in async RabbitMQ listener support in
Spring AMQP for reactive types. The `.block()` is intentional but must never run on a non-blocking Netty event loop
thread. Currently this is safe because `@RabbitListener` callbacks run on AMQP consumer threads (separate from Netty).

**Action**: Document this explicitly in a comment so it is not mistakenly "fixed" in Kotlin. Alternatively, annotate the
method `@Blocking` (Project Reactor) or add a comment explaining that this is an AMQP-to-reactive bridge. When
migrating, preserve the `.block()`.

### Pitfall 4 — `SubmissionAfterSaveListener.subscribe()` fire-and-forget

**Problem**: `onAfterSave` calls `.subscribe()` without handling backpressure or errors beyond logging. A `subscribe()`
at a MongoDB event listener means the upsert is detached from any transaction or caller context. If the upsert fails,
only the log captures it (there is even a TODO comment for retry logic).

**Action**: The TODO comment reads `// todo: implement the duplicate-key retry`. When migrating to Kotlin, this is a
good time to implement retry logic using `.retryWhen(Retry.backoff(...))`. Also consider wrapping the fire-and-forget in
`GlobalScope.launch` (not recommended in production) or keep it as `.subscribe()` but add more robust error handling.

### Pitfall 5 — `Objects.requireNonNull` vs Kotlin Null Safety

**Problem**: Several service methods use `Objects.requireNonNull(submission.getId(), "message")` as a runtime null check
that also provides a non-null type to the compiler. In Kotlin, `Submission.id` is `String?`, so every usage of
`submission.id` that assumes non-null requires a `!!` or `requireNotNull()`.

**Action**: Audit all places where entity IDs are used after a save (when they are guaranteed non-null). Consider
changing entity `id` fields to remain `String?` but using `requireNotNull(id) { "..." }` explicitly, or assert
non-nullability at the repository boundary with a mapping extension function.

### Pitfall 6 — Lombok `@NonNull` on Repository Method Parameters

**Problem**: Java repositories use Lombok `@NonNull` on `Collection<String>` parameters for `@Query` methods. In Kotlin,
this translates to non-nullable types which is correct behavior, but the `@Query` annotation strings must be preserved
exactly.

**Action**: When migrating repository interfaces, copy `@Query` and `@Aggregation` annotation strings verbatim. Verify
no escaping issues with `\"` in Kotlin multiline strings (use `"""..."""` with `trimIndent()` where appropriate).

### Pitfall 7 — Java `@Nullable` in `SubmissionService.findUserSubmissions`

**Problem**: `findUserSubmissions(String userId, @Nullable String problemId, ...)` uses Spring's `@Nullable`. The null
check drives a code branch.

**Action**: Change signature to `problemId: String?` in Kotlin and use `if (problemId != null)` or
`problemId?.let { ... } ?: run { ... }`.

### Pitfall 8 — Wildcard Imports in `SaveListeners`

**Problem**: `SubmissionAfterSaveListener` constructs BSON `Document` lists inline. This is verbose but correct Java. In
Kotlin, `listOf()` and `mapOf()` make it cleaner, but the semantics must not change.

**Action**: Replace `List.of(...)` with `listOf(...)` and `new Document(...)` chains with Kotlin's named constructors.
Carefully test the aggregation pipeline after migration by running integration tests.

### Pitfall 9 — `UserRole` Ordering / `compareTo`

**Problem**: `BundlePermissionEvaluator` uses `userRole.compareTo(requiredRole) >= 0` to check if a user has at least a
certain role. This relies on `UserRole` being a Java/Kotlin enum where declaration order implies rank (
`USER < MODERATOR < ADMIN`).

**Action**: This is already correct since `UserRole` is a Kotlin enum in `edukate-common`. Verify the declaration order
is `USER, MODERATOR, ADMIN`. The `compareTo` works on enum ordinal in both Java and Kotlin.

### Pitfall 10 — Spring WebFlux Controller `Authentication` Parameter Handling

**Problem**: Controllers accept `Authentication` as a method parameter injected by Spring Security. Some controllers
call `AuthUtils.id(authentication)` which can return null if the authentication is anonymous. Some methods guard against
this; others do not.

**Action**: In Kotlin, declare the parameter as `authentication: Authentication?` where the endpoint can be accessed
without authentication (e.g., `permitAll` routes), and `authentication: Authentication` (non-null) where authentication
is required by security config. This makes the null contract explicit at compile time.

---

## Suggested Improvements During Migration

### 1. Replace `Tuple2` returns with data classes

In `CheckResultService.saveAndUpdateSubmission` the `Tuple2<CheckResult, Submission>` return type is opaque. Replace
with:

```kotlin
data class CheckResultWithSubmission(val checkResult: CheckResult, val submission: Submission)
```

### 2. Extract `SemVer` data class from `SemVerUtils`

The three-component version parse result is used in two places (`SemVerUtils.parse` and `ProblemBeforeSaveListener`). A
proper `data class SemVer(val major: Int, val minor: Int, val patch: Int)` with a companion
`fun parse(version: String): SemVer` is cleaner than `Triple` or `Tuple3`.

### 3. `FileManager.doesFileExist` returns `Mono<Boolean>` but implementation is fragile

The current implementation calls `storage.metadata(key).thenReturn(true).defaultIfEmpty(false)`. If metadata throws an
error (vs returning empty), it propagates as an error rather than `false`. Consider `.onErrorReturn(false)` for
resilience.

### 4. `BundleService.getBundleInvitedUsers` — unnecessary wrapping

```java
Mono.justOrEmpty(bundle.getInvitedUserIds()).

flatMapMany(userService::findUsersByIds)
```

`bundle.invitedUserIds` is a `Set<String>`, never null in Kotlin. Remove `justOrEmpty` and call
`userService.findUsersByIds(bundle.invitedUserIds)` directly.

### 5. `SubmissionService.saveSubmission` — `.then(Flux...)` anti-pattern

The current code does:

```java
submissionFileService.moveSubmissionFiles(...)
    .

then(Flux.fromIterable(...).

flatMap(...).

collectList())
```

`.then()` discards the upstream result. This is correct here (the move side-effect is fire-and-wait) but is subtle. In
Kotlin, extract into a named variable for clarity.

### 6. `CheckResultMessageListener` — notification fire-and-forget

`doOnSuccess(notifier::notify)` does not subscribe to the returned `Mono` from `notifier.notify(...)`. If
`Notifier.notify` returns `Mono<Void>`, the notification is never actually sent. Verify `Notifier.notify` signature and
fix to `.flatMap(notifier::notify)` if it returns a reactive type.

### 7. Use `coroutines` for complex reactive chains (optional)

For services with deeply nested `.flatMap` chains (e.g., `SubmissionService.saveSubmission`), consider adding
`kotlinx-coroutines-reactor` to the dependency and using `suspend fun` + `awaitSingle()`. This is optional but improves
readability significantly for complex orchestration logic.

### 8. Remove Lombok once all Java files are gone

Lombok is used only in Java files (`@RequiredArgsConstructor`, `@AllArgsConstructor`, `@NonNull`, `@Slf4j`,
`@UtilityClass`). All of these have direct Kotlin equivalents:

- `@RequiredArgsConstructor` → primary constructor with `val` params
- `@AllArgsConstructor` → same
- `@NonNull` → non-nullable type
- `@Slf4j` → `private val log = LoggerFactory.getLogger(...)` or `KotlinLogging.logger {}`
- `@UtilityClass` → `object`

After full migration, remove `lombok` and `lombok-mapstruct-binding` from `build.gradle.kts`.
