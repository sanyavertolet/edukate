import { tooltipClasses } from "@mui/material/Tooltip";

export const defaultTooltipSlotProps = {
    popper: {
        sx: {
            [`&.${tooltipClasses.popper}[data-popper-placement*="bottom"] .${tooltipClasses.tooltip}`]: {
                marginTop: "5px",
            },
            [`&.${tooltipClasses.popper}[data-popper-placement*="top"] .${tooltipClasses.tooltip}`]: {
                marginBottom: "5px",
            },
            [`&.${tooltipClasses.popper}[data-popper-placement*="right"] .${tooltipClasses.tooltip}`]: {
                marginLeft: "5px",
            },
            [`&.${tooltipClasses.popper}[data-popper-placement*="left"] .${tooltipClasses.tooltip}`]: {
                marginRight: "5px",
            },
        },
    },
};

export const getFirstLetters = (str: string, n: number) => str.substring(0, n);

export function sizeOf(selectedFiles: File[]) {
    if (selectedFiles.length === 0) return 0;
    return selectedFiles.map((it) => it.size).reduce((prev, current) => prev + current);
}

export function formatFileSize(bytes: number) {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const dm = 2;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${String(parseFloat((bytes / Math.pow(k, i)).toFixed(dm)))} ${sizes[i]}`;
}

export function getColorByStringHash(str: string) {
    const colors = [
        "#F44336",
        "#E91E63",
        "#9C27B0",
        "#673AB7",
        "#3F51B5",
        "#2196F3",
        "#03A9F4",
        "#00BCD4",
        "#009688",
        "#4CAF50",
        "#8BC34A",
        "#CDDC39",
    ];
    let hash = 0;
    for (let i = 0; i < str.length; ++i) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}
