import { useMemo } from "react";
import { formatFileSize } from "@/shared/utils/utils";
import { FileMetadata } from "@/features/files/types";
import { useTranslation } from "react-i18next";

type UseFileStatsDisplayValuesProps = {
    files: FileMetadata[];
    maxFiles?: number;
    maxSize?: number;
};

function displayValue(current: string | number, maximum?: string | number, postfix?: string) {
    return `${String(current)}${maximum ? `/${String(maximum)}` : ""}${postfix ? ` ${postfix}` : ""}`;
}

export const useFileStatsDisplayValues = ({ files, maxFiles, maxSize }: UseFileStatsDisplayValuesProps) => {
    const { t } = useTranslation();
    const currentSize = useMemo(() => files.reduce((total, file) => total + file.size, 0), [files]);

    const primaryText = useMemo(
        () => (files.length > 0 ? displayValue(files.length, maxFiles, t("files_selected")) : t("no_files_selected")),
        [files.length, maxFiles, t],
    );

    const secondaryText = useMemo(
        () =>
            files.length > 0
                ? displayValue(formatFileSize(currentSize), maxSize && formatFileSize(maxSize))
                : t("no_files_yet"),
        [currentSize, maxSize, files.length, t],
    );

    return {
        primaryText,
        secondaryText,
    };
};
