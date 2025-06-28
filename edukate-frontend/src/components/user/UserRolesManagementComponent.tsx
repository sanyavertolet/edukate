import { FC, useEffect, useMemo, useState } from "react";
import {
    Alert, Avatar, Box, FormControl, IconButton, InputBase, InputLabel, List,
    ListItem, ListItemAvatar, ListItemText, MenuItem, Paper, Select, Snackbar
} from "@mui/material";
import { getColorByStringHash, getFirstLetters } from "../../utils/utils";
import AddIcon from "@mui/icons-material/AddOutlined";
import {
    useBundleChangeUserRoleMutation, useBundleInviteUserMutation, useBundleUserListQuery
} from "../../http/requests";
import { useAuthContext } from "../auth/AuthContextProvider";

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
                setSnackbarMessage({ message: `User ${username} has been invited!`, severity: "success" });
                setUsername("");
            },
            onError: () => {
                setSnackbarMessage({ message: `Could not invite ${username}!`, severity: "error" });
                setUsername("");
            },
        });
    };
    type SnackbarMessage = { message: string, severity: "success" | "error" };
    const [snackbarMessage, setSnackbarMessage] = useState<SnackbarMessage | null>(null);
    const handleSnackbarClose = () => { setSnackbarMessage(null); };
    return (
        <Paper component="form" sx={{ p: '0px 1px', display: 'flex', alignItems: 'center' }} variant={"outlined"}>
            <InputBase
                sx={{ ml: 1, flex: 1 }} placeholder="Username" value={username}
                onChange={(e) => setUsername(e.target.value)} inputProps={{ 'aria-label': 'username' }}
            />
            <IconButton color="primary" aria-label="add user" onClick={onAddClick}>
                <AddIcon/>
            </IconButton>
            <Snackbar open={snackbarMessage != null} autoHideDuration={4000} onClose={handleSnackbarClose}>
                <Alert onClose={handleSnackbarClose} severity={snackbarMessage?.severity} sx={{ width: '100%' }}>
                    { snackbarMessage?.message }
                </Alert>
            </Snackbar>
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
        <Paper variant={"outlined"}>
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                { users && users.map(({username, role}) =>
                    <ListItem secondaryAction={
                        <FormControl>
                            <InputLabel id={`${username}-role-label`}>Role</InputLabel>
                            <Select
                                labelId={`${username}-role-label`} id={`${username}-role`} value={role} label="Role"
                                onChange={(event) => handleRoleChange(username, event.target.value)}
                                disabled={ user?.name == username } size={"small"} sx={{ minWidth: { xs: 125, md: 200 } }}
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
        <Box>
            <UserSearchForm bundleShareCode={shareCode}/>
            <UserRoleManager shareCode={shareCode}/>
        </Box>
    );
};
