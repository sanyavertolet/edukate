import { SlotProps } from "@mui/material";
import { AdditionalNavigationElement } from "../components/topbar/AdditionalNavigationElement";
import { tooltipClasses } from "@mui/material/Tooltip";

// eslint-disable-next-line
export const defaultTooltipSlotProps: SlotProps<any, any, any> = {
    popper: {
        sx: {
            [`&.${tooltipClasses.popper}[data-popper-placement*="bottom"] .${tooltipClasses.tooltip}`]:
                {
                    marginTop: '0px',
                },
            [`&.${tooltipClasses.popper}[data-popper-placement*="top"] .${tooltipClasses.tooltip}`]:
                {
                    marginBottom: '0px',
                },
            [`&.${tooltipClasses.popper}[data-popper-placement*="right"] .${tooltipClasses.tooltip}`]:
                {
                    marginLeft: '0px',
                },
            [`&.${tooltipClasses.popper}[data-popper-placement*="left"] .${tooltipClasses.tooltip}`]:
                {
                    marginRight: '0px',
                },
        },
    },
};

export const getFirstLetters = (str: string, n: number) => str.substring(0, n);

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

export function getColorByStringHash(str: string) {
    const colors = [
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
        "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    ];
    let hash = 0;
    for (let i = 0; i < str.length; ++i) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}
