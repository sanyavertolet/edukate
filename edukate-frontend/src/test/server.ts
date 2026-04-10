import { setupServer } from "msw/node";
import { getOpenAPIDefinitionMock as backendMocks } from "@/generated/backend";
import { getOpenAPIDefinitionMock as gatewayMocks } from "@/generated/gateway";
import { getOpenAPIDefinitionMock as notifierMocks } from "@/generated/notifier";

export const server = setupServer(...backendMocks(), ...gatewayMocks(), ...notifierMocks());
