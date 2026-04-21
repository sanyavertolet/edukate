import { setupServer } from "msw/node";
import { getEdukateAPIMock as backendMocks } from "@/generated/backend";
import { getEdukateAPIMock as gatewayMocks } from "@/generated/gateway";
import { getEdukateAPIMock as notifierMocks } from "@/generated/notifier";

export const server = setupServer(...backendMocks(), ...gatewayMocks(), ...notifierMocks());
