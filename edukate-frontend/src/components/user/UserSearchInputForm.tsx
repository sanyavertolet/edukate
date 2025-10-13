import { FC, KeyboardEventHandler, useState } from "react";
import { useBundleInviteUserMutation } from "../../http/requests";
import { toast } from "react-toastify";
import { IconButton, InputBase, Paper } from "@mui/material";
import AddIcon from "@mui/icons-material/AddOutlined";

interface UserSearchInputFormProps {
    bundleShareCode: string;
    onInvited?: (username: string) => void;
}

export const UserSearchInputForm: FC<UserSearchInputFormProps> = ({ bundleShareCode, onInvited }) => {
    const [username, setUsername] = useState("");
    const inviteUserMutation = useBundleInviteUserMutation();

    const canInvite = username.trim().length > 0 && !inviteUserMutation.isPending;

    const onAddClick = () => {
        if (!canInvite) return;
        const normalized = username.trim();
        inviteUserMutation.mutate({ username: normalized, shareCode: bundleShareCode }, {
            onSuccess: () => {
                toast.success(`User ${normalized} has been invited!`);
                setUsername("");
                onInvited?.(normalized);
            },
            onError: () => {
                toast.error(`Could not invite ${normalized}!`);
            },
        });
    };

    const onKeyDown: KeyboardEventHandler<HTMLInputElement> = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            onAddClick();
        }
    }

    return (
        <Paper
            component="form" variant={"outlined"}
            sx={{ p: '0px 1px', display: 'flex', alignItems: 'center', border: 0 }}
            onSubmit={(e) => { e.preventDefault(); onAddClick(); }}
        >
            <InputBase
                sx={{ ml: 1, flex: 1 }} placeholder="Username" value={username}
                onChange={(e) => setUsername(e.target.value)} onKeyDown={onKeyDown}
                inputProps={{ 'aria-label': 'username' }}
            />
            <IconButton color="primary" aria-label="add user" onClick={onAddClick} disabled={!canInvite}>
                <AddIcon/>
            </IconButton>
        </Paper>
    );
};
