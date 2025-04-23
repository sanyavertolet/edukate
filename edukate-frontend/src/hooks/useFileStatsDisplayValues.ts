import { useMemo } from "react";
import { formatFileSize } from "../utils/utils";
import { FileMetadata } from "../types/FileMetadata";

type UseFileStatsDisplayValuesProps = {
    files: FileMetadata[];
    maxFiles?: number;
    maxSize?: number;
};

function displayValue(current: string | number, maximum?: string | number, postfix?: string) {
    return `${current}${maximum ? `/${maximum}` : ""}${postfix ? ` ${postfix}` : ""}`
}

export const useFileStatsDisplayValues= ({
    files, maxFiles, maxSize,
}: UseFileStatsDisplayValuesProps) => {
    const currentSize = useMemo(
        () => files.reduce((total, file) => total + file.size, 0),
        [files]
    );

    const primaryText = useMemo(
        () => files.length > 0 ? displayValue(files.length, maxFiles, `file${files.length == 1 ? "" : "s"} selected`)
            : "No files selected...",
        [files.length, maxFiles]
    );

    const secondaryText = useMemo(
        () => files.length > 0 ? displayValue(formatFileSize(currentSize), maxSize && formatFileSize(maxSize))
            : "Yet.",
        [currentSize, maxSize, files.length]
    );

    return {
        primaryText,
        secondaryText,
    };
};