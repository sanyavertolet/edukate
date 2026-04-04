import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { execSync } from "child_process";

const gitCommit = (() => {
    try {
        return execSync("git rev-parse --short HEAD").toString().trim();
    } catch {
        return "unknown";
    }
})();

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    define: {
        __GIT_COMMIT__: JSON.stringify(gitCommit),
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
    optimizeDeps: {
        exclude: ["js-big-decimal"],
    },
});
