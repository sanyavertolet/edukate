import { FC } from "react";
import { Avatar, Divider, IconButton, List, ListItem, ListItemAvatar, ListItemText, Paper } from "@mui/material";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { getColorByStringHash, getFirstLetters } from "@/shared/utils/utils";
import { useBundleExpireInviteMutation, useBundleInvitedUserListQuery } from "@/features/bundles/api";
import { toast } from "react-toastify";
import { UserSearchInput } from "./UserSearchInput";

interface InvitedUserManagementProps {
    shareCode: string;
}

export const InvitedUserManagement: FC<InvitedUserManagementProps> = ({ shareCode }) => {
    const expireInviteMutation = useBundleExpireInviteMutation();

    const { data: invitedUsers, refetch: refetchInvitedUsers } = useBundleInvitedUserListQuery(shareCode);

    const handleRemoveInvitation = (username: string) => {
        expireInviteMutation.mutate(
            { shareCode, username },
            {
                onSuccess: () => {
                    toast.info(`Invitation to ${username} has been removed`);
                    void refetchInvitedUsers();
                },
                onError: () => {
                    toast.error(`Failed to remove invitation to ${username}`);
                },
            },
        );
    };

    return (
        <Paper variant="outlined" sx={{ borderRadius: 0 }}>
            <UserSearchInput
                bundleShareCode={shareCode}
                onInvited={() => {
                    void refetchInvitedUsers();
                }}
            />
            <Divider />
            <List sx={{ width: "100%", bgcolor: "background.paper" }}>
                {invitedUsers?.length === 0 && (
                    <ListItem>
                        <ListItemText secondary="No pending invitations" />
                    </ListItem>
                )}
                {invitedUsers &&
                    invitedUsers.map((username: string) => (
                        <ListItem
                            key={`invited-${username}`}
                            secondaryAction={
                                <IconButton
                                    edge="end"
                                    aria-label="remove-invitation"
                                    onClick={() => {
                                        handleRemoveInvitation(username);
                                    }}
                                >
                                    <DeleteOutlineIcon />
                                </IconButton>
                            }
                        >
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
};
