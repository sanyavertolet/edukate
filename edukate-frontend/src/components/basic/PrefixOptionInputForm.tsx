import {Fragment, SyntheticEvent, useEffect, useState} from "react";
import { useDebounce } from "@uidotdev/usehooks";
import { useOptionsRequest } from "../../http/requests/requests";
import { Autocomplete, Chip, CircularProgress, TextField } from "@mui/material";

interface PrefixOptionInputFormProps {
    optionsUrl: string;
    selectedOptions: string[];
    onSelectedOptionsChange: (options: string[]) => void;
    debounceTime?: number;
    placeholderText?: string;
    label?: string
}

export function PrefixOptionInputForm(
    {
        optionsUrl,
        onSelectedOptionsChange,
        debounceTime = 500,
        selectedOptions = [],
        placeholderText,
        label
    } : PrefixOptionInputFormProps) {
    const [search, setSearch] = useState("");
    const [options, setOptions] = useState<string[]>([]);
    const { data, isLoading, error } = useDebounce(useOptionsRequest(optionsUrl, search), debounceTime);

    useEffect(() => { if (data && !isLoading && !error) { setOptions(data); }}, [data, isLoading, error]);

    const handleInputChange = (_: SyntheticEvent, value: string) => { setSearch(value); };
    const handleOptionSelect = (_: SyntheticEvent, newOptions: string[]) => {
        setSearch(""); onSelectedOptionsChange(newOptions);
    };

    return (
        <Autocomplete
            id="option-select-multiple"
            value={selectedOptions || null}
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
                    return (<Chip key={key} label={option}{...tagProps}/>);
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
                                    {isLoading ? <CircularProgress color="inherit" size={20}/> : null}
                                    {params.InputProps.endAdornment}
                                </Fragment>
                            ),
                        },
                    }}
                />
            )}
        />
    )
}
