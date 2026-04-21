# edukate-backend

Core business logic service. Manages problems, problem sets, submissions, users, and file storage.

## Port

5800 (main), 5801 (management)

## Domain Entities (PostgreSQL via R2DBC)

| Entity             | Purpose                                                    |
|--------------------|------------------------------------------------------------|
| `Problem`          | Educational problem with subtasks, images, answers, status |
| `Book`             | Problem book/collection with slug identifier               |
| `ProblemSet`       | Collection of problems; user roles, visibility, share code |
| `ProblemSetProblem`| Junction table: ProblemSet ↔ Problem with ordering         |
| `Submission`       | User's submission for a problem; statuses: PENDING, FAILED, SUCCESS |
| `User`             | User account with name, email, roles, status               |
| `CheckResult`      | AI checker output for a submission                         |
| `ProblemProgress`  | Per-user progress per problem (latest + best submission)   |
| `Answer`           | Reference answer for a problem                             |
| `FileObject`       | S3 file metadata with polymorphic FileKey                  |

## Problem Key

Problems are identified by a composite key: `bookSlug/code` (e.g., `savchenko/1.1.1`).

- `code` — numeric part like `1.1.1` (unique per book, also globally unique by convention)
- `bookSlug` — book identifier like `savchenko`
- `key` — composite stored column: `savchenko/1.1.1` (globally unique)

The `key` is the public identifier used in URLs and all API fields (`problemKey`).

## ID Strategy

All entity IDs are `Long` (`BIGSERIAL` in PostgreSQL). **DTOs never expose raw numeric IDs.**
Each entity has a human-readable public identifier:

| Entity      | Public Identifier | Example              |
|-------------|-------------------|----------------------|
| Problem     | `key`             | `savchenko/1.1.1`   |
| Book        | `slug`            | `savchenko`          |
| User        | `name` (username) | `john`               |
| ProblemSet  | `shareCode`       | `aBcDeF1234`         |
| Submission  | `id` (no natural key) | `42`             |

Controllers accept `{bookSlug}/{code}` in paths for problem-related endpoints,
services resolve the key to internal IDs.

## Key DTOs

- `ProblemDto` — has `key`, `code`, `bookSlug` (no raw numeric id)
- `ProblemMetadata` — has `key`, `code`, `bookSlug`, `isHard`, `tags`, `status`
- `ProblemSetDto`, `ProblemSetMetadata` — problem set API representations (use `shareCode`)
- `SubmissionDto` — uses `problemKey` (composite key)
- `CreateSubmissionRequest` — accepts `problemKey: String`
- `CreateProblemSetRequest`, `ChangeProblemSetProblemsRequest` — accept `problemKeys: List<String>`
- `AnswerDto` — no id field, identified by problem key
- `CheckResultDto`, `CheckResultInfo`
- `FileMetadata`, `FileObject`

## Mapper Layer (`mappers/`)

`ProblemSetMapper`, `ProblemMapper`, `SubmissionMapper` are `@Component` assembler beans that own all
reactive DTO construction (presigned URLs, user-name lookups, status resolution, key-to-id resolution).
Controllers inject both the relevant service and its mapper; services contain only business logic.

## Internal Controllers

- `AnswerInternalController` — POST `/internal/answers` and `/internal/answers/batch` for seeding answers
- `ProblemInternalController` — POST `/internal/problems` and `/internal/problems/batch` for seeding problems
- `BookInternalController` — POST `/internal/books` for seeding books
- `UserInternalController` — user management endpoints

## Async Messaging (RabbitMQ)

- Publishes to `edukate.check.schedule.v1` → checker consumes
- Consumes from `backend.check.result.v1.q` ← checker publishes results
- Publishes to `edukate.notify.v1` → notifier sends notifications

## Dependencies

`edukate-common`, `edukate-auth`, `edukate-messaging`, `edukate-storage`

## Configuration

- `application.properties` / `application-dev.properties`
- PostgreSQL via R2DBC, S3/MinIO endpoint, RabbitMQ settings
- Flyway migrations in `src/main/resources/db/migration/`
- Max in-memory size: 100MB
- Presigned URL signature duration: 1h
- Profiles: `dev`, `secure`, `local` (MinIO local endpoint), `notifier`

## Testing Notes

- Use `@SpringBootTest` + Testcontainers `PostgreSQLContainer` with `@ServiceConnection`
- Use `WebTestClient` for controller/integration tests
- Mock `Notifier` and RabbitMQ-dependent beans — use `NoopNotifier` from `edukate-common`
- Mock S3/storage with a test `Storage` implementation or Testcontainers MinIO
- All reactive code must use `StepVerifier` from `reactor-test` for unit-level testing
- Test access control: problem set ADMIN vs USER roles, visibility rules, share codes
- Test submission status transitions
