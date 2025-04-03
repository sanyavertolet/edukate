import { AxiosResponse } from "axios";
import { SlotProps } from "@mui/material";

export const fullStatus = (response: AxiosResponse | undefined) => {
    const statusCode = response?.status
    const statusText = response?.statusText;
    if (statusText && statusCode) {
        return `${statusCode} ${statusText}`;
    } else if (statusCode) {
        return `${statusCode}`;
    } else {
        return "Unknown Status"
    }
};

export const defaultTooltipSlotProps: SlotProps<any, any, any> = {
    popper: {
        modifiers: [
            { name: 'offset', options: { offset: [0, -14] } },
        ],
    }
};
