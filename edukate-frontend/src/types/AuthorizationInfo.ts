export interface AuthorizationInfo {
    username: string;
    token: string;
    roles: ("ADMIN" | "USER" | "MODERATOR")[];
    status: "ACTIVE" | "PENDING" | "DELETED";
}
