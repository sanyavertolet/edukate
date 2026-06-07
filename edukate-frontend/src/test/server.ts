import { setupServer } from "msw/node";
import type { RequestHandler } from "msw";
import {
    getEdukateAPIMock as backendMocks,
    getGetMemberProblemSetsMockHandler,
    getGetPublicProblemSetsMockHandler,
} from "@/generated/backend";
import { getEdukateAPIMock as gatewayMocks } from "@/generated/gateway";
import { getEdukateAPIMock as notifierMocks } from "@/generated/notifier";

// Orval emits mock handlers in OpenAPI declaration order, so the catch-all
// `*/api/v1/problem-sets/:shareCode` handler is registered before the literal
// `/member` and `/public` paths and would intercept them as if the segment
// were a shareCode. Re-register the literal paths first.
// Anchored to `RequestHandler[]` so the IDE's TS service sees a concrete type
// even if it loses Orval's generic chain on `ProblemSetMetadata`.
const literalRouteHandlers: RequestHandler[] = [
    getGetMemberProblemSetsMockHandler(),
    getGetPublicProblemSetsMockHandler(),
];

export const server = setupServer(...literalRouteHandlers, ...backendMocks(), ...gatewayMocks(), ...notifierMocks());
