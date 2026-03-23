# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Edukate is a microservices-based educational platform. The backend is built with Kotlin/Spring Boot (WebFlux reactive),
and the frontend is React 19 + TypeScript + Vite.

## Commands

### Backend (Gradle)

```bash
# Build all modules
./gradlew build

# Run tests (all modules)
./gradlew test

# Run tests for a specific module
./gradlew :edukate-backend:test

# Run a specific test class
./gradlew :edukate-backend:test --tests "io.github.sanyavertolet.edukate.backend.SomeTest"

# Detekt static analysis
./gradlew detekt

# Run services locally
./gradlew :edukate-gateway:bootRun --args='--spring.profiles.active=dev,secure'
./gradlew :edukate-backend:bootRun --args='--spring.profiles.active=dev,secure,local,notifier'
./gradlew :edukate-notifier:bootRun --args='--spring.profiles.active=dev,secure'
```

### Frontend

```bash
cd edukate-frontend
npm install
npm run dev      # Dev server with HMR (proxies API to localhost:5810)
npm run build    # TypeScript compile + Vite build
npm run lint     # ESLint
```

### Infrastructure

```bash
# Start local dependencies (MongoDB, MinIO, RabbitMQ)
docker compose up -d
```

## Architecture

### Services

| Module             | Port | Role                                                                                           |
|--------------------|------|------------------------------------------------------------------------------------------------|
| `edukate-gateway`  | 5810 | API Gateway — single entry point, routes to backend/notifier, aggregates Swagger at `/swagger` |
| `edukate-backend`  | 5800 | Core business logic — problems, submissions, bundles, file storage                             |
| `edukate-notifier` | 5820 | Async notification delivery via RabbitMQ                                                       |
| `edukate-checker`  | —    | AI-powered problem checking (Spring AI + OpenAI)                                               |
| `edukate-frontend` | 80   | React SPA                                                                                      |

### Shared Libraries

- **`edukate-auth`** — JWT generation/validation and cookie processing; used by gateway and backend
- **`edukate-common`** — Shared domain models, security config, utilities
- **`edukate-messaging`** — RabbitMQ AMQP abstractions; used for backend↔notifier communication
- **`edukate-storage`** — S3/MinIO client abstractions

### Key Architectural Points

- All backend services use **Spring WebFlux** (reactive, non-blocking). Use `Mono`/`Flux` throughout, never block.
- The gateway handles authentication; downstream services trust the forwarded user context.
- Spring profiles control feature flags: `dev` (local env), `secure` (enable security), `local` (MinIO local endpoint),
  `notifier` (enable HTTP notifier bean).
- MongoDB is the data store for all services. Reactive MongoDB (`ReactiveMongoRepository`) is used.
- Async operations between backend and notifier go through RabbitMQ.
- Dependency versions are centralized in `gradle/libs.versions.toml`.
- Custom Gradle plugins live in `gradle/plugins/` (Spring Boot app config, Docker image build).

### Frontend Architecture

- React Router v7 for navigation, TanStack React Query for server state, Axios for HTTP.
- MUI (Material-UI) component library.
- KaTeX for math rendering in problem content.
- Vite proxies `/api` requests to the gateway at `http://localhost:5810`.

## Code Quality

- **Detekt** enforces Kotlin code quality (max issues: 0). Config is in `detekt.yml`.
- **ESLint** with TypeScript strict mode for the frontend. `tsconfig.app.json` has `noUnusedLocals` and
  `noUnusedParameters` enabled.
- Java toolchain is set to Java 23 across all modules.