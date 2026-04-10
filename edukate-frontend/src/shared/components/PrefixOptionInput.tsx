import { Fragment, SyntheticEvent, useEffect, useState } from "react";
import { useDebounce } from "@/shared/hooks/useDebounce";
import { useOptionsRequest } from "@/shared/hooks/useOptionsRequest";
import { Autocomplete, Chip, CircularProgress, TextField } from "@mui/material";

interface PrefixOptionInputFormProps {
    optionsUrl: string;
    selectedOptions: string[];
    onSelectedOptionsChange: (options: string[]) => void;
    debounceTime?: number;
    placeholderText?: string;
    label?: string;
}

export function PrefixOptionInputForm({
    optionsUrl,
    onSelectedOptionsChange,
    debounceTime = 500,
    selectedOptions = [],
    placeholderText,
    label,
}: PrefixOptionInputFormProps) {
    const [search, setSearch] = useState("");
    const [options, setOptions] = useState<string[]>([]);
    const debouncedSearch = useDebounce(search, debounceTime);
    const { data, isLoading, error } = useOptionsRequest(optionsUrl, debouncedSearch);

    useEffect(() => {
        // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition -- data is undefined before fetch completes
        if (data && !isLoading && !error) {
            setOptions(data);
        }
    }, [data, isLoading, error]);

    const handleInputChange = (_: SyntheticEvent, value: string) => {
        setSearch(value);
    };
    const handleOptionSelect = (_: SyntheticEvent, newOptions: string[]) => {
        setSearch("");
        onSelectedOptionsChange(newOptions);
    };

    return (
        <Autocomplete
            id="option-select-multiple"
            value={selectedOptions}
            options={options}
            onChange={handleOptionSelect}
            loading={isLoading}
            inputValue={search}
            onInputChange={handleInputChange}
            multiple
            filterOptions={(x) => x}
            renderTags={(tagValue, getTagProps) =>
                tagValue.map((option, index) => {
                    const { key, ...tagProps } = getTagProps({ index });
                    return <Chip key={key} label={option} {...tagProps} />;
                })
            }
            renderInput={(params) => (
                <TextField
                    {...params}
                    required
                    fullWidth
                    label={label}
                    placeholder={placeholderText}
                    slotProps={{
                        input: {
                            ...params.InputProps,
                            endAdornment: (
                                <Fragment>
                                    {isLoading ? <CircularProgress color="inherit" size={20} /> : null}
                                    {params.InputProps.endAdornment}
                                </Fragment>
                            ),
                        },
                    }}
                />
            )}
        />
    );
}
