# Frontend Upgrade Roadmap

Ordered list of self-contained improvement steps. Each step is independently shippable.
**Steps 0-4 are foundational — do them in order.** Steps 5+ can be done in any order.

---

## Step 0 — Project structure: feature-colocation

**Problem:** The current layout groups files by _technology type_ (`components/`, `hooks/`,
`http/requests/`). As the codebase grows, this means a single user action (e.g. submitting
a solution) requires navigating across 4-5 unrelated directories. It also makes it easy to
accidentally couple features through shared imports.

**Modern standard:** The React community has converged on **feature-colocation** — everything
belonging to a domain lives together. The reference architecture is
[Bulletproof React](https://github.com/alan2207/bulletproof-react) and TkDodo's React Query
blog. The React team's own docs now recommend organizing "by route/feature, not file type".

### Target structure

```
src/
├── app/                        # App-level wiring — not a feature
│   ├── providers.tsx           # All context providers composed in one place
│   ├── router.tsx              # Route definitions (moved from Router.tsx)
│   └── App.tsx
│
├── features/                   # Self-contained domain modules
│   ├── auth/
│   │   ├── api.ts              # useWhoamiQuery, useSignInMutation, useSignOutMutation
│   │   ├── components/
│   │   │   ├── AuthRequired.tsx
│   │   │   ├── SignInForm.tsx   # was SignInComponent.tsx
│   │   │   └── SignUpForm.tsx
│   │   ├── context.tsx         # AuthProvider + useAuthContext (was AuthContextProvider)
│   │   └── types.ts            # User, Role, UserNameWithRole
│   │
│   ├── problems/
│   │   ├── api.ts              # useProblemListRequest, useProblemRequest, …
│   │   ├── components/
│   │   │   ├── ProblemCard.tsx
│   │   │   ├── SolutionCard.tsx
│   │   │   ├── SubmissionsCard.tsx
│   │   │   ├── ProblemStatusIcon.tsx
│   │   │   ├── SubtasksComponent.tsx
│   │   │   ├── ResultAccordion.tsx
│   │   │   └── table/
│   │   │       ├── ProblemTable.tsx
│   │   │       ├── ProblemTablePagination.tsx
│   │   │       ├── ProblemTableRows.tsx
│   │   │       ├── ProblemTableToolbar.tsx
│   │   │       └── RandomProblemButton.tsx
│   │   ├── hooks/
│   │   │   └── useProblemTableParams.ts
│   │   └── types.ts            # Problem, ProblemMetadata, ProblemStatus, Result
│   │
│   ├── bundles/
│   │   ├── api.ts
│   │   ├── components/
│   │   │   ├── BundleCard.tsx
│   │   │   ├── BundleComponent.tsx
│   │   │   ├── BundleJoinForm.tsx
│   │   │   ├── BundleProblemSelector.tsx
│   │   │   ├── BundleDescriptionTab.tsx
│   │   │   ├── BundleSettingsTab.tsx
│   │   │   ├── BundleInfoCards.tsx
│   │   │   ├── BundleIndexCard.tsx
│   │   │   ├── BundleCategoryList.tsx
│   │   │   ├── PublicityIcon.tsx
│   │   │   └── user/
│   │   │       ├── BundleUserManagement.tsx
│   │   │       ├── InvitedUsersManagement.tsx
│   │   │       └── UserSearchInput.tsx
│   │   └── types.ts            # Bundle, BundleMetadata, BundleCategory, CreateBundleRequest
│   │
│   ├── submissions/
│   │   ├── api.ts
│   │   ├── components/
│   │   │   ├── SubmissionComponent.tsx
│   │   │   ├── SubmissionList.tsx
│   │   │   └── SubmissionListItems.tsx
│   │   └── types.ts            # Submission, CreateSubmissionRequest
│   │
│   ├── notifications/
│   │   ├── api.ts
│   │   ├── components/
│   │   │   ├── NotificationButton.tsx
│   │   │   ├── NotificationMenu.tsx
│   │   │   ├── SimpleNotification.tsx
│   │   │   ├── InviteNotification.tsx
│   │   │   ├── CheckedNotification.tsx
│   │   │   └── InvitationDialog.tsx
│   │   └── types.ts            # BaseNotification, NotificationType, NotificationStatistics
│   │
│   ├── checks/
│   │   ├── api.ts              # useRequestCheckMutation, useCheckResultsRequest
│   │   ├── components/
│   │   │   └── CheckResultInfoList.tsx
│   │   └── types.ts            # CheckRequest, CheckResult, CheckResultInfo
│   │
│   └── files/
│       ├── api.ts              # useTempFileListQuery, useUploadFileMutation, …
│       ├── components/
│       │   ├── FileUploadComponent.tsx
│       │   ├── FileDragAndDrop.tsx
│       │   ├── FileInputComponent.tsx
│       │   ├── FilePreviewDialog.tsx
│       │   ├── FileStatusIcon.tsx
│       │   ├── MobileFileInput.tsx
│       │   └── MobileFileUpload.tsx
│       ├── hooks/
│       │   ├── useFileUpload.ts
│       │   └── useFileStatsDisplayValues.ts
│       └── types.ts            # FileMetadata, ExtendedFile
│
├── pages/                      # Thin route components — compose features, no logic
│   ├── IndexPage.tsx           # was IndexView.tsx
│   ├── ProblemListPage.tsx
│   ├── ProblemPage.tsx
│   ├── BundleListPage.tsx
│   ├── BundlePage.tsx
│   ├── BundleCreationPage.tsx
│   ├── SubmissionPage.tsx
│   ├── SignInPage.tsx
│   └── SignUpPage.tsx
│
├── shared/                     # Code shared across multiple features
│   ├── components/
│   │   ├── layout/
│   │   │   ├── PageSkeleton.tsx
│   │   │   └── topbar/
│   │   │       ├── EdukateTopBar.tsx
│   │   │       ├── SiteMark.tsx
│   │   │       ├── UserMenu.tsx
│   │   │       ├── NavigationElement.tsx
│   │   │       └── MobileDrawer.tsx
│   │   ├── ConditionalTooltip.tsx
│   │   ├── OptionPicker.tsx
│   │   ├── PrefixOptionInput.tsx
│   │   ├── TagChip.tsx
│   │   ├── LatexComponent.tsx
│   │   ├── Particles.tsx
│   │   └── images/
│   │       ├── ImageList.tsx
│   │       └── ZoomingImageDialog.tsx
│   ├── context/
│   │   ├── ThemeContext.tsx    # was components/themes/ThemeContextProvider
│   │   └── DeviceContext.tsx   # was components/topbar/DeviceContextProvider
│   ├── hooks/                  # Hooks used by >1 feature
│   └── utils/
│       ├── date.ts
│       ├── utils.ts
│       └── validation.ts
│
├── lib/                        # Third-party infrastructure wrappers
│   ├── axios.ts                # Axios instance (was http/client.ts)
│   ├── query-client.ts         # React Query client config (was http/queryClient.ts)
│   └── query-keys.ts           # Centralized React Query key factory (new — see below)
│
└── types/
    └── api.d.ts                # Auto-generated by openapi-typescript (Step 1)
```

### What moves where (migration map)

| Current path                                      | Target path                                                    |
| ------------------------------------------------- | -------------------------------------------------------------- |
| `src/Router.tsx`                                  | `src/app/router.tsx`                                           |
| `src/App.tsx`                                     | `src/app/App.tsx` (providers extracted to `providers.tsx`)     |
| `src/components/auth/AuthContextProvider.tsx`     | `src/features/auth/context.tsx`                                |
| `src/components/auth/AuthRequired.tsx`            | `src/features/auth/components/AuthRequired.tsx`                |
| `src/components/auth/SignInComponent.tsx`         | `src/features/auth/components/SignInForm.tsx`                  |
| `src/http/requests/auth.ts`                       | `src/features/auth/api.ts`                                     |
| `src/http/requests/problems.ts`                   | `src/features/problems/api.ts`                                 |
| `src/http/requests/bundles.ts`                    | `src/features/bundles/api.ts`                                  |
| `src/http/requests/submissions.ts`                | `src/features/submissions/api.ts`                              |
| `src/http/requests/notifications.ts`              | `src/features/notifications/api.ts`                            |
| `src/http/requests/checkResults.ts`               | `src/features/checks/api.ts`                                   |
| `src/http/requests/files.ts`                      | `src/features/files/api.ts`                                    |
| `src/http/client.ts`                              | `src/lib/axios.ts`                                             |
| `src/http/queryClient.ts`                         | `src/lib/query-client.ts`                                      |
| `src/components/themes/ThemeContextProvider.tsx`  | `src/shared/context/ThemeContext.tsx`                          |
| `src/components/topbar/DeviceContextProvider.tsx` | `src/shared/context/DeviceContext.tsx`                         |
| `src/views/*.tsx`                                 | `src/pages/*.tsx`                                              |
| `src/types/**`                                    | `src/features/<domain>/types.ts` (until Step 1 generates them) |
| `src/utils/**`                                    | `src/shared/utils/`                                            |
| `src/hooks/useProblemTableParams.ts`              | `src/features/problems/hooks/`                                 |
| `src/hooks/useFileUpload.ts`                      | `src/features/files/hooks/`                                    |
| `src/components/basic/**`                         | `src/shared/components/`                                       |
| `src/components/images/**`                        | `src/shared/components/images/`                                |

### Add a React Query key factory (`src/lib/query-keys.ts`)

Currently query keys are string literals scattered across files. A key factory is the
[TkDodo-recommended](https://tkdodo.eu/blog/effective-react-query-keys) pattern:

```ts
export const queryKeys = {
    problems: {
        all: ["problems"] as const,
        list: (page: number, size: number) => ["problems", "list", page, size] as const,
        detail: (id: string) => ["problems", "detail", id] as const,
        count: ["problems", "count"] as const,
        random: ["problems", "random"] as const,
        result: (id: string) => ["problems", "result", id] as const,
    },
    bundles: {
        all: ["bundles"] as const,
        detail: (code: string) => ["bundles", "detail", code] as const,
        list: (category: string) => ["bundles", "list", category] as const,
        users: (code: string) => ["bundles", "users", code] as const,
        invitedUsers: (code: string) => ["bundles", "invited-users", code] as const,
    },
    submissions: {
        all: ["submissions"] as const,
        detail: (id: string) => ["submissions", "detail", id] as const,
        byProblem: (problemId: string) => ["submissions", "by-problem", problemId] as const,
    },
    notifications: {
        all: ["notifications"] as const,
        stats: ["notifications", "stats"] as const,
    },
    auth: {
        whoami: ["auth", "whoami"] as const,
    },
};
```

This means `queryClient.invalidateQueries({ queryKey: queryKeys.bundles.all })` invalidates
every bundle query at once, and typos become compile errors.

### Anti-patterns to fix during migration

These exist in the current code and should be corrected as files are moved:

**1. Derived state from React Query (`ProblemComponent.tsx:16-25`)**

```tsx
// ❌ current — useEffect copies query data to local state
const { data, isLoading, error } = useProblemRequest(problemId, shouldRefresh);
const [problem, setProblem] = useState<Problem>();
useEffect(() => {
    if (data && !isLoading && !error) {
        setProblem(data);
    }
}, [data, isLoading, error]);

// ✅ fix — use data directly; React Query is already reactive
const { data: problem, isLoading, error } = useProblemRequest(problemId);
```

**2. Authorization checks inside `mutationFn` (`bundles.ts`, `files.ts`, `submissions.ts`)**

```ts
// ❌ current — silent no-op when not authorized
mutationFn: async (bundleCode: string) => {
  if (!isAuthorized) return null
  // ...
}

// ✅ fix — disable the UI element; keep mutationFn pure
<Button disabled={!isAuthorized} onClick={() => joinMutation.mutate(code)}>Join</Button>
```

**3. `user` in problem list query key (`problems.ts:12`)**

```ts
// ❌ current — entire User object in query key; deep equality breaks caching
queryKey: ["problemList", page, size, user];

// ✅ fix — use a stable scalar
queryKey: queryKeys.problems.list(page, size);
// Auth state changes trigger re-fetch via React Query's refetchOnMount, not key changes
```

### Migration strategy

This is a mechanical move — no logic changes. Do it feature-by-feature over multiple PRs:

1. `lib/` — move axios + queryClient, add query-keys.ts (no component changes, low risk)
2. `auth` feature + pages (signin, signup)
3. `problems` feature + problem pages
4. `bundles` feature + bundle pages
5. `submissions` + `checks` features
6. `notifications` + `files` features
7. `shared/` cleanup (layout, contexts, basic components)
8. Delete `src/components/`, `src/http/`, `src/views/`, `src/types/`, `src/hooks/` (now empty)

---

## Step 1 — Type alignment: generate frontend types from backend OpenAPI spec

**Problem:** `src/types/` is hand-maintained and can silently drift from backend DTOs.

**Solution:** `openapi-typescript` reads the live OpenAPI JSON from the gateway and
generates a typed `.d.ts` file. No runtime cost.

### Install

```bash
npm install --save-dev openapi-typescript
```

### Add script to `package.json`

```json
"scripts": {
  "generate:types": "openapi-typescript http://localhost:5810/v3/api-docs -o src/types/api.d.ts"
}
```

The gateway serves the aggregated spec at `http://localhost:5810/v3/api-docs`
(springdoc-openapi default). If the gateway aggregates sub-service specs differently,
check the `/swagger` UI to find the exact JSON URL.

### Usage pattern

`openapi-typescript` emits a `paths` + `components` namespace. To extract component schemas:

```ts
// src/types/api.d.ts is auto-generated — never edit by hand
import type { components } from "./api";

export type Problem = components["schemas"]["Problem"];
export type Submission = components["schemas"]["Submission"];
// ... add re-exports for every schema you use
```

Put re-exports in `src/types/index.ts` — components still import from `src/types`, but
the backing source is now generated.

### Migration

1. Run `npm run generate:types` with backend running.
2. Compare generated shapes against hand-written types in `src/types/`.
3. Replace hand-written types with the generated ones incrementally (one domain at a time).
4. Delete hand-written files once all consumers are migrated.
5. Add `npm run generate:types` to the CI workflow after the backend build step
   (or at minimum document "run this after backend model changes").

### What to watch for

- Kotlin `data class` fields serialised with Jackson: nullable fields become `T | null`,
  `Optional<T>` fields may become `T | undefined`. Verify each shape carefully.
- Enums: springdoc may emit them as `string` with `enum:` constraint or as a `$ref`.
  Adjust re-export aliases if needed.

---

## Step 2 — Path aliases

**Problem:** Deep relative imports (`../../../../http/client`) hurt readability and
break on refactors.

**Solution:** Configure `@/` as an alias for `src/`.

### `tsconfig.app.json`

```json
{
    "compilerOptions": {
        "paths": {
            "@/*": ["./src/*"]
        }
    }
}
```

### `vite.config.ts`

```ts
import path from "path";

export default defineConfig({
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    // ... rest of config
});
```

Add `@types/node` as a devDependency (`npm install --save-dev @types/node`) for the
`path` import to typecheck.

### Migration

Mechanical find-and-replace. Search for `from '../../` / `from '../../../` etc. and
replace with `from '@/`. Do this directory by directory to keep diffs reviewable.

---

## Step 3 — Stricter ESLint (type-checked rules)

**Problem:** Current ESLint config uses `tseslint.configs.recommended` (no type information),
missing a large class of type-level bugs.

**Solution:** Enable type-checked rules and add `eslint-plugin-react`.

### Install

```bash
npm install --save-dev eslint-plugin-react
```

### `eslint.config.js` replacement

```js
import js from "@eslint/js";
import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import react from "eslint-plugin-react";
import tseslint from "typescript-eslint";

export default tseslint.config(
    { ignores: ["dist", "src/types/api.d.ts"] }, // never lint generated types
    {
        extends: [
            js.configs.recommended,
            ...tseslint.configs.strictTypeChecked, // upgrade from recommended
            ...tseslint.configs.stylisticTypeChecked,
        ],
        files: ["**/*.{ts,tsx}"],
        languageOptions: {
            ecmaVersion: 2020,
            globals: globals.browser,
            parserOptions: {
                project: ["./tsconfig.app.json"],
                tsconfigRootDir: import.meta.dirname,
            },
        },
        settings: { react: { version: "detect" } },
        plugins: {
            "react-hooks": reactHooks,
            "react-refresh": reactRefresh,
            react,
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            ...react.configs.recommended.rules,
            ...react.configs["jsx-runtime"].rules,
            "react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
            "no-console": ["warn", { allow: ["warn", "error"] }],
        },
    },
);
```

**Note:** `strictTypeChecked` will produce many new violations on the existing codebase.
Fix them incrementally — start with `recommendedTypeChecked` if the initial noise is
too large to handle in one PR.

---

## Step 4 — KaTeX: move from CDN to npm package

**Problem:** `index.html` loads KaTeX from a CDN (`jsDelivr`). This is a production
reliability risk (CDN availability) and blocks CSP headers.

**Solution:** Import KaTeX and its CSS from npm.

### Install

KaTeX is already in `dependencies` (`"katex": "^0.16.21"`). Only the CDN tags in
`index.html` need removing.

### Changes

1. Remove the three `<script>` and `<link>` tags for KaTeX from `index.html`.
2. In `LatexComponent.tsx` (or a new `src/lib/katex.ts` init module), import:

```ts
import "katex/dist/katex.min.css";
import renderMathInElement from "katex/contrib/auto-render";
```

3. Remove the `window.renderMathInElement` call (currently relies on the global injected
   by the CDN script). Use the imported function directly.
4. Declare the KaTeX contrib type if missing:
    ```ts
    // src/types/katex-auto-render.d.ts
    declare module "katex/contrib/auto-render" {
        function renderMathInElement(element: Element, options?: object): void;
        export default renderMathInElement;
    }
    ```

---

## Step 5 — Error boundaries

**Problem:** Any unhandled render error in a component tree crashes the entire app with
a blank white screen — no user-facing message, no recovery path.

**Solution:** Wrap route-level views in an `ErrorBoundary` component.

### Install

```bash
npm install react-error-boundary
```

### Usage

In `Router.tsx`, wrap each route element:

```tsx
import { ErrorBoundary } from "react-error-boundary";

function FallbackComponent({ error }: { error: Error }) {
    return (
        <Box>
            <Typography>Something went wrong.</Typography>
            <Typography variant="caption">{error.message}</Typography>
        </Box>
    );
}

// wrap each route:
element: <ErrorBoundary FallbackComponent={FallbackComponent}>
    <ProblemView />
</ErrorBoundary>;
```

Or wrap the entire `<Outlet>` in `PageSkeleton.tsx` for a single catch-all.

---

## Step 6 — Code splitting (lazy-load views)

**Problem:** All views are bundled in a single JS chunk. Every user downloads code for
routes they may never visit.

**Solution:** `React.lazy` + `Suspense` on view imports.

### In `Router.tsx`

```tsx
import { lazy, Suspense } from 'react'

const ProblemListView = lazy(() => import('@/views/ProblemListView'))
const ProblemView = lazy(() => import('@/views/ProblemView'))
// ... all views

// wrap in Suspense where the <Outlet> is (PageSkeleton.tsx):
<Suspense fallback={<CircularProgress />}>
  <Outlet />
</Suspense>
```

**Expected impact:** Initial bundle should drop noticeably; each view becomes its own
async chunk loaded on navigation.

---

## Step 7 — Unit testing (Vitest + RTL + MSW)

See `TESTING.md` for the full setup guide.

**Summary of what to test first (highest value / lowest effort):**

1. `src/utils/validation.ts` — pure functions, zero setup needed
2. `src/utils/date.ts` — pure functions
3. `src/utils/utils.ts` (`formatFileSize`, `getColorByStringHash`) — pure functions
4. `AuthRequired` component — redirect logic
5. `useFileUpload` hook — state machine (add/remove/validate files)
6. Individual form components (sign-in, sign-up) — submit + validation feedback

---

## Step 8 — E2E testing (Playwright)

**Install**

```bash
npm init playwright@latest
```

Choose: TypeScript, `e2e/` directory, Chromium only to start.

**Test scenarios to start with:**

1. Sign up → sign in → see problem list
2. View a problem (unauthenticated)
3. Attempt to access `/bundles/new` unauthenticated → redirected to `/sign-in`
4. Sign in → submit a solution → see submission in list

**Dev environment note:** E2E tests need the full stack running (gateway, backend, mongo).
Use `docker compose up -d` + start all services before running Playwright.
Consider a separate `npm run test:e2e` script targeting `http://localhost:80`.

---

## Step 9 — Bundle analysis

After code splitting (Step 6) is in place, run:

```bash
npm install --save-dev rollup-plugin-visualizer
```

In `vite.config.ts`:

```ts
import { visualizer } from "rollup-plugin-visualizer";

plugins: [react(), visualizer({ open: true, filename: "dist/stats.html" })];
```

Run `npm run build` — a browser window opens with a treemap of the bundle. Look for:

- Unexpectedly large dependencies (MUI icons is a common offender if imported broadly)
- Duplicated polyfills
- KaTeX assets (should be in a separate chunk after Step 4)

**Known likely finding:** `@mui/icons-material` — ensure all icon imports are named
(`import { Add } from '@mui/icons-material'`), not namespace imports
(`import * as Icons from '@mui/icons-material'`). Vite tree-shakes named imports.

---

## Step 10 — Accessibility audit

**Install**

```bash
npm install --save-dev @axe-core/react
```

In `main.tsx` (dev only):

```ts
if (import.meta.env.DEV) {
    const axe = await import("@axe-core/react");
    const ReactDOM = await import("react-dom");
    axe.default(React, ReactDOM, 1000);
}
```

This logs accessibility violations to the browser console during development.

**Known areas to check:**

- `ProblemTable` — ensure column headers have correct `scope` attributes
- Icon-only buttons (notifications bell, theme toggle) — need `aria-label`
- `LatexComponent` — rendered math needs `aria-label` or `role="math"`
- Color-only status indicators (`ProblemStatusIcon`) — need text alternative

---

## Step 11 — Frontend CI workflow

**Create `.github/workflows/frontend.yml`:**

```yaml
name: Frontend

on:
    push:
        branches: [master]
    pull_request:
        paths:
            - "edukate-frontend/**"

jobs:
    frontend:
        runs-on: ubuntu-latest
        defaults:
            run:
                working-directory: edukate-frontend
        steps:
            - uses: actions/checkout@v4
            - uses: actions/setup-node@v4
              with:
                  node-version: "22"
                  cache: npm
                  cache-dependency-path: edukate-frontend/package-lock.json
            - run: npm ci
            - run: npm run lint
            - run: npm run build
```

Add `npm run test` to this once Step 7 is done.

---

## Deferred / optional

- **i18n** — `react-i18next` if multi-language support is needed
- **Storybook** — useful once component library stabilises; overkill now
- **PWA / service worker** — `vite-plugin-pwa` if offline support is desired
- **React DevTools Profiler** — run during development to find unnecessary re-renders;
  fix with `React.memo`, `useMemo`, `useCallback` only where profiler shows actual cost
