import { FC, HTMLAttributes, MouseEvent, useMemo, useState } from "react";
import { Autocomplete, AutocompleteRenderInputParams, CircularProgress, TextField } from "@mui/material";
import Fuse from "fuse.js";
import { useTranslation } from "react-i18next";
import { ProblemSetMetadata } from "@/features/problem-sets/types";
import { ProblemSetMetadataCurrentUserRole } from "@/generated/backend";
import { useMemberProblemSetsSearch } from "@/features/problem-sets/api";
import { ProblemSetOption } from "./ProblemSetOption";
import { colorForRole } from "./roleColor";

const DEFAULT_INITIAL_OPTIONS = 5;
const MAX_FUZZY_RESULTS = 5;

type ProblemSetSelectorInputProps = AutocompleteRenderInputParams & {
    label: string;
    placeholder: string;
    isLoading: boolean;
    selectedRole: ProblemSetMetadataCurrentUserRole | undefined;
};

const ProblemSetSelectorInput: FC<ProblemSetSelectorInputProps> = ({
    label,
    placeholder,
    isLoading,
    selectedRole,
    ...params
}) => (
    <TextField
        {...params}
        label={label}
        placeholder={placeholder}
        size="small"
        sx={{
            "& .MuiInputBase-input": {
                color: colorForRole(selectedRole),
                fontWeight: selectedRole === "ADMIN" ? 600 : undefined,
            },
        }}
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
);

type ProblemSetSelectorProps = {
    value: ProblemSetMetadata | null;
    onChange: (ps: ProblemSetMetadata | null) => void;
    problemKey?: string;
};

export const ProblemSetSelector: FC<ProblemSetSelectorProps> = ({ value, onChange, problemKey }) => {
    const { t } = useTranslation("submissions");
    const { data: options, isLoading } = useMemberProblemSetsSearch(problemKey);
    const [expandedCode, setExpandedCode] = useState<string | null>(null);
    const [inputValue, setInputValue] = useState("");

    const fuse = useMemo(
        () =>
            new Fuse(options, {
                keys: [
                    { name: "name", weight: 0.7 },
                    { name: "description", weight: 0.3 },
                ],
                threshold: 0.4,
                ignoreLocation: true,
            }),
        [options],
    );

    const filteredOptions = useMemo(() => {
        const trimmed = inputValue.trim();
        if (!trimmed) return options.slice(0, DEFAULT_INITIAL_OPTIONS);
        const exact = options.find((o) => o.shareCode === trimmed);
        if (exact) return [exact];
        return fuse.search(trimmed, { limit: MAX_FUZZY_RESULTS }).map((r) => r.item);
    }, [options, inputValue, fuse]);

    const toggleExpand = (e: MouseEvent, shareCode: string) => {
        e.stopPropagation();
        e.preventDefault();
        setExpandedCode((prev) => (prev === shareCode ? null : shareCode));
    };

    return (
        <Autocomplete
            value={value}
            onChange={(_e, newValue) => {
                onChange(newValue);
            }}
            inputValue={inputValue}
            onInputChange={(_e, newValue) => {
                setInputValue(newValue);
            }}
            openOnFocus
            options={filteredOptions}
            filterOptions={(opts) => opts}
            loading={isLoading}
            slotProps={{
                popper: {
                    sx: { zIndex: (theme) => theme.zIndex.snackbar + 2 },
                },
            }}
            getOptionLabel={(opt) => opt.name}
            isOptionEqualToValue={(opt, val) => opt.shareCode === val.shareCode}
            noOptionsText={t("no_problem_sets_found")}
            renderOption={({ key, ...listItemProps }: HTMLAttributes<HTMLLIElement> & { key: string }, option) => (
                <ProblemSetOption
                    key={key}
                    option={option}
                    isExpanded={expandedCode === option.shareCode}
                    onToggleExpand={(e) => {
                        toggleExpand(e, option.shareCode);
                    }}
                    listItemProps={listItemProps}
                />
            )}
            renderInput={(params) => (
                <ProblemSetSelectorInput
                    {...params}
                    label={t("problem_set_code_label")}
                    placeholder={t("select_problem_set_placeholder")}
                    isLoading={isLoading}
                    selectedRole={value?.currentUserRole}
                />
            )}
        />
    );
};
