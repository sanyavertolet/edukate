import { Box } from "@mui/material";
import { PrefixOptionInputForm } from "./PrefixOptionInputForm";

export interface OptionPickerComponentProps {
    optionsUrl: string;
    selectedOptions: string[];
    debounceTime?: number;
    onOptionsChange: (options: string[]) => void;
    placeholderText?: string;
    label?: string;
}

export function OptionPickerComponent(
    {optionsUrl, selectedOptions = [], debounceTime = 500, onOptionsChange, placeholderText, label}: OptionPickerComponentProps
) {
    const onSelectedOptionsChange = (options: string[]) => {
        onOptionsChange && onOptionsChange(options)
    };

    return (
        <Box maxWidth={"lg"}>
            <PrefixOptionInputForm
                selectedOptions={selectedOptions}
                optionsUrl={optionsUrl}
                placeholderText={placeholderText}
                onSelectedOptionsChange={onSelectedOptionsChange}
                debounceTime={debounceTime}
                label={label}
            />
        </Box>
    );
}
