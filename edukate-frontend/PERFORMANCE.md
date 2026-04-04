# Frontend Performance Notes

Goal: minimize initial bundle size, time-to-interactive, and per-request transferred bytes.

---

## Estimated current bundle composition

| Dependency                         | Est. size (minified)   | Notes                                     |
| ---------------------------------- | ---------------------- | ----------------------------------------- |
| `@tsparticles/slim` + engine       | ~200KB                 | Decorative only — highest-ROI replacement |
| KaTeX (npm)                        | ~290KB JS + ~100KB CSS | Already npm; needs lazy-load              |
| `@mui/material` + emotion          | ~250KB (tree-shaken)   | Depends on components used                |
| `@mui/icons-material` (used icons) | ~30-60KB               | Only if named imports everywhere          |
| React + ReactDOM                   | ~130KB                 | Fixed cost                                |
| React Router v7                    | ~35KB                  | Fixed cost                                |
| TanStack React Query v5            | ~42KB                  | Fixed cost                                |
| Axios                              | ~35KB                  | Replaceable with `fetch`                  |
| react-toastify                     | ~30KB                  | —                                         |
| @uidotdev/usehooks                 | ~20KB                  | Check actual usage                        |
| react-cookie + typescript-cookie   | ~15KB                  | Two cookie libs — audit if both needed    |

**Estimated total:** ~1.3MB minified, ~1.5MB+ with CSS and fonts.
**After gzip:** ~430KB. **After brotli:** ~360KB (requires nginx config — see §4).

---

## §1 — Replace `@tsparticles` with a CSS animation

**Impact: ~200KB saved from initial bundle + ongoing CPU eliminated.**

`@tsparticles/slim` runs a JavaScript physics simulation with 100 particles at 144fps on
**every page**, every session. For a decorative background this is a poor ROI.

### Replacement options (pick one)

**Option A — Pure CSS particles (zero JS, zero dependencies)**

A CSS `@keyframes` animation with `::before`/`::after` pseudo-elements can achieve a
similar "floating dots" aesthetic at zero bundle cost. See:
https://codepen.io/tag/css-particles (reference, not a dependency)

**Option B — `particles.js` direct (vanilla, ~50KB)**

If the exact tsparticles behaviour must be kept, use the original `particles.js` loaded
via a `<script>` tag with `defer` — it won't block the main bundle.

**Option C — Keep tsparticles, reduce scope**

If keeping `@tsparticles`, at minimum:

- Drop `fpsLimit` from 144 → 30 (background animation doesn't need 144fps)
- Drop particle count from 100 → 40
- Disable collision detection (`collisions.enable: false`) — expensive with 100 particles
- Lazy-load the component (`React.lazy`) so it doesn't block first paint

### Current config issues

```ts
fpsLimit: 144,       // ← excessive for decorative background; use 30
value: 100,          // ← 100 particles with collision detection is CPU-heavy
collisions: { enable: true, mode: "bounce" }  // ← O(n²) collision check every frame
```

---

## §2 — Lazy-load KaTeX

**Impact: ~290KB JS + ~100KB CSS deferred until a math-rendering route is visited.**

KaTeX is currently bundled eagerly — every user downloads it on the first load even if
they never visit a problem page.

### Current state

`LatexComponent.tsx` already uses `katex` from npm correctly (inline `renderToString`).
The CDN `<script>` and `<link>` tags in `index.html` are **redundant** and should be
removed — the npm package covers everything.

### Lazy-load strategy

```tsx
// src/features/problems/components/LazyLatex.tsx
import { lazy, Suspense } from "react";

const LatexComponent = lazy(() =>
    import("@/shared/components/LatexComponent").then(async (m) => {
        // Import KaTeX CSS as a side-effect when the component loads
        await import("katex/dist/katex.min.css");
        return m;
    }),
);

export function LazyLatex({ text }: { text: string }) {
    return (
        <Suspense fallback={<span>{text}</span>}>
            <LatexComponent text={text} />
        </Suspense>
    );
}
```

Use `LazyLatex` only on routes that render problem content. KaTeX won't load at all for
users who only browse the index or bundle list pages.

### Remove CDN from `index.html`

Delete these lines (they're a double-load with the npm package):

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@.../katex.min.css" ... />
<script defer src="https://cdn.jsdelivr.net/npm/katex@.../katex.min.js" ...></script>
<script defer src="https://cdn.jsdelivr.net/npm/katex@.../auto-render.min.js" ...></script>
```

---

## §3 — Vite build: manual chunk splitting

**Impact: better long-term caching — vendor chunks stay cached across deploys if libs didn't change.**

By default Vite puts all vendor code in one `vendor.js` chunk. If MUI or React Query
releases a patch, users must re-download the entire vendor chunk. Manual splitting lets
stable libs (React, React Router) stay cached while frequently-updated ones (MUI) change
independently.

### `vite.config.ts` addition

```ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: { "@": path.resolve(__dirname, "./src") },
    },
    build: {
        rollupOptions: {
            output: {
                manualChunks: {
                    "react-core": ["react", "react-dom", "react-router-dom"],
                    query: ["@tanstack/react-query"],
                    mui: ["@mui/material", "@mui/icons-material", "@emotion/react", "@emotion/styled"],
                    katex: ["katex"], // will be async chunk via lazy-load (§2)
                    particles: ["@tsparticles/react", "@tsparticles/slim"], // until replaced (§1)
                },
            },
        },
    },
    server: {
        host: "0.0.0.0",
        port: 80,
        proxy: {
            "/api": { target: "http://localhost:5810", changeOrigin: true },
        },
    },
    optimizeDeps: {
        exclude: ["js-big-decimal"],
    },
});
```

---

## §4 — nginx: compression + caching headers

**Impact: 60-70% reduction in transferred bytes; near-zero re-downloads for returning users.**

The current `nginx.conf` serves all assets uncompressed with no cache headers. This means:

- Every request transfers the full file size
- Every navigation reload re-fetches all JS/CSS (even unchanged files)

### Replace `nginx.conf`

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_comp_level 6;
    gzip_types
        text/plain text/css application/json application/javascript
        text/javascript application/x-javascript image/svg+xml;

    # Serve pre-compressed .gz files if they exist (Vite can generate them)
    gzip_static on;

    # Health check
    location /health {
        access_log off;
        return 200 'healthy\n';
    }

    # Deny dotfiles
    location ~ /\.(?!well-known) {
        deny all;
    }

    # Hashed JS/CSS chunks — cache forever (content-addressed)
    location ~* \.(js|css)$ {
        add_header Cache-Control "public, max-age=31536000, immutable";
        try_files $uri =404;
    }

    # Images and fonts — cache for a week
    location ~* \.(png|jpg|jpeg|gif|webp|ico|woff2|woff|ttf|svg)$ {
        add_header Cache-Control "public, max-age=604800";
        try_files $uri =404;
    }

    # index.html — never cache (SPA entry point, must always be fresh)
    location = /index.html {
        add_header Cache-Control "no-store, no-cache, must-revalidate";
    }

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

**To generate pre-compressed `.gz` files at build time**, add to `vite.config.ts`:

```bash
npm install --save-dev vite-plugin-compression
```

```ts
import compression from "vite-plugin-compression";

