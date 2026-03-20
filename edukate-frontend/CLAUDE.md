# edukate-frontend

React 19 SPA. Communicates with the gateway at `/api` (proxied to `http://localhost:5810` in dev).

## Stack

- **React 19** + **React Router v7** (file-based routing in `Router.tsx`)
- **TanStack React Query v5** — all server state, cache invalidation
- **Axios** — HTTP client (`src/http/client.ts`, `baseURL: window.location.origin`, `credentials: true`)
- **MUI v6** — component library
- **KaTeX** — math rendering in problem content
- **TypeScript ~5.7** — `noUnusedLocals` + `noUnusedParameters` enforced

## Directory Layout

```
src/
├── components/     # Reusable UI (auth, bundle, problem, submission, notifications, check, files, topbar, themes)
├── views/          # Page-level components (one per route)
├── http/
│   ├── client.ts       # Axios instance
│   ├── queryClient.ts  # React Query config
│   └── requests/       # API wrappers (auth, problems, bundles, submissions, notifications, checkResults, files)
├── types/          # TypeScript type definitions (mirrors backend domain)
├── hooks/          # Custom React hooks
├── utils/          # validation.ts, date.ts, utils.ts
└── theme/          # MUI theme config
```

## Routes

| Path | View | Auth required |
|---|---|---|
| `/` | `IndexView` | No |
| `/problems` | `ProblemListView` | No |
| `/problems/:id` | `ProblemView` | No |
| `/sign-in` | `SignInView` | No |
| `/sign-up` | `SignUpView` | No |
| `/bundles` | `BundleListView` | No |
| `/bundles/new` | `BundleCreationView` | Yes |
| `/bundles/:code` | `BundleView` | Yes |
| `/submissions/:id` | `SubmissionView` | Yes |

## Key Patterns

- `AuthContextProvider` wraps the app; `AuthRequired` wraps protected routes
- `DeviceContextProvider` — responsive breakpoints
- `ThemeContextProvider` — dark/light mode
- `react-cookie` — reads the JWT cookie set by the gateway
- React Query keys mirror the REST resource paths

## Testing Notes

- Use **Vitest** (compatible with Vite) + **React Testing Library**
- Mock Axios with `msw` (Mock Service Worker) or `axios-mock-adapter`
- Test React Query hooks by wrapping with `QueryClientProvider` using a fresh client per test
- Test `AuthRequired` — unauthenticated renders redirect to `/sign-in`
- Test form validation logic in `src/utils/validation.ts` as plain unit tests (no React needed)
- Snapshot tests are discouraged — prefer behaviour assertions
