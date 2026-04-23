import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { visualizer } from "rollup-plugin-visualizer";

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
    plugins: [react(), mode === "analyze" && visualizer({ open: true, filename: "dist/stats.html", gzipSize: true })],
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
                manualChunks: {
                    "vendor-react": ["react", "react-dom", "react-router-dom"],
                    "vendor-query": ["@tanstack/react-query"],
                    "vendor-mui": ["@mui/material", "@mui/icons-material", "@emotion/react", "@emotion/styled"],
                    "vendor-misc": [
                        "axios",
                        "react-toastify",
                        "react-cookie",
                        "typescript-cookie",
                        "react-error-boundary",
                        "yet-another-react-lightbox",
                    ],
                    "vendor-particles": ["@tsparticles/react", "@tsparticles/slim"],
                },
            },
        },
    },
    optimizeDeps: {
        exclude: ["js-big-decimal"],
    },
}));