plugins: [react(), compression({ algorithm: "gzip" })];
// Or for brotli (better ratio, requires nginx brotli module):
// compression({ algorithm: 'brotliCompress', ext: '.br' })
```

---

## §5 — Audit MUI icon imports

**Impact: potentially 0.5-2MB saved if any namespace imports exist.**

`@mui/icons-material` exports 2,000+ icons. Vite tree-shakes named imports correctly, but
a namespace import defeats tree-shaking entirely.

```ts
// ✅ fine — tree-shaken
import { Add, Delete, ExpandMore } from "@mui/icons-material";

// ❌ pulls in all 2,000+ icons
import * as Icons from "@mui/icons-material";
```

Run this to audit:

```bash
grep -r "from '@mui/icons-material'" src/ | grep -v "{ "
```

Any line without `{` is a namespace import that needs fixing.

---

## §6 — Consider replacing Axios with native `fetch`

**Impact: ~35KB saved. Tradeoff: lose interceptors, automatic JSON parsing, progress events.**

Modern browsers have excellent `fetch` support and React Query can wrap it directly.
Only worth doing if bundle size is critical and the features above aren't needed.

This is a low-priority, high-effort change. **Do §1 and §4 first** — they're much higher ROI.

---

## §7 — Audit `@uidotdev/usehooks`

`@uidotdev/usehooks` adds ~20KB. Check if it's actually used:

```bash
grep -r "@uidotdev/usehooks" src/
```

If usage is 1-2 hooks, copy those hooks inline (they're typically small) and drop the
dependency entirely.

---

## Prioritised order

| Priority | Action                                 | Effort  | Saving                     |
| -------- | -------------------------------------- | ------- | -------------------------- |
| 1        | nginx compression + cache headers (§4) | Low     | 60-70% transferred bytes   |
| 2        | Remove KaTeX CDN from `index.html`     | Trivial | Eliminates double-load     |
| 3        | Lazy-load KaTeX (§2)                   | Low     | ~390KB from initial bundle |
| 4        | Replace `@tsparticles` with CSS (§1)   | Medium  | ~200KB + CPU               |
| 5        | Vite `manualChunks` (§3)               | Low     | Better caching             |
| 6        | Audit MUI icons (§5)                   | Low     | Variable                   |
| 7        | Audit `@uidotdev/usehooks` (§7)        | Trivial | ~20KB                      |
| 8        | Replace Axios with `fetch` (§6)        | High    | ~35KB                      |
