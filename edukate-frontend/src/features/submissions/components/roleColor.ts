import { ProblemSetMetadataCurrentUserRole } from "@/generated/backend";

const ROLE_COLOR: Record<ProblemSetMetadataCurrentUserRole, string> = {
    ADMIN: "primary.main",
    MODERATOR: "info.main",
    USER: "secondary.main",
};

export const colorForRole = (role: ProblemSetMetadataCurrentUserRole | undefined): string | undefined =>
    role ? ROLE_COLOR[role] : undefined;
