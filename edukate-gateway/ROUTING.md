# edukate-gateway: Routing

This document describes how requests flow through the gateway — from the client, through the security filter chain, to
downstream services.

---

## Overview

```
Client
  │
  ▼
JwtAuthenticationFilter  ← extracts X-Auth cookie, validates JWT, populates X-Authorization-* headers
  │
  ▼
Security filter chain    ← enforces access rules (public / authenticated / denied)
  │
  ▼
Spring Cloud Gateway router
  ├── /api/*/auth/*            → handled locally (AuthController)
  ├── /api/*/notifications/**  → edukate-notifier
  └── /api/**                  → edukate-backend
```

---

## Spring Cloud Gateway Routes

Defined in `src/main/resources/application.yml` under `spring.cloud.gateway.routes`. Routes are evaluated **in
declaration order** — first match wins.

### Route 1 — `gateway_route` (local handling)

| Property   | Value                                                             |
|------------|-------------------------------------------------------------------|
| URI        | `no://op` (no forwarding — request is handled inside the gateway) |
| Predicates | `Path=/api/*/auth/*, /swagger/gateway/api-docs/**`                |
| Filters    | _(none)_                                                          |

Auth endpoints (`sign-in`, `sign-up`, `sign-out`) are handled by `AuthController` in this service. The Swagger API docs
for the gateway itself are also served locally.

### Route 2 — `notifier_route`

| Property   | Value                                                                                |
|------------|--------------------------------------------------------------------------------------|
| URI        | `${gateway.notifier.url}` — `http://notifier` (prod) / `http://localhost:5820` (dev) |
| Predicates | `Path=/api/*/notifications/**, /swagger/notifier/api-docs/**`                        |
| Filters    | `RemoveRequestHeader=Cookie`                                                         |

Notification API requests and the notifier's OpenAPI docs are forwarded to `edukate-notifier`. The `Cookie` header is
stripped before forwarding — downstream services authenticate via the `X-Authorization-*` headers set by
`JwtAuthenticationFilter`, not the raw cookie.

### Route 3 — `api_route`

| Property   | Value                                                                              |
|------------|------------------------------------------------------------------------------------|
| URI        | `${gateway.backend.url}` — `http://backend` (prod) / `http://localhost:5800` (dev) |
| Predicates | `Path=/api/**, /swagger/backend/api-docs/**`                                       |
| Filters    | `RemoveRequestHeader=Cookie`                                                       |

All other API requests fall through to `edukate-backend`. The backend's OpenAPI docs are also proxied here. Again, the
`Cookie` header is stripped.

---

## Security Filter Chain

Defined in `WebSecurityConfig` (active on `secure` profile). Two ordered filter chains:

### Chain 1 — Public endpoints (`@Order(1)`)

Matches `PublicEndpoints.asMatcher()` — currently:

```
/actuator/**
/internal/**
/api/v1/problems/**
/api/v1/auth/*
/swagger/**
/swagger-ui/**
```

These paths bypass authentication entirely. CSRF is disabled. No JWT validation occurs.

**Note**: `/internal/**` appears in `PublicEndpoints` to exclude it from the second chain's JWT processing, but it is
separately denied to all callers by Chain 2's `denyAll()` rule — see below.

### Chain 2 — All other requests (`@Order(2)`)

| Path pattern            | Rule                                                          |
|-------------------------|---------------------------------------------------------------|
| `/internal/**`          | `denyAll()` — always 403, regardless of authentication        |
| `PublicEndpoints` paths | `permitAll()`                                                 |
| `/api/**`               | `authenticated()` — requires valid JWT; returns 401 if absent |

`JwtAuthenticationFilter` is added at `SecurityWebFiltersOrder.AUTHENTICATION` in this chain. Unauthenticated access
returns `401 UNAUTHORIZED` (via `HttpStatusServerEntryPoint`).

HTTP Basic, form login, and logout are all disabled.

---

## User Context Propagation

`JwtAuthenticationFilter` extracts the JWT from the `X-Auth` cookie, validates it, loads the full user from
`edukate-backend` via `/internal/users/by-id/{id}`, and writes the following headers onto the mutated forwarded request:

| Header                   | Source field                  | Set by                               |
|--------------------------|-------------------------------|--------------------------------------|
| `X-Authorization-Id`     | `EdukateUserDetails.id`       | `HttpHeadersUtils.populateHeaders()` |
| `X-Authorization-Name`   | `EdukateUserDetails.username` | `HttpHeadersUtils.populateHeaders()` |
| `X-Authorization-Roles`  | `EdukateUserDetails.roles`    | `HttpHeadersUtils.populateHeaders()` |
| `X-Authorization-Status` | `EdukateUserDetails.status`   | `HttpHeadersUtils.populateHeaders()` |

Downstream services (`edukate-backend`, `edukate-notifier`) reconstruct `EdukateUserDetails` from these headers using
`HttpHeadersUtils.toEdukateUserDetails()`. They never see or validate the JWT directly.

The `Cookie` header is removed from forwarded requests (via the `RemoveRequestHeader=Cookie` filter) so raw JWT tokens
are not leaked to downstream services.

---

## Swagger Aggregation

The gateway exposes a unified Swagger UI at `/swagger`. It aggregates OpenAPI specs from all three services:

| Name               | Spec URL                                                    |
|--------------------|-------------------------------------------------------------|
| `edukate-gateway`  | `/swagger/gateway/api-docs` (served locally)                |
| `edukate-backend`  | `/swagger/backend/api-docs` (proxied via `api_route`)       |
| `edukate-notifier` | `/swagger/notifier/api-docs` (proxied via `notifier_route`) |

The Swagger UI configuration is served at `/swagger/gateway/api-docs/swagger-config`.

---

## CORS

Configured in `WebSecurityConfig.corsConfigurationSource()`.

| Setting                | Value                                                                  |
|------------------------|------------------------------------------------------------------------|
| Allowed origin pattern | `https://edukatemeplease.online` (prod) / `http://localhost:[*]` (dev) |
| Allowed methods        | `GET, POST, PUT, DELETE, OPTIONS`                                      |
| Allowed headers        | `Content-Type, api_key`                                                |
| Max age                | 3600 seconds                                                           |
| Applied to             | `/**` (all paths)                                                      |

---

## Environment URLs

| Property                      | Production                       | Development (`dev` profile) |
|-------------------------------|----------------------------------|-----------------------------|
| `gateway.backend.url`         | `http://backend`                 | `http://localhost:5800`     |
| `gateway.notifier.url`        | `http://notifier`                | `http://localhost:5820`     |
| `gateway.url`                 | `https://edukatemeplease.online` | `http://localhost:5810`     |
| `cors.allowed-origin-pattern` | `https://edukatemeplease.online` | `http://localhost:[*]`      |

---

## Updating This Document

This file must be kept in sync with:

- `src/main/resources/application.yml` — route definitions, CORS, URLs
- `edukate-common/.../PublicEndpoints.kt` — public endpoint list
- `security/WebSecurityConfig.java` (→ `WebSecurityConfig.kt` after migration) — security rules
- `filters/JwtAuthenticationFilter.java` (→ `JwtAuthenticationFilter.kt`) — header forwarding logic
