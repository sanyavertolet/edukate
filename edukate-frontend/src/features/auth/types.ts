export type Role = "ADMIN" | "MODERATOR" | "USER";

export interface User {
    name: string;
    roles: Role[];
    status: UserStatus;
}

export type UserStatus = "ACTIVE" | "PENDING" | "DELETED";

export type UserNameWithRole = {
    name: string;
    role: Role;
};
