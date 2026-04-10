# edukate-checker — Testing Plan

## Testability Assessment

| Class                                                   | Strategy       | Notes                                                                                    |
|---------------------------------------------------------|----------------|------------------------------------------------------------------------------------------|
| `RequestContext`                                        | ✅ Pure unit    | Validates `init` blocks only — no deps                                                   |
| `ModelResponse`                                         | ✅ Pure unit    | Default values + Jackson schema annotations                                              |
| `CheckResultMessageUtils` (top-level fns)               | ✅ Pure unit    | Pure functions, no deps                                                                  |
| `NoopChatService`                                       | ✅ Pure unit    | No deps; verify stub values and log warning                                              |
| `RabbitConfig`                                          | ✅ Pure unit    | Instantiate bean directly; assert queue/binding props                                    |
| `CheckerService`                                        | MockK          | Mock `ChatService` + `MediaContentResolver`                                              |
| `SpringAiChatService`                                   | MockK          | Mock `ChatClient` builder chain — never call real OpenAI                                 |
| `RabbitResultPublisher`                                 | MockK          | Mock `RabbitTemplate`; assert retry behaviour                                            |
| `MediaContentResolver`                                  | MockK          | Mock `RawKeyReadOnlyStorage`; verify byte assembly + Media                               |
| `SubmissionContextListener`                             | MockK          | Mock `CheckerService` + `ResultPublisher`; verify delegation                             |
| `RawKeyReadOnlyStorage`                                 | MockK          | Mock `S3AsyncClient` + `S3Presigner`; integration-test via Testcontainers MinIO (future) |
| Config classes (`ChatClientConfig`, `RestClientConfig`) | Spring context | Wired correctly by `@SpringBootTest`; not worth isolated unit tests                      |

**Key testing rule:** never call real OpenAI or real S3. Always mock at the `ChatClient` /
`RawKeyReadOnlyStorage` boundary. The `silent` profile (`NoopChatService`) is for runtime dev use,
not for tests.

---

## Test Layout

```
src/test/kotlin/io/github/sanyavertolet/edukate/checker/
├── CheckerFixtures.kt
├── domain/
│   ├── RequestContextTest.kt
│   └── ModelResponseTest.kt
├── utils/
│   └── CheckResultMessageUtilsTest.kt
├── services/
│   ├── CheckerServiceTest.kt
│   ├── NoopChatServiceTest.kt
│   ├── SpringAiChatServiceTest.kt
│   └── RabbitResultPublisherTest.kt
├── components/
│   ├── MediaContentResolverTest.kt
│   └── SubmissionContextListenerTest.kt
└── config/
    └── RabbitConfigTest.kt
```

---

## `CheckerFixtures.kt` — shared builders

No tests. Provides reusable factory functions for all test files:

```kotlin
fun submissionContext(
    submissionId: String = "sub-1",
    problemId: String = "prob-1",
    problemText: String = "Solve x^2 = 4",
    problemImageRawKeys: List<String> = listOf("problems/prob-1/img.png"),
    submissionImageRawKeys: List<String> = listOf("users/u1/submissions/prob-1/sub-1/img.png"),
): SubmissionContext

fun modelResponse(
    status: CheckStatus = CheckStatus.SUCCESS,
    trustLevel: Float = 0.9f,
    errorType: CheckErrorType = CheckErrorType.NONE,
    explanation: String = "Correct.",
): ModelResponse

fun mockMedia(): Media  // minimal Spring AI Media stub
```

---

## `RequestContextTest` — pure unit

No Spring context. All assertions use AssertJ.

| Test                                  | Contract                                                                         |
|---------------------------------------|----------------------------------------------------------------------------------|
| `valid context is created`            | No exception when `problemText` is non-blank and `submissionImages` is non-empty |
| `blank problem text throws`           | `require` throws `IllegalArgumentException` when `problemText` is blank          |
| `whitespace-only problem text throws` | Same — `"   "` is blank                                                          |
| `empty submission images throws`      | `require` throws when `submissionImages` is empty list                           |
| `empty problem images is allowed`     | No exception — problem may have text only, no images                             |

---

## `ModelResponseTest` — pure unit

Uses `ObjectMapper().registerKotlinModule()` directly. No Spring context.

| Test                                                | Contract                                                                                  |
|-----------------------------------------------------|-------------------------------------------------------------------------------------------|
| `defaults are INTERNAL_ERROR`                       | `status == INTERNAL_ERROR`, `trustLevel == 0f`, `errorType == NONE`, `explanation == ""`  |
| `all fields survive Jackson round-trip`             | Serialise a fully populated response; deserialise back; all fields match                  |
| `missing fields fall back to defaults`              | Deserialise `{}` → default `ModelResponse`                                                |
| `@JsonPropertyDescription is present on all fields` | Each field has a non-blank description (Spring AI reads these to build the OpenAI schema) |

---

## `CheckResultMessageUtilsTest` — pure unit

Tests the two top-level functions: `success()` and `error()`.

