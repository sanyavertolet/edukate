# Architectural Decisions

Record of technology choices, alternatives considered, and reasoning.

---

## Mobile strategy: responsive web + PWA

**Decision:** Invest in a solid responsive web experience and ship it as a PWA.
React Native is deferred indefinitely.

### Why not React Native (for now)

React Native requires a full rewrite of every UI component — MUI doesn't run in RN, React
Router doesn't run in RN, cookies don't work in RN. The business logic layer (React Query
hooks, types, utils) transfers, but that's maybe 20% of the codebase. The effort is
3-5 weeks minimum for feature parity, and it creates a second codebase to maintain forever.

The web app already runs in mobile browsers. Fixing the **responsive layout** and adding
a **PWA manifest + service worker** gets Edukate onto home screens in days, not weeks.

### PWA

**Status:** Partially done — `site.webmanifest` and icons already exist in `public/`.

**What's missing:**

```bash
npm install --save-dev vite-plugin-pwa
```

In `vite.config.ts`:

```ts
import { VitePWA } from "vite-plugin-pwa";

plugins: [
    react(),
    VitePWA({
        registerType: "autoUpdate",
        manifest: {
            name: "Edukate",
            short_name: "Edukate",
            theme_color: "#851691",
            background_color: "#f9ebd9",
            display: "standalone",
            icons: [
                { src: "/android-chrome-192x192.png", sizes: "192x192", type: "image/png" },
                { src: "/android-chrome-512x512.png", sizes: "512x512", type: "image/png" },
            ],
        },
        workbox: {
            // Cache static assets; navigate to index.html for SPA routes
            navigateFallback: "/index.html",
            globPatterns: ["**/*.{js,css,html,ico,png,svg,woff2}"],
        },
    }),
];
```

**Effort:** half a day. Gives users "Add to Home Screen" on Android and iOS 16.4+.

### Mobile responsive improvements

The app uses MUI `Container maxWidth="lg"` and MUI breakpoints throughout, so basic
responsiveness is already there. Specific areas to audit and improve:

| Area                   | Issue                                                     | Fix                                                          |
| ---------------------- | --------------------------------------------------------- | ------------------------------------------------------------ |
| `ProblemTable`         | Full table on small screens is cramped                    | Collapse to card list on `xs`/`sm`                           |
| `BundleComponent`      | Sidebar + content grid may stack awkwardly                | Audit at 375px width                                         |
| `EdukateTopBar`        | Mobile drawer exists — verify all nav items are reachable | Smoke test on 375px                                          |
| `FileUploadComponent`  | Desktop drag-and-drop is primary                          | Mobile variant exists — verify it renders correctly on touch |
| `PageSkeleton` padding | `pt: "120px"` on mobile eats significant viewport height  | Reduce on `xs` breakpoint                                    |

### When to revisit React Native

If native-only capabilities become requirements: camera (e.g., scan a QR code to join a
bundle), offline problem solving, or platform push notifications beyond what PWA supports
on iOS — then React Native with Expo is the right path. Document the decision here and
follow the guide in the git history of this file.
