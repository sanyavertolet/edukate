export interface User {
    name: string;
    roles: UserRole[];
    status: UserStatus;
}

export type UserStatus = "ACTIVE" | "PENDING" | "DELETED";
export type UserRole = "ADMIN" | "USER" | "MODERATOR";
