import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
    plugins: [react()],
    test: {
        globals: true,
        environment: "jsdom",
        setupFiles: ["src/test/setup.ts"],
        typecheck: {
            tsconfig: "./tsconfig.test.json",
        },
        coverage: {
            provider: "v8",
            reporter: ["text", "lcov", "html"],
            include: ["src/**/*.{ts,tsx}"],
            exclude: [
                "src/generated/**",
                "src/test/**",
                "src/**/*.d.ts",
                "src/main.tsx",
                "src/app/router.tsx",
            ],
            thresholds: {
                statements: 62,
                branches: 61,
                functions: 55,
                lines: 63,
            },
        },
    },
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
});
