import { FC } from "react";
import { IconButton, List, ListItem, ListItemIcon, ListItemText, Tooltip } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile";
import { defaultTooltipSlotProps, formatFileSize } from "../../utils/utils";
import { SelectedFilesHeaderComponent } from "./SelectedFilesHeaderComponent";

type SelectedFilesComponentProps = {
    files: File[];
    isLoading: boolean;
    handleRemoveFile: (index: number) => void;
    onUploadButtonClick?: () => void;
    maxFiles?: number;
    maxSize?: number;
};

export const SelectedFilesComponent: FC<SelectedFilesComponentProps> = (
    {files, isLoading, handleRemoveFile, onUploadButtonClick, maxFiles, maxSize}
) => {
    const listItemSx = {
        backgroundColor: 'background.paper', mb: 1, borderRadius: 1, border: '1px solid', borderColor: 'divider',
    };

    return (
        <List>
            <SelectedFilesHeaderComponent
                files={files} onUploadButtonClick={onUploadButtonClick} maxFiles={maxFiles} maxSize={maxSize}/>

            { files.length > 0 && files.map((file, index) => (
                <ListItem key={`${index}-${file.name}`} sx={listItemSx} secondaryAction={
                    <IconButton edge="end" aria-label="delete" disabled={isLoading}
                                onClick={() => handleRemoveFile(index)}>
                        <Tooltip title={"Remove file"} slotProps={defaultTooltipSlotProps}>
                            <DeleteIcon/>
                        </Tooltip>
                    </IconButton>
                }>
                    <ListItemIcon> <InsertDriveFileIcon/> </ListItemIcon>
                    <ListItemText primary={file.name} secondary={formatFileSize(file.size)}
                                  slotProps={{ primary: { noWrap: true } }}/>
                </ListItem>
            ))}
        </List>
    );
};
