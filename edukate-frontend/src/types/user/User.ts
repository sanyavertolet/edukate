import { SpringUserRole } from "./Role";

export interface User {
    name: string;
    roles: SpringUserRole[];
    status: SpringUserStatus;
}

export type SpringUserStatus = "ACTIVE" | "PENDING" | "DELETED";
