# TODO

## Observability in cluster ✅

### Done
- LGTM stack (Loki, Grafana, Tempo, Prometheus via `kube-prometheus-stack`, Promtail) deployed as Helm
  sub-charts in `edukate-chart`, gated by `lgtm.enabled`
- All four Spring Boot services emit Prometheus metrics at `/actuator/prometheus` (Micrometer + `micrometer-registry-prometheus`)
- Prometheus auto-discovers all pods via `prometheus.io/scrape` annotations (already on all pods via `_helpers.tpl`)
- Distributed tracing wired via `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp`; traces pushed
  to Tempo at `http://tempo:4318/v1/traces` in prod (50% sampling)
- Structured JSON logging via `logstash-logback-encoder` in non-dev profiles; `traceId`/`spanId` in MDC
  automatically correlated to Tempo traces in Grafana
- Grafana available at `https://grafana.edukatemeplease.online` with TLS, persistent storage, and pre-wired
  Loki + Tempo datasources
- Dev tracing disabled via `management.tracing.export.enabled=false` (no OTLP noise in local dev)
- `management.metrics.tags.application` set on all 4 services — adds `application` label to every metric so
  Grafana dashboard template variables resolve correctly
- `ObservedAspect` registered in `edukate-checker` (`ObservationConfig`) — activates `@Observed` AOP processing
- `@Observed(name = "ai.check")` on `CheckerService.runCheck()` — end-to-end AI check span + timer in Tempo/Prometheus
- `@Observed(name = "openai.call")` on `SpringAiChatService.makeRequest()` — raw OpenAI API call span + timer
- Business counters in `edukate-backend`: `submissions.created` (by `problemId`), `check.outcomes` (by `status`)

---

## Display CheckResult on frontend

Show the AI checker's verdict (`CheckResult`) to the user after a submission is checked.

**How:** Poll or subscribe to the submission status on `SubmissionPage`; once status transitions from `PENDING`
to `ACCEPTED`/`REJECTED`, fetch the associated `CheckResult` via a new `/api/v1/check-results/{submissionId}`
endpoint (or include it in `SubmissionDto`). Render `status`, `trustLevel`, `errorType`, and `explanation` —
possibly in a collapsible accordion similar to `ResultAccordionComponent`.

---

## Human check panel

Admin/moderator UI to manually review and override AI checker verdicts.

**How:** Add a protected page (`/admin/submissions`) listing submissions with `PENDING` or disputed status.
Each row shows the problem, submitted images, and AI verdict. A moderator can accept/reject with an optional
comment, triggering a `PATCH /api/v1/submissions/{id}/check` endpoint that writes a `CheckResult` with
`trustLevel = 1.0` and `source = HUMAN`, then updates `UserProblemStatus`.

---

## Suppress actuator access logs

`/actuator/health` is probed every few seconds by Kubernetes liveness/readiness probes, flooding logs with
noise at DEBUG/INFO level.

**How:** Set `logging.level.reactor.netty.http.server=WARN` to silence Reactor Netty's access log globally,
then selectively re-enable it for application routes via a custom `AccessLogFactory` that filters out
`/actuator/**` paths. Alternatively, configure `reactor.netty.http.server.access-logs=false` and emit
access logs from a WebFilter instead, skipping actuator paths. Move `logging.level.org.springframework=DEBUG`
to `application-dev.properties` so prod is quiet by default.

---

## Bundle invite link management

Direct join-by-share-code is disabled. Bundles currently support invite-only membership (admin invites
a specific user by name). This should be replaced with a proper invite link flow — similar to Telegram
group links — where a bundle admin generates a time-limited, revocable invite URL that any recipient can
click to join without the admin knowing the username in advance.

**Planned design:**
- `BundleInviteLink` entity: `id`, `bundleShareCode`, `createdBy`, `expiresAt`, `maxUses`, `uses`, `revoked`
- `POST /api/v1/bundles/{shareCode}/invite-links` — admin creates a link (set TTL, max uses)
- `GET  /api/v1/bundles/join/{linkId}` — anyone with the link calls this to join; enforces TTL, max uses, not-already-member
- `DELETE /api/v1/bundles/{shareCode}/invite-links/{linkId}` — admin revokes a link
- `GET  /api/v1/bundles/{shareCode}/invite-links` — admin lists active links
- Remove or repurpose `inviteUser`/`expireInvite`/`reactToInvite` service+controller methods once the new flow is live

**Why disabled now:** the old direct-join endpoint and service method (`joinUser`) were removed during the
mapper-extraction refactor; restoring them without a proper link-management story adds dead surface area.

---

## Implement ReactiveUserDetailsPasswordService (`updatePassword`)

`UserDetailsService` in `edukate-gateway` implements `ReactiveUserDetailsPasswordService` (required by
Spring Security 7 when a `ReactiveUserDetailsService` bean is present), but `updatePassword` is currently
a no-op stub that returns the user unchanged. Spring Security calls this method to upgrade password hashes
on login (e.g. bcrypt cost factor increase) — without a real implementation, hash upgrades are silently
dropped and the stored password is never updated.

### What needs to happen

Spring Security passes the existing `UserDetails` and the newly encoded password string. The implementation
must persist that new password to MongoDB (via the backend), then invalidate the affected cache entries so
the next lookup fetches the fresh credentials.

### Changes required

**`edukate-backend` — new internal endpoint**

- Add `PATCH /internal/users/{id}/password` to `UserController` (or a dedicated `InternalUserController`).
  Body: a simple `UpdatePasswordRequest(encodedPassword: String)`.
- Add `UserService.updateUserPassword(id: String, encodedPassword: String): Mono<UserCredentials>` that
  updates only the `token` field on the `User` document and returns the updated credentials.
- The endpoint must be whitelisted in the backend's security config as an internal-only route (same pattern
  as the existing `/internal/users/**` endpoints).

**`edukate-gateway` — wire it up**

- Add `BackendService.updateUserPassword(id: String, newEncodedPassword: String): Mono<UserCredentials>` —
  issues `PATCH /internal/users/{id}/password`. Annotate with:
  - `@CacheEvict(cacheNames = ["user-credentials-by-id"], key = "#id")`
  - Also evict `user-credentials-by-name`, but the username is only known from the response — use
    `@CacheEvict(cacheNames = ["user-credentials-by-name"], key = "#result.username")` with
    `afterInvocation = true`, or fetch the username from the `UserDetails` argument passed into
    `updatePassword` and evict by it explicitly before delegating.
- Replace the stub in `UserDetailsService.updatePassword`:
  ```kotlin
  override fun updatePassword(user: UserDetails, newPassword: String?): Mono<UserDetails> {
      val id = requireNotNull((user as EdukateUserDetails).id)
      return backendService.updateUserPassword(id, checkNotNull(newPassword))
          .map(::EdukateUserDetails)
  }
  ```

### Cache eviction note

Password update must evict **both** caches because the same credentials are reachable by name
(login flow → `user-credentials-by-name`) and by id (JWT filter → `user-credentials-by-id`).
The by-id entry's 24h TTL makes stale-password risk especially high without explicit eviction.
