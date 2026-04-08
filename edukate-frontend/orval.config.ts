import { defineConfig } from "orval";

export default defineConfig({
    backend: {
        input: { target: "../spec/openapi-edukate-backend.yaml" },
        output: {
            client: "react-query",
            httpClient: "axios",
            target: "./src/generated/backend.ts",
            override: {
                mutator: { path: "./src/lib/axios.ts", name: "client" },
            },
        },
    },
    gateway: {
        input: { target: "../spec/openapi-edukate-gateway.yaml" },
        output: {
            client: "react-query",
            httpClient: "axios",
            target: "./src/generated/gateway.ts",
            override: {
                mutator: { path: "./src/lib/axios.ts", name: "client" },
            },
        },
    },
    notifier: {
        input: { target: "../spec/openapi-edukate-notifier.yaml" },
        output: {
            client: "react-query",
            httpClient: "axios",
            target: "./src/generated/notifier.ts",
            override: {
                mutator: { path: "./src/lib/axios.ts", name: "client" },
            },
        },
    },
});
