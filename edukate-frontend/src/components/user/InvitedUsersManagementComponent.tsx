import { FC } from "react";
import { Avatar, Divider, IconButton, List, ListItem, ListItemAvatar, ListItemText, Paper } from "@mui/material";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { getColorByStringHash, getFirstLetters } from "../../utils/utils";
import { useBundleExpireInviteMutation, useBundleInvitedUserListQuery } from "../../http/requests";
import { toast } from "react-toastify";
import { UserSearchInputForm } from "./UserSearchInputForm";

interface InvitedUsersManagementComponentProps {
    shareCode: string;
}

export const InvitedUsersManagementComponent: FC<InvitedUsersManagementComponentProps> = ({ shareCode }) => {
    const expireInviteMutation = useBundleExpireInviteMutation();

    const { data: invitedUsers, refetch: refetchInvitedUsers } = useBundleInvitedUserListQuery(shareCode);

    const handleRemoveInvitation = (username: string) => {
        expireInviteMutation.mutate({ shareCode, username }, {
            onSuccess: () => {
                toast.info(`Invitation to ${username} has been removed`);
                refetchInvitedUsers().then();
            },
            onError: () => {
                toast.error(`Failed to remove invitation to ${username}`);
            }
        });
    };

    return (
        <Paper variant="outlined" sx={{ borderRadius: 0 }}>
            <UserSearchInputForm bundleShareCode={shareCode} onInvited={() => refetchInvitedUsers()} />
            <Divider/>
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                {invitedUsers?.length === 0 && (
                    <ListItem>
                        <ListItemText secondary="No pending invitations" />
                    </ListItem>
                )}
                {invitedUsers && invitedUsers.map((username) => (
                    <ListItem key={`invited-${username}`} secondaryAction={
                        <IconButton
                            edge="end" aria-label="remove-invitation" onClick={() => handleRemoveInvitation(username)}
                        >
                            <DeleteOutlineIcon />
                        </IconButton>
                    }>
                        <ListItemAvatar>
                            <Avatar sx={{ backgroundColor: getColorByStringHash(username) }}>
                                {getFirstLetters(username, 2)}
                            </Avatar>
                        </ListItemAvatar>
                        <ListItemText primary={username} secondary="Invitation pending" />
                    </ListItem>
                ))}
            </List>
        </Paper>
    );
}
