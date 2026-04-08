# Frontend Testing Guide

## Stack

| Tool                              | Role                                                                                         |
|-----------------------------------|----------------------------------------------------------------------------------------------|
| **Vitest**                        | Test runner — reuses `vite.config.ts` aliases, TypeScript config, env                        |
| **React Testing Library**         | Component testing — queries DOM by role/label, not implementation details                    |
| **`@testing-library/user-event`** | Simulates real user input (full keydown/keyup/change sequences)                              |
| **`@testing-library/jest-dom`**   | DOM-specific `expect` matchers (`.toBeInTheDocument()`, `.toHaveValue()`, etc.)              |
| **MSW**                           | HTTP mocking — intercepts at the network level, full React Query + Axios stack runs for real |
| **`@vitest/coverage-v8`**         | V8-native coverage, zero instrumentation overhead                                            |

## Running tests

```bash
npm run test            # watch mode (re-runs on file change)
npm run test:run        # single run, no watch
npm run test:coverage   # single run + lcov/html coverage report → coverage/
```

Coverage HTML report: open `coverage/index.html` in a browser after `test:coverage`.

## File conventions

| Pattern               | Purpose                                                                 |
|-----------------------|-------------------------------------------------------------------------|
| `src/**/*.test.ts`    | Pure logic tests (no React)                                             |
| `src/**/*.test.tsx`   | Component / hook tests                                                  |
| `src/test/setup.ts`   | Global setup — extends `expect` with jest-dom matchers                  |
| `src/test/server.ts`  | MSW server — shared across all component/hook tests                     |
| `src/test/render.tsx` | Custom `render()` helper — wraps with QueryClient, Router, AuthProvider |

## Writing a test

### Pure function (no setup needed)

```ts
// src/shared/utils/something.test.ts
import {myFn} from "./something";

describe("myFn", () => {
    it("returns X for input Y", () => {
        expect(myFn("Y")).toBe("X");
    });
});
```

### Component (needs providers)

```tsx
// src/features/auth/components/SignInForm.test.tsx
import {render, screen} from "@/test/render";    // custom render with providers
import userEvent from "@testing-library/user-event";
import {server} from "@/test/server";
import {http, HttpResponse} from "msw";
import {SignInForm} from "./SignInForm";

it("shows an error when credentials are wrong", async () => {
    server.use(
        http.post("/api/auth/sign-in", () => HttpResponse.json({message: "Unauthorized"}, {status: 401})),
    );
    render(<SignInForm onSignInSuccess={() => {
    }}/>);
    await userEvent.type(screen.getByLabelText("Username"), "alice");
    await userEvent.type(screen.getByLabelText("Password"), "wrong");
    await userEvent.click(screen.getByRole("button", {name: "Sign In"}));
    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
});
```

### Hook (needs QueryClient wrapper)

```tsx
import {renderHook, waitFor} from "@testing-library/react";
import {createWrapper} from "@/test/render";
import {useProblemRequest} from "@/features/problems/api";

it("returns problem data", async () => {
    const {result} = renderHook(() => useProblemRequest("prob-1"), {wrapper: createWrapper()});
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.name).toBe("prob-1");
});
```

---

## Coverage roadmap

Legend: `[ ]` todo · `[~]` in progress · `[x]` done · **L** light (≤30 min) · **M** medium (1–2 h)

### Phase 1 — Pure utilities (no DOM, no network)

| #  | File                             | Status | Effort | Notes                                                                                                                   |
|----|----------------------------------|--------|--------|-------------------------------------------------------------------------------------------------------------------------|
| T1 | `src/shared/utils/validation.ts` | `[x]`  | **L**  | 27 tests — all branches covered                                                                                         |
| T2 | `src/shared/utils/utils.ts`      | `[ ]`  | **L**  | `formatFileSize`, `sizeOf`, `getFirstLetters`, `getColorByStringHash`                                                   |
| T3 | `src/shared/utils/date.ts`       | `[ ]`  | **L**  | `parseDate`, `formatDate`, `formatRelative`, `isUtcIsoString`, `toUtcIso` — use `vi.setSystemTime` for `formatRelative` |

