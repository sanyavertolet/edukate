import { FC, useMemo } from "react";
import { defaultTooltipSlotProps } from "../../utils/utils";
import { IconButton, ListItem, ListItemText, Tooltip } from "@mui/material";
import UploadIcon from "@mui/icons-material/Upload";
import { FileMetadata } from "../../types/FileMetadata";
import { useFileStatsDisplayValues } from "../../hooks/useFileStatsDisplayValues";

type FileDragAndDropComponentProps = {
    files: FileMetadata[];
    maxFiles?: number;
    maxSize?: number;
    onUploadButtonClick?: () => void;
};

export const FileDragAndDropComponent: FC<FileDragAndDropComponentProps> = (
    { files, maxFiles, maxSize, onUploadButtonClick }
) => {
    const uploadSecondaryAction = useMemo(
        () => onUploadButtonClick && (
            <Tooltip title={"Upload files"} slotProps={defaultTooltipSlotProps}>
                <IconButton color={"primary"} edge="end" aria-label="delete" onClick={onUploadButtonClick}>
                    <UploadIcon/>
                </IconButton>
            </Tooltip>
        ),
        [onUploadButtonClick]
    );

    const headerListItemSx = {
        backgroundColor: 'rgb(0,0,0,0.04)', mb: 1, borderRadius: 1, border: '1px solid', borderColor: 'divider',
    };

    const { primaryText, secondaryText } = useFileStatsDisplayValues({ files, maxFiles, maxSize });
    return (
        <ListItem sx={headerListItemSx} secondaryAction={uploadSecondaryAction}>
            <ListItemText primary={primaryText} secondary={secondaryText}/>
        </ListItem>
    );
};