| Test                                                   | Contract                                                                                                   |
|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `success maps status and trust level`                  | `ModelResponse(MISTAKE, 0.7f, ALGEBRAIC, "...")` → `CheckResultMessage` has `MISTAKE`, `0.7f`, `ALGEBRAIC` |
| `success clamps trustLevel > 1 to 1`                   | Input `1.5f` → output `1.0f`                                                                               |
| `success clamps trustLevel < 0 to 0`                   | Input `-0.3f` → output `0.0f`                                                                              |
| `success forces errorType NONE when status is SUCCESS` | `ModelResponse(SUCCESS, 0.9f, ALGEBRAIC, "")` → `errorType == NONE`                                        |
| `success preserves errorType when status is MISTAKE`   | `ModelResponse(MISTAKE, 0.5f, CONCEPTUAL, "")` → `errorType == CONCEPTUAL`                                 |
| `success carries submissionId from context`            | `result.submissionId == submissionContext.submissionId`                                                    |
| `error returns INTERNAL_ERROR`                         | `error(ctx).status == INTERNAL_ERROR`                                                                      |
| `error sets trustLevel to 0`                           | `error(ctx).trustLevel == 0f`                                                                              |
| `error explanation is non-blank`                       | `error(ctx).explanation.isNotBlank()`                                                                      |
| `error carries submissionId from context`              | `error(ctx).submissionId == ctx.submissionId`                                                              |

---

## `NoopChatServiceTest` — pure unit

| Test                                 | Contract                                                                             |
|--------------------------------------|--------------------------------------------------------------------------------------|
| `makeRequest emits exactly one item` | `StepVerifier.create(svc.makeRequest(ctx)).expectNextCount(1).verifyComplete()`      |
| `returned status is SUCCESS`         | `StepVerifier` asserts `value.status == CheckStatus.SUCCESS`                         |
| `trustLevel is in range [0, 1]`      | `value.trustLevel in 0f..1f`                                                         |
| `@PostConstruct log is emitted`      | After construction, a WARN-level log is produced (use `ListAppender` or log capture) |

---

## `CheckerServiceTest` — MockK

```kotlin
val chatService = mockk<ChatService>()
val resolver   = mockk<MediaContentResolver>()
val service    = CheckerService(chatService, resolver)
```

`StepVerifier` for all reactive assertions.

| Test                                                | Setup                                                                         | Contract                                                              |
|-----------------------------------------------------|-------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `happy path returns SUCCESS result`                 | resolver returns media; chatService returns `ModelResponse(SUCCESS, 0.9f, …)` | emitted `CheckResultMessage` has `SUCCESS` and correct `submissionId` |
| `problem text is forwarded to RequestContext`       | capture `RequestContext` via slot                                             | `captured.problemText == ctx.problemText`                             |
| `resolved images are passed to RequestContext`      | resolver returns specific `Media` list                                        | `captured.submissionImages` matches mocked list                       |
| `MISTAKE response is mapped correctly`              | chatService returns `ModelResponse(MISTAKE, 0.6f, CONCEPTUAL, …)`             | result has `MISTAKE`, `CONCEPTUAL`, `trustLevel ≈ 0.6f`               |
| `chatService error returns INTERNAL_ERROR`          | chatService returns `Mono.error(RuntimeException())`                          | `onErrorReturn` catches it; result has `INTERNAL_ERROR`               |
| `resolver error returns INTERNAL_ERROR`             | resolver returns `Flux.error(RuntimeException())`                             | same `onErrorReturn` path; result has `INTERNAL_ERROR`                |
| `empty chatService response returns INTERNAL_ERROR` | chatService returns `Mono.empty()`                                            | `switchIfEmpty` triggers error; caught by `onErrorReturn`             |

---

## `SpringAiChatServiceTest` — MockK

The `ChatClient` builder chain is deeply nested. Mock the entire chain using MockK's relaxed mode
and capture the arguments passed to critical steps.

```kotlin
val chatClient   = mockk<ChatClient>(relaxed = true)
val promptSpec   = mockk<ChatClient.ChatClientRequestSpec>(relaxed = true)
val callSpec     = mockk<ChatClient.CallResponseSpec>(relaxed = true)
val service      = SpringAiChatService(chatClient)

every { chatClient.prompt() } returns promptSpec
every { promptSpec.system(any<Consumer<ChatClient.PromptSystemSpec>>()) } returns promptSpec
every { promptSpec.user(any<Consumer<ChatClient.PromptUserSpec>>()) } returns promptSpec
every { promptSpec.call() } returns callSpec
every { callSpec.entity(ModelResponse::class.java) } returns fixtures.modelResponse()
```

| Test                                                  | Contract                                                                                  |
|-------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `makeRequest emits the entity returned by ChatClient` | `StepVerifier` asserts emitted value equals the mocked entity                             |
| `makeRequest completes without error on happy path`   | `verifyComplete()` — no error terminal signal                                             |
| `problem text is set as system param`                 | capture `Consumer<PromptSystemSpec>` and assert `param("problemText", …)` called          |
| `problem images are passed to user spec`              | capture `Consumer<PromptUserSpec>` and assert `.media(*problemMedia.toTypedArray())`      |
| `submission images are passed to user spec`           | same — assert second `.media()` call                                                      |
| `null entity response throws`                         | `every { callSpec.entity(…) } returns null`; `StepVerifier` asserts `onError`             |
| `makeRequest subscribes on boundedElastic`            | assert calling thread is not the subscriber thread (wrap in `publishOn(immediate)` check) |

