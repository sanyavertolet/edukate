# Frontend Modernisation Checklist

Track progress against the upgrade roadmap. Update this file as steps complete.

Legend: `[ ]` todo · `[~]` in progress · `[x]` done

---

## Modern React Standards

| #   | Practice                                                                  | Status | Notes                                                                                      |
| --- | ------------------------------------------------------------------------- | ------ | ------------------------------------------------------------------------------------------ |
| —   | Feature-colocation structure (`src/features/`)                            | `[x]`  | UPGRADE.md Step 0                                                                          |
| —   | React Query key factory (`queryKeys`)                                     | `[x]`  | UPGRADE.md Step 0                                                                          |
| —   | No derived state from React Query (`useState` + `useEffect` anti-pattern) | `[x]`  | Fixed in `ProblemComponent`, `BundleCategoryList`, `ResultAccordion`                       |
| —   | Auth guards at UI layer, not inside `mutationFn`                          | `[x]`  | Fixed in `bundles.ts`, `files.ts`, `submissions.ts`, `checkResults.ts`, `notifications.ts` |
| —   | `enabled` option for lazy queries instead of flag in key                  | `[x]`  | Applied to all auth-gated queries                                                          |
| —   | Path aliases (`@/` → `src/`)                                              | `[x]`  | UPGRADE.md Step 2                                                                          |
| —   | Type-checked ESLint (`strictTypeChecked`)                                 | `[ ]`  | UPGRADE.md Step 3                                                                          |
| —   | Error boundaries on routes                                                | `[ ]`  | UPGRADE.md Step 5                                                                          |
| —   | Lazy-loaded views (`React.lazy`)                                          | `[ ]`  | UPGRADE.md Step 6                                                                          |

## Type Safety

| #   | Practice                                                          | Status | Notes             |
| --- | ----------------------------------------------------------------- | ------ | ----------------- |
| —   | Generate types from OpenAPI spec (`openapi-typescript`)           | `[ ]`  | UPGRADE.md Step 1 |
| —   | Delete hand-written `src/types/` once generated types are adopted | `[ ]`  | After Step 1      |

## Bundle & Performance

| #   | Practice                                                 | Status | Notes                                                                                       |
| --- | -------------------------------------------------------- | ------ | ------------------------------------------------------------------------------------------- |
| —   | Remove KaTeX CDN tags from `index.html`                  | `[x]`  | UPGRADE.md Step 4 — CSS imported from npm in `LatexComponent.tsx`                           |
| —   | Lazy-load KaTeX (only on problem routes)                 | `[x]`  | `LazyLatexComponent` wraps `React.lazy` + `Suspense`; KaTeX splits into its own async chunk |
| —   | Replace `@tsparticles` with CSS animation                | `[ ]`  | PERFORMANCE.md §1 — saves ~200KB                                                            |
| —   | nginx: enable gzip + cache headers for hashed chunks     | `[ ]`  | PERFORMANCE.md §4                                                                           |
| —   | Vite `manualChunks` for vendor splitting                 | `[ ]`  | PERFORMANCE.md §3                                                                           |
| —   | Bundle analysis with `rollup-plugin-visualizer`          | `[ ]`  | UPGRADE.md Step 9                                                                           |
| —   | Audit MUI icon imports (named only, no namespace `* as`) | `[ ]`  | PERFORMANCE.md §5                                                                           |

## Quality & Testing

| #   | Practice                                | Status | Notes                                                                   |
| --- | --------------------------------------- | ------ | ----------------------------------------------------------------------- |
| —   | Unit tests: Vitest + RTL + MSW          | `[ ]`  | UPGRADE.md Step 7, TESTING.md                                           |
| —   | E2E tests: Playwright                   | `[ ]`  | UPGRADE.md Step 8                                                       |
| —   | Accessibility: `@axe-core/react` in dev | `[ ]`  | UPGRADE.md Step 10                                                      |
| —   | Frontend CI workflow                    | `[x]`  | `.github/workflows/frontend.yml` — format, lint+SARIF, typecheck, build |

## Mobile & PWA

| #   | Practice                                     | Status | Notes                                                   |
| --- | -------------------------------------------- | ------ | ------------------------------------------------------- |
| —   | PWA service worker (`vite-plugin-pwa`)       | `[ ]`  | DECISIONS.md — half a day                               |
| —   | `site.webmanifest` colors aligned with theme | `[ ]`  | `theme_color: '#851691'`, `background_color: '#f9ebd9'` |
| —   | `ProblemTable` → card list on mobile         | `[ ]`  | DECISIONS.md responsive audit                           |
| —   | `BundleComponent` sidebar audit at 375px     | `[ ]`  | DECISIONS.md responsive audit                           |
| —   | `PageSkeleton` top padding reduced on `xs`   | `[ ]`  | 120px eats viewport on phones                           |
| —   | React Native                                 | `[ ]`  | Deferred indefinitely — DECISIONS.md                    |
