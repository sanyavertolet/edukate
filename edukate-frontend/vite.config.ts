import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";
import path from "path";
import { visualizer } from "rollup-plugin-visualizer";

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
    plugins: [
        react(),
        VitePWA({
            registerType: "prompt",
            manifest: false,
            devOptions: { enabled: false },
            workbox: {
                globPatterns: ["**/*.{js,css,html,ico,png,svg,woff2}"],
                navigateFallback: "/index.html",
                navigateFallbackDenylist: [/^\/api\//],
                runtimeCaching: [
                    {
                        urlPattern: /^https?:\/\/.*\/api\//,
                        handler: "NetworkFirst",
                        options: {
                            cacheName: "api-cache",
                            expiration: { maxEntries: 100, maxAgeSeconds: 300 },
                            cacheableResponse: { statuses: [0, 200] },
                            networkTimeoutSeconds: 5,
                        },
                    },
                ],
            },
        }),
        mode === "analyze" && visualizer({ open: true, filename: "dist/stats.html", gzipSize: true }),
    ],
    define: {
        __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
    },
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    server: {
        host: "0.0.0.0",
        port: 80,
        proxy: {
            "/api": {
                target: "http://localhost:5810",
                changeOrigin: true,
            },
        },
    },
    build: {
        rollupOptions: {
            output: {
                // Function form (not object form) so that EVERY third-party module — including
                // transitive deps and sub-path imports — is guaranteed to land in a `vendor-` chunk.
                // Object form only seeds a chunk with a package's entry module and lets Rollup's
                // dedup pass leak the rest (e.g. `scheduler`, `katex`) into the app/page chunks.
                manualChunks(id) {
                    if (!id.includes("/node_modules/")) {
                        return undefined;
                    }
                    const afterModules = id.split("/node_modules/").pop() ?? "";
                    const segments = afterModules.split("/");
                    const pkg = afterModules.startsWith("@") ? `${segments[0]}/${segments[1]}` : segments[0];

                    if (pkg === "react" || pkg === "react-dom" || pkg === "scheduler" || pkg.startsWith("react-router")) {
                        return "vendor-react";
                    }
                    if (pkg.startsWith("@tanstack/")) {
                        return "vendor-query";
                    }
                    if (pkg.startsWith("@mui/") || pkg.startsWith("@emotion/")) {
                        return "vendor-mui";
                    }
                    if (pkg.startsWith("@tsparticles/")) {
                        return "vendor-particles";
                    }
                    if (pkg === "katex") {
                        return "vendor-katex";
                    }
                    if (pkg.startsWith("i18next") || pkg === "react-i18next") {
                        return "vendor-i18n";
                    }
                    if (pkg.startsWith("@dnd-kit/")) {
                        return "vendor-dnd";
                    }
                    // Everything else third-party (axios, fuse.js, react-* utilities, and all
                    // remaining transitive deps) — catch-all so nothing escapes into app chunks.
                    return "vendor-misc";
                },
            },
        },
    },
    optimizeDeps: {
        exclude: ["js-big-decimal"],
    },
}));
