import { FC, MouseEvent, useState } from "react";
import {
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Divider,
    IconButton,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    ListSubheader,
    Menu,
    MenuItem,
    Stack,
    Typography,
} from "@mui/material";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { UserAvatar } from "@/shared/components/UserAvatar";
import {
    useProblemSetChangeUserRoleMutation,
    useProblemSetExpireInviteMutation,
    useProblemSetInvitedUserListQuery,
    useProblemSetUserListQuery,
} from "@/features/problem-sets/api";
import { useAuthContext } from "@/features/auth/context";
import { UserSearchInput } from "./UserSearchInput";
import { toast } from "react-toastify";

interface ProblemSetUserManagementProps {
    shareCode: string;
}

const roleLabel: Record<string, string> = {
    ADMIN: "Admin",
    MODERATOR: "Moderator",
    USER: "User",
};

const roleColor: Record<string, "primary" | "secondary" | "default"> = {
    ADMIN: "primary",
    MODERATOR: "secondary",
    USER: "default",
};

type RemoveConfirmation = { username: string; type: "member" | "invited" };

export const ProblemSetUserManagement: FC<ProblemSetUserManagementProps> = ({ shareCode }) => {
    const { user } = useAuthContext();
    const { data: members } = useProblemSetUserListQuery(shareCode);
    const { data: invitedUsers, refetch: refetchInvitedUsers } = useProblemSetInvitedUserListQuery(shareCode);
    const changeRoleMutation = useProblemSetChangeUserRoleMutation();
    const expireInviteMutation = useProblemSetExpireInviteMutation();

    const [menuAnchor, setMenuAnchor] = useState<{ el: HTMLElement; username: string } | null>(null);
    const [removeDialogOpen, setRemoveDialogOpen] = useState(false);
    const [removeConfirmation, setRemoveConfirmation] = useState<RemoveConfirmation | null>(null);

    const currentUserRole = members?.find((m) => m.name === user?.name)?.role ?? "USER";

    const handleRoleChipClick = (event: MouseEvent<HTMLElement>, username: string) => {
        if (username === user?.name) return;
        setMenuAnchor({ el: event.currentTarget, username });
    };

    const handleRoleSelect = (newRole: string) => {
        if (!menuAnchor) return;
        changeRoleMutation.mutate({ shareCode, username: menuAnchor.username, role: newRole });
        setMenuAnchor(null);
    };

    const handleConfirmRemove = () => {
        if (!removeConfirmation) return;
        const { username, type } = removeConfirmation;
        if (type === "member") {
            changeRoleMutation.mutate({ shareCode, username });
        } else {
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
        }
        setRemoveDialogOpen(false);
    };

    return (
        <>
            <UserSearchInput
                problemSetShareCode={shareCode}
                onInvited={() => {
                    void refetchInvitedUsers();
                }}
            />

            <List disablePadding>
                {/* Members Section */}
                <ListSubheader sx={{ bgcolor: "transparent", lineHeight: "36px" }}>Members</ListSubheader>
                {(!members || members.length === 0) && (
                    <ListItem>
                        <ListItemText secondary="No members yet" />
                    </ListItem>
                )}
                {members?.map(({ name: username, role }) => (
                    <ListItem
                        key={username}
                        secondaryAction={
                            <Stack direction="row" alignItems="center" spacing={0.5}>
                                <Chip
                                    label={roleLabel[role] ?? role}
                                    color={roleColor[role] ?? "default"}
                                    size="small"
                                    variant={role === "ADMIN" ? "filled" : "outlined"}
                                    onClick={
                                        username !== user?.name
                                            ? (e) => {
                                                  handleRoleChipClick(e, username);
                                              }
                                            : undefined
                                    }
                                    sx={{
                                        minWidth: 80,
                                        justifyContent: "center",
                                        ...(username !== user?.name && { cursor: "pointer" }),
                                    }}
                                />
                                <IconButton
                                    edge="end"
                                    aria-label="remove user"
                                    size="small"
                                    onClick={() => {
                                        setRemoveConfirmation({ username, type: "member" });
                                        setRemoveDialogOpen(true);
                                    }}
                                    disabled={username === user?.name}
                                    sx={{ visibility: username === user?.name ? "hidden" : "visible" }}
                                >
                                    <DeleteOutlineIcon fontSize="small" />
                                </IconButton>
                            </Stack>
                        }
                    >
                        <ListItemAvatar>
                            <UserAvatar name={username} highlighted={username === user?.name} />
                        </ListItemAvatar>
                        <ListItemText primary={username} />
                    </ListItem>
                ))}

                {/* Pending Invitations Section */}
                {invitedUsers && invitedUsers.length > 0 && (
                    <>
                        <Divider />
                        <ListSubheader sx={{ bgcolor: "transparent", lineHeight: "36px" }}>
                            Pending Invitations
                        </ListSubheader>
                        {invitedUsers.map((username) => (
                            <ListItem
                                key={`invited-${username}`}
                                secondaryAction={
                                    <Stack direction="row" alignItems="center" spacing={0.5}>
                                        <Chip
                                            label="Pending"
                                            color="warning"
                                            size="small"
                                            variant="outlined"
                                            sx={{ minWidth: 80, justifyContent: "center" }}
                                        />
                                        <IconButton
                                            edge="end"
                                            aria-label="remove invitation"
                                            size="small"
                                            onClick={() => {
                                                setRemoveConfirmation({ username, type: "invited" });
                                                setRemoveDialogOpen(true);
                                            }}
                                        >
                                            <DeleteOutlineIcon fontSize="small" />
                                        </IconButton>
                                    </Stack>
                                }
                            >
                                <ListItemAvatar>
                                    <UserAvatar name={username} />
                                </ListItemAvatar>
                                <ListItemText primary={username} />
                            </ListItem>
                        ))}
                    </>
                )}
            </List>

            {/* Role Change Menu */}
            <Menu
                anchorEl={menuAnchor?.el}
                open={Boolean(menuAnchor)}
                onClose={() => {
                    setMenuAnchor(null);
                }}
                anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
                <MenuItem disabled>
                    <Typography variant="caption" color="text.secondary">
                        Change role
                    </Typography>
                </MenuItem>
                <Divider />
                <MenuItem
                    disabled
                    onClick={() => {
                        handleRoleSelect("ADMIN");
                    }}
                >
                    Admin
                </MenuItem>
                <MenuItem
                    disabled={currentUserRole === "USER"}
                    onClick={() => {
                        handleRoleSelect("MODERATOR");
                    }}
                >
                    Moderator
                </MenuItem>
                <MenuItem
                    onClick={() => {
                        handleRoleSelect("USER");
                    }}
                >
                    User
                </MenuItem>
            </Menu>

            {/* Remove Confirmation Dialog */}
            <Dialog
                open={removeDialogOpen}
                onClose={() => {
                    setRemoveDialogOpen(false);
                }}
                onTransitionExited={() => {
                    setRemoveConfirmation(null);
                }}
            >
                <DialogTitle>{removeConfirmation?.type === "member" ? "Remove member" : "Cancel invitation"}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {removeConfirmation?.type === "member"
                            ? `Are you sure you want to remove ${removeConfirmation.username} from this problem set?`
                            : `Are you sure you want to cancel the invitation for ${removeConfirmation?.username ?? "this user"}?`}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => {
                            setRemoveDialogOpen(false);
                        }}
                    >
                        Cancel
                    </Button>
                    <Button onClick={handleConfirmRemove} color="error" variant="contained">
                        Yes, remove user
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};
