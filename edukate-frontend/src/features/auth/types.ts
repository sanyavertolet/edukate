export type Role = "ADMIN" | "MODERATOR" | "USER";

export type UserStatus = "ACTIVE" | "PENDING" | "DELETED";

export type { UserDto as User } from "@/generated/backend";

export type UserNameWithRole = {
    name: string;
    role: Role;
};
