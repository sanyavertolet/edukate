import { FC, useEffect, useMemo } from "react";
import {
    Avatar, Box, FormControl, InputLabel, List, ListItem, ListItemAvatar, ListItemText, MenuItem, Paper, Select
} from "@mui/material";
import { getColorByStringHash, getFirstLetters } from "../../utils/utils";
import { useBundleChangeUserRoleMutation, useBundleUserListQuery } from "../../http/requests";
import { useAuthContext } from "../auth/AuthContextProvider";
import { InvitedUsersManagementComponent } from "./InvitedUsersManagementComponent";

interface UserRoleManagerProps {
    shareCode: string;
}

const UserRoleManager: FC<UserRoleManagerProps> = ({shareCode}) => {
    const changeUserRoleMutation = useBundleChangeUserRoleMutation();
    const { data: userNameWithRoleList, refetch: refetchUserList } = useBundleUserListQuery(shareCode);

    useEffect(() => { refetchUserList().then(); }, [changeUserRoleMutation.isSuccess, refetchUserList]);
    const handleRoleChange = (username: string, newRole: string) => {
        changeUserRoleMutation.mutate({shareCode, username, role: newRole});
    };

    const { user } = useAuthContext();
    const userRole = useMemo(
        () => userNameWithRoleList?.find(
            (userWithRole) => userWithRole.name == user?.name,
        )?.role || "USER",
        [userNameWithRoleList, user?.name]
    )
    return (
        <Paper variant={"outlined"} sx={{ borderRadius: 0, borderTop: 0 }}>
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                { userNameWithRoleList?.map(({name: username, role}) =>
                    <ListItem key={`current-${username}`} secondaryAction={
                        <FormControl>
                            <InputLabel id={`${username}-role-label`}>Role</InputLabel>
                            <Select
                                labelId={`${username}-role-label`} id={`${username}-role`} value={role} label="Role"
                                onChange={(event) => handleRoleChange(username, event.target.value)}
                                disabled={ user?.name == username } size={"small"} sx={{ minWidth: { xs: 125, lg: 200 } }}
                            >
                                <MenuItem value={"ADMIN"} disabled>Admin</MenuItem>
                                <MenuItem value={"MODERATOR"} disabled={userRole == "USER"}>Moderator</MenuItem>
                                <MenuItem value={"USER"}>User</MenuItem>
                            </Select>
                        </FormControl>
                    }>
                        <ListItemAvatar>
                            <Avatar key={`${username}-avatar`} sx={{ backgroundColor: getColorByStringHash(username) }}>
                                {getFirstLetters(username, 2)}
                            </Avatar>
                        </ListItemAvatar>
                        <ListItemText primary={username} />
                    </ListItem>
                )}
            </List>
        </Paper>
    );
};


interface UserRolesManagementComponentProps {
    shareCode: string;
}

export const BundleUserManagement: FC<UserRolesManagementComponentProps> = ({ shareCode }) => {

    return (
        <Box sx={{ borderRadius: 1 }}>
            <InvitedUsersManagementComponent shareCode={shareCode}/>
            <UserRoleManager shareCode={shareCode}/>
        </Box>
    );
};