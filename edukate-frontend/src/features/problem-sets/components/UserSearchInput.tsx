import { FC, SyntheticEvent, HTMLAttributes, useState } from "react";
import { useProblemSetCreateInvitationMutation } from "@/features/problem-sets/api";
import { useDebounce } from "@/shared/hooks/useDebounce";
import { useOptionsRequest } from "@/shared/hooks/useOptionsRequest";
import { toast } from "react-toastify";
import { Autocomplete, ListItem, ListItemAvatar, ListItemText, TextField } from "@mui/material";
import { UserAvatar } from "@/shared/components/UserAvatar";
import { useTranslation } from "react-i18next";

interface UserSearchInputProps {
    problemSetShareCode: string;
    onInvited?: (username: string) => void;
}

export const UserSearchInput: FC<UserSearchInputProps> = ({ problemSetShareCode, onInvited }) => {
    const { t } = useTranslation("problem-sets");
    const [inputValue, setInputValue] = useState("");
    const debouncedInput = useDebounce(inputValue, 300);
    const { data: options, isLoading } = useOptionsRequest("/api/v1/users/by-prefix", debouncedInput, 5, {
        problemSetShareCode,
    });
    const createInvitationMutation = useProblemSetCreateInvitationMutation();

    const handleSelect = (_: SyntheticEvent, value: string | null) => {
        if (!value) return;
        createInvitationMutation.mutate(
            { inviteeName: value, shareCode: problemSetShareCode },
            {
                onSuccess: () => {
                    toast.success(t("user_invited_success", { username: value }));
                    setInputValue("");
                    onInvited?.(value);
                },
                onError: () => {
                    toast.error(t("user_invited_error", { username: value }));
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
            noOptionsText={inputValue.length > 0 ? t("no_users_found") : t("type_to_search")}
            renderOption={({ key, ...rest }: HTMLAttributes<HTMLLIElement> & { key: string }, option) => (
                <ListItem key={key} {...rest} dense>
                    <ListItemAvatar sx={{ minWidth: 40 }}>
                        <UserAvatar name={option} size="small" />
                    </ListItemAvatar>
                    <ListItemText primary={option} />
                </ListItem>
            )}
            renderInput={(params) => (
                <TextField {...params} placeholder={t("search_users_placeholder")} size="small" variant="standard" />
            )}
            sx={{ px: 1.5, py: 0.5 }}
        />
    );
};
