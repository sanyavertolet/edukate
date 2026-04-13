# edukate-checker

AI-powered problem checking service. Receives check requests from the backend via RabbitMQ, fetches submission images
from S3, calls OpenAI via Spring AI, and publishes results back.

## Port

5830 (main), 5831 (management)

## Messaging Flow

```
edukate-backend  →  [edukate.check.schedule.v1]  →  edukate-checker
edukate-checker  →  [edukate.check.result.v1]    →  edukate-backend
```

**Inbound payload:** `SubmissionContext` (problem ID, submission ID, problem text, image S3 raw keys)
**Outbound payload:** `CheckResultMessage` (submission ID, status, trust level, error type, explanation)

## Architecture

This service is purely event-driven — no REST endpoints except Actuator.

```
SubmissionContextListener
    └─ CheckerService.runCheck(SubmissionContext)
          ├─ MediaContentResolver  →  S3 (fetch problem + submission images)
          ├─ ChatService.makeRequest(RequestContext)
          │     └─ SpringAiChatService  →  OpenAI (structured output: ModelResponse)
          │        NoopChatService      →  stub (profile: silent)
          ├─ CheckResultMessageUtils.success(ModelResponse)
          └─ on error: CheckResultMessageUtils.error(SubmissionContext)
                └─ RabbitResultPublisher.publish(CheckResultMessage)
```

## Classes

### Services

| Class                       | Responsibility                                                                       |
|-----------------------------|--------------------------------------------------------------------------------------|
| `CheckerService`            | Orchestrates check: build context → call AI → map response → publish result          |
| `ChatService`               | `fun interface` abstraction for AI calls; returns `Mono<ModelResponse>`              |
| `SpringAiChatService`       | Calls OpenAI via Spring AI ChatClient with structured output (`@Profile("!silent")`) |
| `NoopChatService`           | Stub returning SUCCESS; used for dev/testing without OpenAI (`@Profile("silent")`)   |
| `ResultPublisher`           | `fun interface` abstraction for publishing results to RabbitMQ                       |
| `RabbitResultPublisher`     | Publishes `CheckResultMessage` to exchange; retries 3× on failure                    |
| `MediaContentResolver`      | Fetches images from S3 by raw key, converts to Spring AI `Media` objects             |
| `SubmissionContextListener` | RabbitMQ `@RabbitListener`; bridges reactive chain via `.block()`                    |

### Domain Models

| Class            | Purpose                                                                                                                                  |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `RequestContext` | Bundles problem text + images for the AI call; validates non-blank text and non-empty submission images                                  |
| `ModelResponse`  | Structured OpenAI response: `status`, `trustLevel` (0–1), `errorType`, `explanation`; `@JsonPropertyDescription` drives Spring AI schema |

### Config & Utilities

| Class                       | Responsibility                                                                                                                                     |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `EdukateCheckerApplication` | Spring Boot entry point; scans `checker`, `common`, `storage` packages                                                                             |
| `ChatClientConfig`          | `@Primary ChatModel` bean that wraps `OpenAiChatModel`; fixes Spring AI 2.0.0-SNAPSHOT regression where `OpenAiChatOptions` lacks `mutate()` — returns `DefaultChatOptions` from `ChatOptions.builder()` which properly implements `mutate()` |
| `RestClientConfig`          | Configures HTTP timeouts (connect: 5s, read: 2m) for Spring AI's OpenAI client; requires `spring-boot-http-client` dep (Boot 4 module split)       |
| `RabbitConfig`              | Declares durable queue and binding for `checker.check.schedule.v1.q`                                                                               |
| `RawKeyReadOnlyStorage`     | `AbstractReadOnlyStorage<String, MediaType>`; fetches S3 objects by raw key                                                                        |
| `CheckResultMessageUtils`   | Top-level Kotlin functions; converts `ModelResponse` → `CheckResultMessage`; clamps trust level to [0, 1]; enforces `errorType = NONE` for SUCCESS |

## Configuration

- `application.properties` / `application-dev.properties`
- OpenAI: system prompt (`OPENAI_SYSTEM_PROMPT`), model (`OPENAI_MODEL`), API key (`OPENAI_API_KEY`), temperature fixed
  at 1.0
- HTTP client: connect timeout 5s, read timeout 2m (AI calls are slow)
- S3: fetches problem and submission images
- RabbitMQ: `RABBITMQ_ADDRESSES`
- Profiles: `dev`, `secure`, `local`, `silent`

## Dependencies

