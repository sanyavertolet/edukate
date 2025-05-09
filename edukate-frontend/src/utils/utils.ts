import { SlotProps } from "@mui/material";
import { AdditionalNavigationElement } from "../components/topbar/AdditionalNavigationElement";

// eslint-disable-next-line
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

export function sizeOf(selectedFiles: File[]) {
    if (selectedFiles.length === 0) return 0;
    return selectedFiles.map(it => it.size).reduce((prev, current) => prev + current);
}

export function formatFileSize(bytes: number) {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const dm = 2;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
}
