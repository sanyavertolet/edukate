# edukate-storage

Shared library providing a generic S3/MinIO object storage abstraction. Used by `edukate-backend` and `edukate-checker`.

## Interfaces

### `ReadOnlyStorage<Key, Metadata>`

- `metadata(key)` → object metadata
- `getContent(key)` → reactive content stream
- `generatePresignedUrl(key)` → temporary download URL
- `prefixed(prefix)` → list objects by prefix

### `Storage<Key, Metadata>` (extends `ReadOnlyStorage`)

- `upload(key, contentType, content)` — auto-collects size
- `upload(key, length, contentType, content)` — explicit size
- `move(source, target)` — rename/move
- `delete(key)` — single delete
- `deleteAll(keys)` — batch delete

## File Key Types

| Class | Used for |
|---|---|
| `FileKey` | Generic files |
| `ProblemFileKey` | Problem assets |
| `SubmissionFileKey` | User submission files |
| `ResultFileKey` | AI check result files |
| `TempFileKey` | Temporary/staging files |

## Configuration (`S3Properties`)

```
s3.region=...
s3.access-key=...
s3.secret-key=...
s3.bucket=...
s3.endpoint=...          # Set for MinIO local with 'local' profile
s3.signature-duration=1h
```

`S3Config` — Spring `@Configuration` that wires the S3 client bean.

## Testing Notes

- Use Testcontainers MinIO for integration tests that touch real storage
- For unit tests, implement a simple in-memory `Storage` stub — the interface is straightforward to mock
- Test presigned URL expiry, move operations, and batch delete edge cases
- The `local` Spring profile configures a custom MinIO endpoint — integration tests should activate it
