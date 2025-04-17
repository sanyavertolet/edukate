import { FC, useMemo } from "react";
import { defaultTooltipSlotProps, formatFileSize, sizeOf } from "../../utils/utils";
import { IconButton, ListItem, ListItemText, Tooltip } from "@mui/material";
import UploadIcon from "@mui/icons-material/Upload";
import { ExtendedFile } from "./ExtendedFile";

type SelectedFilesHeaderComponentProps = {
    files: ExtendedFile[];
    maxFiles?: number;
    maxSize?: number;
    onUploadButtonClick?: () => void;
};

export const SelectedFilesHeaderComponent: FC<SelectedFilesHeaderComponentProps> = (
    {files, maxFiles, maxSize, onUploadButtonClick}
) => {
    const currentSize = useMemo(() => sizeOf(files.map(it => it.content)), [files]);
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
