import { FC, useMemo } from "react";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { IconButton, ListItem, ListItemText, Tooltip } from "@mui/material";
import UploadIcon from "@mui/icons-material/Upload";
import { FileMetadata } from "@/features/files/types";
import { useFileStatsDisplayValues } from "@/features/files/hooks/useFileStatsDisplayValues";
import { useTranslation } from "react-i18next";

type FileDragAndDropProps = {
    files: FileMetadata[];
    maxFiles?: number;
    maxSize?: number;
    onUploadButtonClick?: () => void;
};

const headerListItemSx = {
    backgroundColor: "action.hover",
    mb: 1,
    borderRadius: 1,
    border: "1px solid",
    borderColor: "divider",
} as const;

export const FileDragAndDrop: FC<FileDragAndDropProps> = ({ files, maxFiles, maxSize, onUploadButtonClick }) => {
    const { t } = useTranslation();
    const uploadSecondaryAction = useMemo(
        () =>
            onUploadButtonClick && (
                <Tooltip title={t("upload_files_label")} slotProps={defaultTooltipSlotProps}>
                    <IconButton color={"primary"} edge="end" aria-label="Upload files" onClick={onUploadButtonClick}>
                        <UploadIcon />
                    </IconButton>
                </Tooltip>
            ),
        [onUploadButtonClick, t],
    );

    const { primaryText, secondaryText } = useFileStatsDisplayValues({ files, maxFiles, maxSize });
    return (
        <ListItem sx={headerListItemSx} secondaryAction={uploadSecondaryAction}>
            <ListItemText primary={primaryText} secondary={secondaryText} />
        </ListItem>
    );
};
