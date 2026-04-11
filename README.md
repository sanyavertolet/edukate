# Edukate

An educational platform for problem sets, submissions, and AI-powered checking.

---

## Status

<!-- CI -->
[![Backend Tests](https://github.com/sanyavertolet/edukate/actions/workflows/backend.yml/badge.svg)](https://github.com/sanyavertolet/edukate/actions/workflows/backend.yml)
[![Frontend](https://github.com/sanyavertolet/edukate/actions/workflows/frontend.yml/badge.svg)](https://github.com/sanyavertolet/edukate/actions/workflows/frontend.yml)
[![OpenAPI Schema](https://github.com/sanyavertolet/edukate/actions/workflows/openapi.yml/badge.svg)](https://github.com/sanyavertolet/edukate/actions/workflows/openapi.yml)
[![Detekt](https://github.com/sanyavertolet/edukate/actions/workflows/detekt.yml/badge.svg)](https://github.com/sanyavertolet/edukate/actions/workflows/detekt.yml)
[![ktfmt](https://github.com/sanyavertolet/edukate/actions/workflows/ktfmt.yml/badge.svg)](https://github.com/sanyavertolet/edukate/actions/workflows/ktfmt.yml)

<!-- Coverage -->
[![codecov](https://codecov.io/gh/sanyavertolet/edukate/branch/master/graph/badge.svg)](https://codecov.io/gh/sanyavertolet/edukate)

---

## Architecture

| Service            | Port | Role                                                           |
|--------------------|------|----------------------------------------------------------------|
| `edukate-gateway`  | 5810 | API gateway — routing, auth, Swagger aggregation at `/swagger` |
| `edukate-backend`  | 5800 | Core logic — problems, bundles, submissions, files             |
| `edukate-notifier` | 5820 | Async notifications via RabbitMQ                               |
| `edukate-checker`  | —    | AI-powered submission checking (Spring AI + OpenAI)            |
| `edukate-frontend` | 80   | React SPA                                                      |

All backend services use Spring WebFlux (reactive). MongoDB is the data store. RabbitMQ handles async messaging between backend and notifier/checker.

---

## Running locally

### Prerequisites

- JDK 21
- Node.js (version from `edukate-frontend/.nvmrc`)
- Docker

### 1. Start infrastructure

```bash
docker compose up -d
```

Starts MongoDB, MinIO, and RabbitMQ.

### 2. Start backend services

Each in a separate terminal:

```bash
./gradlew :edukate-gateway:bootRun --args='--spring.profiles.active=dev,secure'
```

```bash
./gradlew :edukate-backend:bootRun --args='--spring.profiles.active=dev,secure,local,notifier'
```

```bash
./gradlew :edukate-notifier:bootRun --args='--spring.profiles.active=dev,secure'
```

> The `local` profile points S3 at the local MinIO instance.
> The `notifier` profile enables the HTTP notifier bean in the backend.
> The `secure` profile enables the security filter chain.

### 3. Start the frontend

```bash
cd edukate-frontend
npm install
npm run dev
```

Open [http://localhost](http://localhost). Vite proxies `/api` to the gateway at `http://localhost:5810`.

---

## Running tests

### Backend

```bash
# All modules
./gradlew test

# Single module
./gradlew :edukate-backend:test

# With coverage report
./gradlew :edukate-backend:test jacocoTestReport
```

### Frontend

```bash
cd edukate-frontend
npm run test:run        # single run
npm run test:coverage   # with lcov/html report → coverage/
```

---

## Code quality

```bash
# Kotlin static analysis (zero-tolerance)
./gradlew detekt

# Kotlin formatting (rewrites files in place)
./gradlew ktfmtFormat

# Frontend typecheck + lint + formatting
cd edukate-frontend
npm run typecheck
npm run lint
npm run format:check
```

---

## OpenAPI specs

Specs live in `spec/` and are committed to the repository. To regenerate after backend changes:

```bash
./gradlew :edukate-backend:generateOpenApiDocs &
./gradlew :edukate-gateway:generateOpenApiDocs &
./gradlew :edukate-notifier:generateOpenApiDocs &
wait
```

Then regenerate TypeScript types for the frontend:

```bash
cd edukate-frontend
npm run generate:types
```