`edukate-common`, `edukate-auth`, `edukate-messaging`, `edukate-storage`

---

## Testing

### Test layout

```
src/test/kotlin/.../checker/
├── CheckerFixtures.kt                        — shared builders: SubmissionContext, ModelResponse, CheckResultMessage
├── domain/
│   ├── RequestContextTest.kt                 — validation logic (pure unit)
│   └── ModelResponseTest.kt                  — default values + Jackson round-trip (pure unit)
├── utils/
│   └── CheckResultMessageUtilsTest.kt        — trust clamping, errorType enforcement (pure unit)
├── services/
│   ├── CheckerServiceTest.kt                 — orchestration logic (MockK)
│   ├── NoopChatServiceTest.kt                — stub behaviour (pure unit)
│   ├── SpringAiChatServiceTest.kt            — ChatClient invocation (MockK)
│   └── RabbitResultPublisherTest.kt          — RabbitTemplate calls + retry (MockK)
├── components/
│   ├── MediaContentResolverTest.kt           — S3 fetch + Media construction (MockK)
│   └── SubmissionContextListenerTest.kt      — RabbitMQ listener delegation (MockK)
└── config/
    └── RabbitConfigTest.kt                   — queue/binding topology (pure unit)
```

### Detailed test contracts

#### `RequestContextTest` (pure unit)

| Test                              | Contract verified                                                            |
|-----------------------------------|------------------------------------------------------------------------------|
| `valid context is created`        | No exception when problemText is non-blank and submissionImages is non-empty |
| `blank problem text throws`       | `require` throws `IllegalArgumentException` when `problemText` is blank      |
| `empty submission images throws`  | `require` throws when `submissionImages` is empty                            |
| `empty problem images is allowed` | No exception when `problemImages` is empty list                              |

#### `ModelResponseTest` (pure unit)

| Test                                      | Contract verified                                                                                                   |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `default values are INTERNAL_ERROR`       | `status == CheckStatus.INTERNAL_ERROR`, `trustLevel == 0f`, `errorType == CheckErrorType.NONE`, `explanation == ""` |
| `Jackson round-trip preserves all fields` | Serialise then deserialise a fully populated `ModelResponse`; fields match                                          |
| `missing fields fall back to defaults`    | Deserialise `{}` JSON → default `ModelResponse`                                                                     |

#### `CheckResultMessageUtilsTest` (pure unit)

| Test                                                | Contract verified                                                                            |
|-----------------------------------------------------|----------------------------------------------------------------------------------------------|
| `success maps status and errorType`                 | `ModelResponse(MISTAKE, …, ALGEBRAIC, …)` → `CheckResultMessage` has `MISTAKE` / `ALGEBRAIC` |
| `success with trustLevel > 1 is clamped to 1`       | Input 1.5f → output 1.0f                                                                     |
| `success with trustLevel < 0 is clamped to 0`       | Input -0.3f → output 0.0f                                                                    |
| `success with SUCCESS status forces errorType NONE` | `ModelResponse(SUCCESS, …, ALGEBRAIC, …)` → `errorType == NONE`                              |
| `success with MISTAKE preserves errorType`          | `ModelResponse(MISTAKE, …, CONCEPTUAL, …)` → `errorType == CONCEPTUAL`                       |
| `error returns INTERNAL_ERROR status`               | `error(submissionContext).status == CheckStatus.INTERNAL_ERROR`                              |
| `error explanation is non-blank`                    | `error(submissionContext).explanation.isNotBlank()`                                          |
| `error submissionId matches context`                | `error(submissionContext).submissionId == submissionContext.submissionId`                    |

#### `NoopChatServiceTest` (pure unit)

| Test                                    | Contract verified                          |
|-----------------------------------------|--------------------------------------------|
| `makeRequest returns Mono with SUCCESS` | `StepVerifier` asserts `status == SUCCESS` |
| `trustLevel is in range`                | `trustLevel in 0f..1f`                     |

#### `SpringAiChatServiceTest` (MockK)

| Test                                              | Contract verified                                                                        |
|---------------------------------------------------|------------------------------------------------------------------------------------------|
| `makeRequest builds prompt with problem text`     | Verify `chatClient.prompt()` call chain receives `problemText` as system prompt variable |
| `makeRequest attaches problem images as media`    | Verify problem `Media` objects passed to `media(...)`                                    |
| `makeRequest attaches submission images as media` | Verify submission `Media` objects passed to `media(...)`                                 |
| `makeRequest returns Mono wrapping ModelResponse` | `StepVerifier` asserts returned `ModelResponse` equals mocked entity response            |
| `makeRequest runs on boundedElastic`              | Assert call is non-blocking on calling thread (subscribeOn wiring)                       |

