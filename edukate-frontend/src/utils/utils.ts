import { AxiosResponse } from "axios";
import { SlotProps } from "@mui/material";
import { AdditionalNavigationElement } from "../components/topbar/AdditionalNavigationElement";

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

export const areNavigationsEqual = (nav1: AdditionalNavigationElement[], nav2: AdditionalNavigationElement[]) => {
    if (nav1.length !== nav2.length) return false;
    for (let i = 0; i < nav1.length; i++) {
        if (nav1[i].text !== nav2[i].text) return false;
        if (nav1[i].isSelected !== nav2[i].isSelected) return false;
    }
    return true;
};
