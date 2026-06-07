import { FC, HTMLAttributes, useMemo } from "react";
import { Autocomplete, CircularProgress, ListItem, ListItemAvatar, ListItemText, TextField } from "@mui/material";
import { useTranslation } from "react-i18next";
import { useProblemSetRequest } from "@/features/problem-sets/api";
import { UserAvatar } from "@/shared/components/UserAvatar";

type SupervisorAutocompleteProps = {
    problemSetShareCode: string | null;
    value: string | null;
    onChange: (name: string | null) => void;
};

export const SupervisorAutocomplete: FC<SupervisorAutocompleteProps> = ({ problemSetShareCode, value, onChange }) => {
    const { t } = useTranslation("submissions");
    const { data: problemSet, isLoading } = useProblemSetRequest(problemSetShareCode ?? undefined);

    const options = useMemo<string[]>(() => {
        if (!problemSet) return [];
        const unique = new Set([...problemSet.admins, ...problemSet.moderators]);
        return [...unique].sort((a, b) => a.localeCompare(b));
    }, [problemSet]);

    return (
        <Autocomplete
            value={value}
            onChange={(_e, newValue) => {
                onChange(newValue);
            }}
            options={options}
            disabled={!problemSetShareCode}
            loading={isLoading}
            slotProps={{
                popper: {
                    sx: { zIndex: (theme) => theme.zIndex.snackbar + 2 },
                },
            }}
            filterOptions={(opts, state) => opts.filter((o) => o.toLowerCase().startsWith(state.inputValue.toLowerCase()))}
            noOptionsText={problemSetShareCode ? t("no_supervisors_found") : t("select_problem_set_first")}
            renderOption={({ key, ...rest }: HTMLAttributes<HTMLLIElement> & { key: string }, option) => (
                <ListItem key={key} {...rest} dense>
                    <ListItemAvatar sx={{ minWidth: 40 }}>
                        <UserAvatar name={option} size="small" />
                    </ListItemAvatar>
                    <ListItemText primary={option} />
                </ListItem>
            )}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label={t("supervisor_name_label")}
                    placeholder={t("select_supervisor_placeholder")}
                    size="small"
                    slotProps={{
                        input: {
                            ...params.InputProps,
                            endAdornment: (
                                <>
                                    {isLoading ? <CircularProgress size={14} /> : null}
                                    {params.InputProps.endAdornment}
                                </>
                            ),
                        },
                    }}
                />
            )}
        />
    );
};
