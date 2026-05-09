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
import { useTranslation } from "react-i18next";

interface ProblemSetUserManagementProps {
    shareCode: string;
}

const roleLabelKey: Record<string, string> = {
    ADMIN: "role_admin",
    MODERATOR: "role_moderator",
    USER: "role_user",
};

const roleColor: Record<string, "primary" | "secondary" | "default"> = {
    ADMIN: "primary",
    MODERATOR: "secondary",
    USER: "default",
};

type RemoveConfirmation = { username: string; type: "member" | "invited" };

export const ProblemSetUserManagement: FC<ProblemSetUserManagementProps> = ({ shareCode }) => {
    const { t } = useTranslation("problem-sets");
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
                        toast.info(t("invitation_removed_success", { username }));
                        void refetchInvitedUsers();
                    },
                    onError: () => {
                        toast.error(t("invitation_removed_error", { username }));
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
                <ListSubheader sx={{ bgcolor: "transparent", lineHeight: "36px" }}>{t("members_header")}</ListSubheader>
                {(!members || members.length === 0) && (
                    <ListItem>
                        <ListItemText secondary={t("no_members_yet")} />
                    </ListItem>
                )}
                {members?.map(({ name: username, role }) => (
                    <ListItem
                        key={username}
                        secondaryAction={
                            <Stack direction="row" alignItems="center" spacing={0.5}>
                                <Chip
                                    label={roleLabelKey[role] ? t(roleLabelKey[role]) : role}
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
                            {t("pending_invitations_header")}
                        </ListSubheader>
                        {invitedUsers.map((username) => (
                            <ListItem
                                key={`invited-${username}`}
                                secondaryAction={
                                    <Stack direction="row" alignItems="center" spacing={0.5}>
                                        <Chip
                                            label={t("role_pending")}
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
                        {t("change_role_header")}
                    </Typography>
                </MenuItem>
                <Divider />
                <MenuItem
                    disabled
                    onClick={() => {
                        handleRoleSelect("ADMIN");
                    }}
                >
                    {t("role_admin")}
                </MenuItem>
                <MenuItem
                    disabled={currentUserRole === "USER" || changeRoleMutation.isPending}
                    onClick={() => {
                        handleRoleSelect("MODERATOR");
                    }}
                >
                    {t("role_moderator")}
                </MenuItem>
                <MenuItem
                    disabled={changeRoleMutation.isPending}
                    onClick={() => {
                        handleRoleSelect("USER");
                    }}
                >
                    {t("role_user")}
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
                <DialogTitle>
                    {removeConfirmation?.type === "member" ? t("remove_member_title") : t("cancel_invitation_title")}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {removeConfirmation?.type === "member"
                            ? t("remove_member_confirmation", { username: removeConfirmation.username })
                            : t("cancel_invitation_confirmation", {
                                  username: removeConfirmation?.username ?? t("this_user"),
                              })}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => {
                            setRemoveDialogOpen(false);
                        }}
                    >
                        {t("cancel_button")}
                    </Button>
                    <Button
                        onClick={handleConfirmRemove}
                        color="error"
                        variant="contained"
                        disabled={changeRoleMutation.isPending || expireInviteMutation.isPending}
                    >
                        {t("confirm_remove_button")}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};