---

## `RabbitResultPublisherTest` — MockK

```kotlin
val template  = mockk<RabbitTemplate>(relaxed = true)
val publisher = RabbitResultPublisher(template)
val message   = fixtures.checkResultMessage()
```

| Test                                                 | Contract                                                                                         |
|------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `publish calls convertAndSend with correct exchange` | `verify { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, message) }` |
| `publish completes on first attempt`                 | `StepVerifier.create(publisher.publish(message)).verifyComplete()`                               |
| `publish retries on transient failure then succeeds` | mock throws once then succeeds; `verify(exactly = 2) { template.convertAndSend(…) }`             |
| `publish propagates error after 3 failures`          | mock always throws; `StepVerifier` asserts `onError` after ≥ 3 `convertAndSend` calls            |
| `publish subscribes on boundedElastic`               | calling thread is not the subscriber thread                                                      |

---

## `MediaContentResolverTest` — MockK

```kotlin
val storage            = mockk<RawKeyReadOnlyStorage>()
val dataBufferFactory  = DefaultDataBufferFactory()
val resolver           = MediaContentResolver(storage, dataBufferFactory)
```

| Test                                              | Contract                                                                              |
|---------------------------------------------------|---------------------------------------------------------------------------------------|
| `empty key list returns empty Flux`               | `StepVerifier.create(resolver.resolveMedia(emptyList())).verifyComplete()`            |
| `single key fetches bytes and metadata`           | mock storage returns a `ByteBuffer`; assert `Media.data` matches the bytes            |
| `metadata MediaType is passed to Media`           | mock `storage.metadata()` returns `IMAGE_JPEG`; assert `Media.mimeType == IMAGE_JPEG` |
| `multiple keys return one Media per key in order` | three keys → `StepVerifier` asserts three items in insertion order                    |
| `S3 error propagates`                             | `storage.getContent()` returns `Flux.error(…)`; `StepVerifier` asserts `onError`      |

**Note on byte assembly:** `toByteArray()` uses `DataBufferUtils.join()` which materialises all
buffers. Provide a `DefaultDataBufferFactory` directly — no mock needed.

---

## `SubmissionContextListenerTest` — MockK

```kotlin
val checkerService  = mockk<CheckerService>()
val resultPublisher = mockk<ResultPublisher>()
val listener        = SubmissionContextListener(checkerService, resultPublisher)
val ctx             = fixtures.submissionContext()
```

`.block()` in the listener means the method is synchronous from the test's perspective — no
`StepVerifier` needed here.

| Test                                              | Contract                                                                                                                  |
|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `onSubmissionContext delegates to CheckerService` | `every { checkerService.runCheck(ctx) } returns Mono.just(result)`; after call, `verify { checkerService.runCheck(ctx) }` |
| `result is published`                             | `every { resultPublisher.publish(result) } returns Mono.empty()`; `verify { resultPublisher.publish(result) }`            |
| `method returns (does not hang)`                  | `.block()` completes; test does not time out                                                                              |
| `CheckerService error does not throw`             | `checkerService.runCheck()` returns `Mono.error(…)`; `onSubmissionContext()` swallows via `.block()` without rethrowing   |

---

## `RabbitConfigTest` — pure unit

Instantiate `RabbitConfig` directly. Create a minimal `TopicExchange` and call `scheduleTopology()`.

| Test                                   | Contract                                                                          |
|----------------------------------------|-----------------------------------------------------------------------------------|
| `queue name matches topology constant` | `Declarables` contains a `Queue` with `name == RabbitTopology.Q.SCHEDULE_CHECKER` |
| `queue is durable`                     | `queue.isDurable == true`                                                         |
| `binding routing key matches constant` | `binding.routingKey == RabbitTopology.Rk.SCHEDULE`                                |
| `binding exchange matches constant`    | `binding.exchange == RabbitTopology.EXCHANGE`                                     |

---

## Patterns Summary

| Scenario                           | Pattern                                                    |
|------------------------------------|------------------------------------------------------------|
| Pure logic (domain, utils, config) | JUnit 5 + AssertJ only — no Spring context                 |
| Reactive streams                   | `StepVerifier.create(…).assertNext { … }.verifyComplete()` |
| Service with dependencies          | `mockk<T>()` + `every { … } returns …` + `verify { … }`    |
| `.block()` in listener             | Call method directly; assert side effects via `verify`     |
| Log output assertion               | SLF4J `ListAppender` or Logback `OutputCaptureExtension`   |

---

## Running Tests

```bash
# Format first (ktfmt enforces project style)
./gradlew :edukate-checker:ktfmtFormat

# Run tests
./gradlew :edukate-checker:test

# Static analysis
./gradlew :edukate-checker:detekt

# Full check
./gradlew :edukate-checker:check
```
