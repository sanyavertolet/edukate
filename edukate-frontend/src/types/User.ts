export interface User {
    name: string;
    roles: ("ADMIN" | "USER" | "MODERATOR")[];
    status: "ACTIVE" | "PENDING" | "DELETED";
}
