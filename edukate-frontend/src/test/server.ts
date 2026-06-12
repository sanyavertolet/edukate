import { setupServer } from "msw/node";
import type { RequestHandler } from "msw";
import {
    getEdukateAPIMock as backendMocks,
    getGetMemberProblemSetsMockHandler,
    getGetPublicProblemSetsMockHandler,
    getLeaveProblemSetMockHandler,
} from "@/generated/backend";
import { getEdukateAPIMock as gatewayMocks } from "@/generated/gateway";
import { getEdukateAPIMock as notifierMocks } from "@/generated/notifier";

// Orval emits mock handlers in OpenAPI declaration order. The same-path/method
// collisions we need to preempt:
//  * GET  /api/v1/problem-sets/:shareCode   shadows  /member and /public
//  * DELETE /api/v1/problem-sets/:shareCode/members/:username  shadows  /members/me
// Re-register the literal paths first so MSW resolves them ahead of the catch-alls.
// Anchored to `RequestHandler[]` so the IDE's TS service sees a concrete type
// even if it loses Orval's generic chain on `ProblemSetMetadata`.
const literalRouteHandlers: RequestHandler[] = [
    getGetMemberProblemSetsMockHandler(),
    getGetPublicProblemSetsMockHandler(),
    getLeaveProblemSetMockHandler(),
];

export const server = setupServer(...literalRouteHandlers, ...backendMocks(), ...gatewayMocks(), ...notifierMocks());
