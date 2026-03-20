# edukate-backend

Core business logic service. Manages problems, bundles, submissions, users, and file storage.

## Port

5800 (main), 5801 (management)

## Domain Entities (MongoDB documents)

| Entity | Purpose |
|---|---|
| `Problem` | Educational problem with subtasks, images, results, status |
| `Bundle` | Collection of problems; has user roles (ADMIN, USER) and visibility |
| `Submission` | User's answer to a problem; statuses: PENDING, ACCEPTED, REJECTED |
| `User` | Authentication data |
| `CheckResult` | AI checker output for a submission |
| `UserProblemStatus` | Junction — tracks per-user progress per problem |

## Key DTOs

- `ProblemDto`, `ProblemMetadata` — problem API representations
- `BundleDto`, `BundleMetadata` — bundle API representations
- `SubmissionDto`, `CreateSubmissionRequest`
- `CheckResultDto`, `Result`
- `FileMetadata`, `FileObject`

## Async Messaging (RabbitMQ)

- Publishes to `edukate.check.schedule.v1` → checker consumes
- Consumes from `backend.check.result.v1.q` ← checker publishes results
- Publishes to `edukate.notify.v1` → notifier sends notifications

## Dependencies

`edukate-common`, `edukate-auth`, `edukate-messaging`, `edukate-storage`

## Configuration

- `application.properties` / `application-dev.properties`
- MongoDB URI, S3/MinIO endpoint, RabbitMQ settings
- Max in-memory size: 100MB
- Presigned URL signature duration: 1h
- Profiles: `dev`, `secure`, `local` (MinIO local endpoint), `notifier`

## Testing Notes

- Use `@SpringBootTest` + embedded/test MongoDB (e.g., `de.flapdoodle.embed.mongo` or Testcontainers)
- Use `WebTestClient` for controller/integration tests
- Mock `Notifier` and RabbitMQ-dependent beans — use `NoopNotifier` from `edukate-common`
- Mock S3/storage with a test `Storage` implementation or Testcontainers MinIO
- All reactive code must use `StepVerifier` from `reactor-test` for unit-level testing
- Test access control: bundle ADMIN vs USER roles, visibility rules, share codes
- Test submission status transitions
