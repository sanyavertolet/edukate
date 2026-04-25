import React, { FC, SyntheticEvent, useState } from "react";
import { useProblemSetInviteUserMutation } from "@/features/problem-sets/api";
import { useDebounce } from "@/shared/hooks/useDebounce";
import { useOptionsRequest } from "@/shared/hooks/useOptionsRequest";
import { toast } from "react-toastify";
import { Autocomplete, ListItem, ListItemAvatar, ListItemText, TextField } from "@mui/material";
import { UserAvatar } from "@/shared/components/UserAvatar";

interface UserSearchInputProps {
    problemSetShareCode: string;
    onInvited?: (username: string) => void;
}

export const UserSearchInput: FC<UserSearchInputProps> = ({ problemSetShareCode, onInvited }) => {
    const [inputValue, setInputValue] = useState("");
    const debouncedInput = useDebounce(inputValue, 300);
    const { data: options, isLoading } = useOptionsRequest("/api/v1/users/by-prefix", debouncedInput, 5, {
        problemSetShareCode,
    });
    const inviteUserMutation = useProblemSetInviteUserMutation();

    const handleSelect = (_: SyntheticEvent, value: string | null) => {
        if (!value) return;
        inviteUserMutation.mutate(
            { username: value, shareCode: problemSetShareCode },
            {
                onSuccess: () => {
                    toast.success(`User ${value} has been invited!`);
                    setInputValue("");
                    onInvited?.(value);
                },
                onError: () => {
                    toast.error(`Could not invite ${value}!`);
                },
            },
        );
    };

    return (
        <Autocomplete
            freeSolo={false}
            value={null}
            inputValue={inputValue}
            onInputChange={(_, newValue) => {
                setInputValue(newValue);
            }}
            onChange={handleSelect}
            options={options ?? []}
            loading={isLoading}
            filterOptions={(x) => x}
            noOptionsText={inputValue.length > 0 ? "No users found" : "Type to search"}
            renderOption={({ key, ...rest }: React.HTMLAttributes<HTMLLIElement> & { key: string }, option) => (
                <ListItem key={key} {...rest} dense>
                    <ListItemAvatar sx={{ minWidth: 40 }}>
                        <UserAvatar name={option} size="small" />
                    </ListItemAvatar>
                    <ListItemText primary={option} />
                </ListItem>
            )}
            renderInput={(params) => (
                <TextField {...params} placeholder="Search users to invite..." size="small" variant="standard" />
            )}
            sx={{ px: 1.5, py: 0.5 }}
        />
    );
};
