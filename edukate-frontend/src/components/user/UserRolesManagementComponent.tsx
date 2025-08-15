import { FC, useEffect, useMemo, useState } from "react";
import {
    Avatar, Box, FormControl, IconButton, InputBase, InputLabel, List,
    ListItem, ListItemAvatar, ListItemText, MenuItem, Paper, Select
} from "@mui/material";
import { getColorByStringHash, getFirstLetters } from "../../utils/utils";
import AddIcon from "@mui/icons-material/AddOutlined";
import {
    useBundleChangeUserRoleMutation, useBundleInviteUserMutation, useBundleUserListQuery
} from "../../http/requests";
import { useAuthContext } from "../auth/AuthContextProvider";
import { toast } from "react-toastify";

interface UserSearchFormProps {
    bundleShareCode: string;
}

const UserSearchForm: FC<UserSearchFormProps> = ({bundleShareCode}) => {
    const [username, setUsername] = useState("");
    /* todo: should use url from props or an axios request */
    const inviteUserMutation = useBundleInviteUserMutation();
    const onAddClick = () => {
        inviteUserMutation.mutate({ username, shareCode: bundleShareCode }, {
            onSuccess: () => {
                toast.success(`User ${username} has been invited!`);
                setUsername("");
            },
            onError: () => {
                toast.error(`Could not invite ${username}!`);
                setUsername("");
            },
        });
    };

    return (
        <Paper
            component="form" variant={"outlined"}
            sx={{ p: '0px 1px', display: 'flex', alignItems: 'center', borderRadius: 0 }}
        >
            <InputBase
                sx={{ ml: 1, flex: 1 }} placeholder="Username" value={username}
                onChange={(e) => setUsername(e.target.value)} inputProps={{ 'aria-label': 'username' }}
            />
            <IconButton color="primary" aria-label="add user" onClick={onAddClick}>
                <AddIcon/>
            </IconButton>
        </Paper>
    );
};

interface UserRoleManagerProps {
    shareCode: string;
}

const UserRoleManager: FC<UserRoleManagerProps> = ({shareCode}) => {
    /* todo: should use url from props or an axios request */
    const { data: users, refetch: refetchUserList } = useBundleUserListQuery(shareCode);
    const changeUserRoleMutation = useBundleChangeUserRoleMutation();

    useEffect(() => { refetchUserList().then(); }, [changeUserRoleMutation.isSuccess, refetchUserList]);

    const handleRoleChange = (username: string, newRole: string) => {
        changeUserRoleMutation.mutate({shareCode, username, role: newRole});
    };
    const { user } = useAuthContext();
    const userRole = useMemo(
        () => users?.find(
            (userWithRole) => userWithRole.username == user?.name,
        )?.role || "USER",
        [users, user?.name]
    )
    return (
        <Paper variant={"outlined"} sx={{ borderRadius: 0, borderTop: 0 }}>
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                { users && users.map(({username, role}) =>
                    <ListItem secondaryAction={
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

export const UserRolesManagementComponent: FC<UserRolesManagementComponentProps> = ({ shareCode }) => {
    return (
        <Box sx={{ borderRadius: 1 }}>
            <UserSearchForm bundleShareCode={shareCode}/>
            <UserRoleManager shareCode={shareCode}/>
        </Box>
    );
};
