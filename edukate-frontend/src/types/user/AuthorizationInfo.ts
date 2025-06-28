import { SpringUserStatus } from "./User";
import { SpringUserRole } from "./Role";

export interface AuthorizationInfo {
    username: string;
    token: string;
    roles: SpringUserRole[];
    status: SpringUserStatus;
}