#### `RabbitResultPublisherTest` (MockK)

| Test                                                 | Contract verified                                                                 |
|------------------------------------------------------|-----------------------------------------------------------------------------------|
| `publish calls convertAndSend with correct exchange` | Verify `RabbitTemplate.convertAndSend(exchange, routingKey, message)` called once |
| `publish uses result routing key`                    | Routing key equals `RabbitTopology.RK.CHECK_RESULT`                               |
| `publish succeeds on first attempt`                  | `StepVerifier` completes without error                                            |
| `publish retries on transient failure`               | Mock throws once, then succeeds; assert `convertAndSend` called twice             |
| `publish propagates error after 3 failures`          | Mock always throws; `StepVerifier` asserts `onError` after 3 attempts             |

#### `MediaContentResolverTest` (MockK)

| Test                                              | Contract verified                                                                   |
|---------------------------------------------------|-------------------------------------------------------------------------------------|
| `resolve empty key list returns empty`            | `StepVerifier` asserts empty list                                                   |
| `resolve single key fetches bytes from storage`   | Mock storage returns `ByteBuffer`; assert `Media` created with correct bytes        |
| `resolve infers MediaType from storage`           | Mock storage returns `IMAGE_JPEG` metadata; assert `Media.mimeType` is `IMAGE_JPEG` |
| `resolve multiple keys returns one Media per key` | Three keys → three `Media` objects in order                                         |
| `resolve propagates S3 error`                     | Storage mock emits error; `StepVerifier` asserts `onError`                          |

#### `CheckerServiceTest` (MockK)

| Test                                              | Contract verified                                                                                              |
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `runCheck happy path returns SUCCESS`             | Mock media resolver and `ChatService`; verify `CheckResultMessage` has correct submissionId and status         |
| `runCheck passes problem text to request context` | Capture `RequestContext` passed to `ChatService`; assert `problemText` matches `SubmissionContext.problemText` |
| `runCheck attaches resolved images to context`    | Assert `RequestContext.submissionImages` matches resolved `Media` list                                         |
| `runCheck maps MISTAKE response correctly`        | `ChatService` returns `MISTAKE`/`CONCEPTUAL`; result has same status/errorType                                 |
| `runCheck returns error on ChatService failure`   | `ChatService` emits error; result has `INTERNAL_ERROR` (onErrorReturn)                                         |
| `runCheck returns error on S3 fetch failure`      | Media resolver emits error; result has `INTERNAL_ERROR`                                                        |
| `runCheck clamps trust level via utils`           | Verify `CheckResultMessageUtils` receives raw `ModelResponse` (trust clamping covered in utils tests)          |

#### `SubmissionContextListenerTest` (MockK)

| Test                                              | Contract verified                                                                      |
|---------------------------------------------------|----------------------------------------------------------------------------------------|
| `onSubmissionContext delegates to CheckerService` | Verify `checkerService.runCheck(submissionContext)` called with correct argument       |
| `onSubmissionContext publishes result`            | Verify `resultPublisher.publish(result)` called with `CheckResultMessage` from service |
| `onSubmissionContext blocks until complete`       | Method returns only after reactive chain completes (implicit in `.block()` usage)      |

#### `RabbitConfigTest` (pure unit)

| Test                                   | Contract verified                                        |
|----------------------------------------|----------------------------------------------------------|
| `queue name matches topology constant` | `Queue.name == RabbitTopology.Q.SCHEDULE_CHECKER`        |
| `queue is durable`                     | `Queue.isDurable == true`                                |
| `binding uses correct routing key`     | `Binding.routingKey == RabbitTopology.RK.CHECK_SCHEDULE` |
| `binding targets correct exchange`     | `Binding.exchange == RabbitTopology.EXCHANGE`            |

### Key testing patterns

- **Unit tests** (domain, utils, config): no Spring context, JUnit 5 + AssertJ only.
- **Service/component tests**: `mockk()` for dependencies; `StepVerifier` for all reactive assertions.
- **No real OpenAI calls**: always mock `ChatClient` or use the `silent` profile with `NoopChatService`.
- **No real RabbitMQ**: mock `RabbitTemplate`; do not start a broker in unit tests.
- **No real S3**: mock the storage abstraction (`RawKeyReadOnlyStorage` or the underlying client).

### Run tests

```bash
./gradlew :edukate-checker:test
```
