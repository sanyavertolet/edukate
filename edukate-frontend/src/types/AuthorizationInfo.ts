import { UserRole, UserStatus } from "./User";

export interface AuthorizationInfo {
    username: string;
    token: string;
    roles: UserRole[];
    status: UserStatus;
}
