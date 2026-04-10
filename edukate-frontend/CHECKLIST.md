# Frontend Modernisation Checklist

Track progress against the upgrade roadmap. Update this file as steps complete.

Legend: `[ ]` todo · `[~]` in progress · `[x]` done

---

## Modern React Standards

| # | Practice                                                                  | Status | Notes                                                                                      |
|---|---------------------------------------------------------------------------|--------|--------------------------------------------------------------------------------------------|
| — | Feature-colocation structure (`src/features/`)                            | `[x]`  | UPGRADE.md Step 0                                                                          |
| — | React Query key factory (`queryKeys`)                                     | `[x]`  | UPGRADE.md Step 0                                                                          |
| — | No derived state from React Query (`useState` + `useEffect` anti-pattern) | `[x]`  | Fixed in `ProblemComponent`, `BundleCategoryList`, `ResultAccordion`                       |
| — | Auth guards at UI layer, not inside `mutationFn`                          | `[x]`  | Fixed in `bundles.ts`, `files.ts`, `submissions.ts`, `checkResults.ts`, `notifications.ts` |
| — | `enabled` option for lazy queries instead of flag in key                  | `[x]`  | Applied to all auth-gated queries                                                          |
| — | Path aliases (`@/` → `src/`)                                              | `[x]`  | UPGRADE.md Step 2                                                                          |
| — | Type-checked ESLint (`strictTypeChecked`)                                 | `[ ]`  | UPGRADE.md Step 3                                                                          |
| — | Error boundaries on routes                                                | `[x]`  | `ErrorBoundary` wraps `<Outlet>` in `PageSkeleton`; fallback shows error message           |
| — | Lazy-loaded views (`React.lazy`)                                          | `[x]`  | All 9 routes + `ParticlesComponent` lazy-loaded; `Suspense` in `PageSkeleton`              |

## Code Correctness Debt

Specific instances of anti-patterns identified in audit (April 2026).

### React / Hooks

| File                                                                          | Issue                                                                                                                                | Status | Fix                                                                                                                                                                                 |
|-------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `features/auth/components/SignInForm.tsx:18`                                  | `useEffect` watching `mutation.isSuccess` instead of using `onSuccess` callback; `navigate` and `onSignInSuccess` missing from deps  | `[x]`  | Move post-login logic into `mutate(..., { onSuccess })`, delete the `useEffect`                                                                                                     |
| `pages/ProblemPage.tsx` + `features/problems/components/ProblemComponent.tsx` | `onLoaded` callback lifts `problem.status` back up to the parent via `useState`; `ProblemComponent` useEffect missing `onLoaded` dep | `[x]`  | Call `useProblemRequest(id)` directly in `ProblemPage`; remove `onLoaded` prop and the `useEffect` from `ProblemComponent` — React Query cache is shared, no second network request |
| `features/auth/components/SignInForm.tsx:38`                                  | Form validation queries DOM via `document.getElementById` instead of React refs                                                      | `[x]`  | Switched to controlled inputs with `value`/`onChange`/`onBlur`; also fixed in `SignUpForm.tsx`                                                                                      |
| `features/files/hooks/useFileUpload.ts:51`                                    | `useEffect` suppresses exhaustive-deps lint warning; `onTempFileUploaded` is a stale closure                                         | `[x]`  | Store callback in a `useRef` and read `ref.current` inside the effect                                                                                                               |
| `features/files/hooks/useFileUpload.ts:94`                                    | `forEach(async ...)` — `forEach` ignores returned promises; unhandled rejections escape silently                                     | `[x]`  | Replace with `for...of` or drop the `async` keyword (currently harmless because `mutate` is sync)                                                                                   |
| `features/files/hooks/useFileUpload.ts:97`                                    | `try/catch` wrapping `mutate()` is dead code — `mutate` returns `void` and never throws                                              | `[x]`  | Delete the `try/catch`; error handling belongs in `onError` callback only                                                                                                           |
| `shared/components/images/ImageList.tsx:31`                                   | Array index used as React list key                                                                                                   | `[x]`  | Use `imageUrl` string as key — values are unique URLs                                                                                                                               |

### Type Safety / Keys

| File                                                                               | Issue                                                                                            | Status | Fix                                                                                   |
|------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|--------|---------------------------------------------------------------------------------------|
| `features/bundles/api.ts`, `checks/api.ts`, `files/api.ts`, `notifications/api.ts` | `mutationKey` used with no `useMutationState` / persist consumers — pure noise                   | `[x]`  | Removed all `mutationKey` entries; add them back only if `useMutationState` is needed |
| `features/auth/components/SignInForm.tsx:35`                                       | `FormData.get("username") as string` — unsafe cast; `FormData.get()` can return `null` or `File` | `[x]`  | Eliminated by switching to controlled inputs — `FormData` no longer used              |

### Quality

| File                                            | Issue                                                                                   | Status | Fix                                                                    |
|-------------------------------------------------|-----------------------------------------------------------------------------------------|--------|------------------------------------------------------------------------|
| `features/files/hooks/useFileUpload.ts:128,139` | `console.error` in production code; redundant with global `MutationCache.onError` toast | `[x]`  | Delete both `console.error` calls                                      |
| `features/auth/components/SignInForm.tsx:44,51` | Commented-out validator calls (`/* isValidUsername(username) */`) — dead code           | `[x]`  | Forms now use `validate()` from `validation.ts`; dead comments removed |

