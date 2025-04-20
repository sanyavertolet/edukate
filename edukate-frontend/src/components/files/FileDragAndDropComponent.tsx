import { FC, useMemo } from "react";
import { defaultTooltipSlotProps, formatFileSize } from "../../utils/utils";
import { IconButton, ListItem, ListItemText, Tooltip } from "@mui/material";
import UploadIcon from "@mui/icons-material/Upload";
import { FileMetadata } from "../../types/FileMetadata";

type FileDragAndDropComponentProps = {
    files: FileMetadata[];
    maxFiles?: number;
    maxSize?: number;
    onUploadButtonClick?: () => void;
};

export const FileDragAndDropComponent: FC<FileDragAndDropComponentProps> = (
    {files, maxFiles, maxSize, onUploadButtonClick}
) => {
    const currentSize = useMemo(
        () => files.reduce((total, file) => total + file.size, 0),
        [files]
    );
    const uploadSecondaryAction = onUploadButtonClick ? (
        <Tooltip title={"Upload files"} slotProps={defaultTooltipSlotProps}>
            <IconButton color={"primary"} edge="end" aria-label="delete" onClick={onUploadButtonClick}>
                <UploadIcon/>
            </IconButton>
        </Tooltip>
    ) : undefined;

    const displayValue: (current: string | number, maximum?: string | number, postfix?: string) => string = (
        current, maximum, postfix
    ) =>  { return `${current}${maximum ? `/${maximum}` : ""}${postfix ? ` ${postfix}` : ""}` };

    const headerListItemSx = {
        backgroundColor: 'rgb(0,0,0,0.04)', mb: 1, borderRadius: 1,
        border: '1px solid', borderColor: 'divider',
    };

    return files.length > 0 ? (
        <ListItem sx={headerListItemSx} secondaryAction={uploadSecondaryAction}>
            <ListItemText primary={displayValue(files.length, maxFiles, `file${files.length == 1 ? "" : "s"} selected`)}
                          secondary={displayValue(formatFileSize(currentSize), maxSize && formatFileSize(maxSize))}/>
        </ListItem>
    ) : (
        <ListItem sx={headerListItemSx} secondaryAction={uploadSecondaryAction}>
            <ListItemText primary={"No files selected..."}
                          secondary={"Yet."}/>
        </ListItem>
    );
};
