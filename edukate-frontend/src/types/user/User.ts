import { Role } from "./Role";

export interface User {
    name: string;
    roles: Role[];
    status: UserStatus;
}

export type UserStatus = "ACTIVE" | "PENDING" | "DELETED";