### Performance

| File                                                                                                               | Issue                                                                                             | Status | Fix                                                                                                                                                                 |
|--------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `features/auth/components/SignInForm.tsx:61`, `features/problems/components/table/ProblemTable.tsx:12`, and others | `sx` style objects defined inside the component function body — new object reference every render | `[x]`  | Moved to module scope in: `ImageList`, `ZoomingImageDialog`, `EdukateTopBar`, `NotificationMenu`, `FileDragAndDrop`, `ProblemTable`, `FileInput`, `MobileFileInput` |

## Type Safety

| # | Practice                                                                      | Status | Notes                                                                                                                                                                 |
|---|-------------------------------------------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| — | Generate types + hooks from OpenAPI spec (`orval` + axios + react-query)      | `[x]`  | Orval configured; `src/generated/{backend,gateway,notifier}.ts` auto-generated; all `api.ts` files use generated functions                                            |
| — | Replace generated DTO re-exports in `features/*/types.ts`; keep UI-only types | `[x]`  | All feature `types.ts` files now re-export generated DTOs; UI-only types kept (`BundleCategory`, `CheckType`, `CheckRequest`, `FileMetadata extends FileMetadataDto`) |

## Bundle & Performance

| # | Practice                                                 | Status | Notes                                                                                       |
|---|----------------------------------------------------------|--------|---------------------------------------------------------------------------------------------|
| — | Remove KaTeX CDN tags from `index.html`                  | `[x]`  | UPGRADE.md Step 4 — CSS imported from npm in `LatexComponent.tsx`                           |
| — | Lazy-load KaTeX (only on problem routes)                 | `[x]`  | `LazyLatexComponent` wraps `React.lazy` + `Suspense`; KaTeX splits into its own async chunk |
| — | Replace `@tsparticles` with CSS animation                | `[ ]`  | PERFORMANCE.md §1 — saves ~200KB — deferred, keeping tsparticles for now                    |
| — | nginx: enable gzip + cache headers for hashed chunks     | `[x]`  | PERFORMANCE.md §4 — gzip level 6, immutable JS/CSS, 1-week fonts, no-store index.html       |
| — | Vite `manualChunks` for vendor splitting                 | `[x]`  | PERFORMANCE.md §3 — vendor-react/mui/query/misc/particles; all chunks under 500KB           |
| — | Bundle analysis with `rollup-plugin-visualizer`          | `[x]`  | `npm run analyze` — opens `dist/stats.html` treemap with gzip sizes                         |
| — | Audit MUI icon imports (named only, no namespace `* as`) | `[x]`  | PERFORMANCE.md §5 — all imports already named, no changes needed                            |
| — | Drop `@uidotdev/usehooks`, inline `useDebounce`          | `[x]`  | PERFORMANCE.md §7 — also fixed debounce bug in `PrefixOptionInput`                          |

## Quality & Testing

| # | Practice                                | Status | Notes                                                                                                               |
|---|-----------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------|
| — | Unit tests: Vitest + RTL + MSW          | `[~]`  | Infra wired (Vitest + jsdom + jest-dom + MSW installed); `validation.ts` 27/27; roadmap in TESTING.md               |
| — | E2E tests: Playwright                   | `[ ]`  | UPGRADE.md Step 8                                                                                                   |
| — | Accessibility: `@axe-core/react` in dev | `[~]`  | Manual audit done; all known violations fixed (see below). Runtime harness pending — see axe-core setup plan below. |
| — | Frontend CI workflow                    | `[x]`  | `.github/workflows/frontend.yml` — format, lint+SARIF, typecheck, build                                             |

### axe-core setup plan (pending)

When ready to wire up runtime a11y feedback in dev:

1. `npm install --save-dev @axe-core/react`
2. In `src/main.tsx`, before `ReactDOM.createRoot(...)`:
    ```ts
    if (import.meta.env.DEV) {
        const { default: axe } = await import("@axe-core/react");
        const React = (await import("react")).default;
        const ReactDOM = (await import("react-dom")).default;
        axe(React, ReactDOM, 1000);
    }
    ```
3. Open the app in dev, open DevTools console — axe logs violations as `console.error` with selector + description.
4. Mark the checklist row `[x]` once the harness is running and no violations are logged.

Note: axe-core catches runtime issues (dynamic content, focus management after navigation) that static analysis misses.

## Mobile & PWA

| # | Practice                                     | Status | Notes                                                   |
|---|----------------------------------------------|--------|---------------------------------------------------------|
| — | PWA service worker (`vite-plugin-pwa`)       | `[ ]`  | DECISIONS.md — half a day                               |
| — | `site.webmanifest` colors aligned with theme | `[x]`  | `theme_color: '#851691'`, `background_color: '#f9ebd9'` |
| — | `ProblemTable` → card list on mobile         | `[ ]`  | DECISIONS.md responsive audit                           |
| — | `BundleComponent` sidebar audit at 375px     | `[ ]`  | DECISIONS.md responsive audit                           |
| — | `PageSkeleton` top padding reduced on `xs`   | `[x]`  | `pt: { xs: "80px", md: "120px" }`                       |
| — | React Native                                 | `[ ]`  | Deferred indefinitely — DECISIONS.md                    |