### Phase 2 — MSW infrastructure

| #  | Task                         | Status | Effort | Notes                                                                                                                                                                |
|----|------------------------------|--------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| T4 | Create `src/test/server.ts`  | `[ ]`  | **L**  | MSW `setupServer()` with `beforeAll/afterEach/afterAll` hooks; export `server` for per-test handler overrides                                                        |
| T5 | Create `src/test/render.tsx` | `[ ]`  | **L**  | Custom `render()` that wraps tree with `QueryClientProvider` (fresh client per test), `MemoryRouter`, `AuthProvider`; also export `createWrapper()` for `renderHook` |
| T6 | Seed base MSW handlers       | `[ ]`  | **L**  | Default happy-path handlers: `GET /api/v1/problems`, `GET /api/v1/bundles`, `GET /api/auth/whoami` — used unless a test overrides them with `server.use(...)`        |

### Phase 3 — Form components

| #  | File                                          | Status | Effort | Notes                                                                                                                             |
|----|-----------------------------------------------|--------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| T7 | `src/features/auth/components/SignInForm.tsx` | `[ ]`  | **M**  | Cases: renders fields; disables submit while pending; shows field errors on blur; navigates on success; shows server error on 401 |
| T8 | `src/features/auth/components/SignUpForm.tsx` | `[ ]`  | **M**  | Cases: validates username/email/password rules; clears errors when field becomes valid; success path                              |

### Phase 4 — Hook logic

| #   | File                                                 | Status | Effort | Notes                                                                                                                                                                                                   |
|-----|------------------------------------------------------|--------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| T9  | `src/features/files/hooks/useFileUpload.ts`          | `[ ]`  | **M**  | Cases: adds files; rejects duplicates; rejects oversized; calls `onTempFileUploaded` on upload success; calls `onTempFileDeleted` on remove; latest-value ref (callback identity changes mid-lifecycle) |
| T10 | `src/features/problems/api.ts` — `useProblemRequest` | `[ ]`  | **M**  | Cases: fetches on mount; `enabled: false` when no id; surfaces data correctly                                                                                                                           |
| T11 | `src/features/bundles/api.ts` — `useBundleRequest`   | `[ ]`  | **M**  | Same pattern as T10                                                                                                                                                                                     |

### Phase 5 — CI + coverage gate

| #   | Task                                               | Status | Effort | Notes                                                                                                                 |
|-----|----------------------------------------------------|--------|--------|-----------------------------------------------------------------------------------------------------------------------|
| T12 | Add `test` job to `.github/workflows/frontend.yml` | `[ ]`  | **L**  | Run `npm run test:coverage`; upload `coverage/lcov.info` via `codecov/codecov-action@v4` using `CODECOV_TOKEN` secret |
| T13 | Make `build` job depend on `test`                  | `[ ]`  | **L**  | Add `test` to the `needs:` array in the `build` job                                                                   |
| T14 | Set coverage thresholds in `vitest.config.ts`      | `[ ]`  | **L**  | Start at 60% across all metrics once Phase 3–4 tests exist; raise to 80% once Phase 5 is done                         |

### Phase 6 — Page smoke tests

| #   | File                            | Status | Effort | Notes                                                           |
|-----|---------------------------------|--------|--------|-----------------------------------------------------------------|
| T15 | `src/pages/ProblemListPage.tsx` | `[ ]`  | **M**  | Renders heading; renders table; MSW serves problem list         |
| T16 | `src/pages/BundleListPage.tsx`  | `[ ]`  | **M**  | Renders heading; tab switching works                            |
| T17 | `src/pages/IndexPage.tsx`       | `[ ]`  | **L**  | Renders heading and CTA button; button navigates to `/problems` |

---

## What NOT to test

- **Generated files** (`src/generated/`) — auto-generated from OpenAPI spec; excluded from coverage
- **`src/app/router.tsx`** — route wiring; covered by page smoke tests
- **`src/main.tsx`** — app entry point; excluded from coverage
- **MUI internals** — test your logic, not the library's rendering
- **Snapshot tests** — prefer behaviour assertions; snapshots break on any visual change and give false confidence
