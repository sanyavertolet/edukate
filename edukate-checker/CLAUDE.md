# edukate-checker

AI-powered problem checking service. Receives check requests from the backend via RabbitMQ, calls OpenAI via Spring AI, and publishes results back.

## Port

5830 (main), 5831 (management)

## Messaging Flow

```
edukate-backend  →  [edukate.check.schedule.v1]  →  edukate-checker
edukate-checker  →  [edukate.check.result.v1]    →  edukate-backend
```

## Key Classes

- `ModelResponse` — OpenAI API response model
- `RequestContext` — Context bundled with each check request

## Configuration

- `application.properties` / `application-dev.properties`
- OpenAI: system prompt, model selection, temperature (fixed at 1)
- HTTP client: connect timeout 5s, read timeout 2m (long — AI calls can be slow)
- S3 for fetching submission files
- RabbitMQ settings
- Profiles: `dev`, `secure`, `local`

## Dependencies

`edukate-common`, `edukate-auth`, `edukate-messaging`, `edukate-storage`

## Testing Notes

- Mock the OpenAI Spring AI client — do not make real API calls in tests
- Test the RabbitMQ listener: publish a `SubmissionContext` message, verify `CheckResultMessage` is published back
- Test error paths: AI returns invalid response, AI times out, S3 file not found
- Use `StepVerifier` for any reactive chains
- Check that `CheckErrorType` values are set correctly for each failure mode
