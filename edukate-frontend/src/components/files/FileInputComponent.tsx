import {FC, useEffect, useRef} from "react";
import { IconButton, List, ListItem, ListItemIcon, ListItemText, Tooltip, Box, Typography, Input } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { defaultTooltipSlotProps, formatFileSize } from "../../utils/utils";
import { FileDragAndDropComponent } from "./FileDragAndDropComponent";
import { FileStatusIcon } from "./FileStatusIcon";
import { useFileUpload } from "../../hooks/useFileUpload";
import { FilePreviewDialog } from "./FilePreviewDialog";
import { toast } from "react-toastify";

type FileInputComponentProps = {
    onTempFileUploaded: (fileKey: string) => void;
    onTempFileDeleted: (fileKey: string) => void;
    maxFiles?: number;
    maxSize?: number;
    accept?: string;
};

export const FileInputComponent: FC<FileInputComponentProps> = (
    { onTempFileUploaded, onTempFileDeleted, maxFiles = 5, maxSize = 50 * 1024 * 1024, accept = "*" }
) => {
    const {
        fileMetadataList, previewDialogOpen, selectedFileKey, errorText,
        handleFileClick, handleClosePreview, handleAddFiles, handleRemoveFile
    } = useFileUpload({ onTempFileUploaded, onTempFileDeleted, maxFiles, maxSize });

    const fileInputRef = useRef<HTMLInputElement>(null);
    const onUploadButtonClick = () => { fileInputRef.current?.click(); };
    const listItemSx = {
        backgroundColor: 'background.paper', mb: 1, borderRadius: 1, border: '1px solid', borderColor: 'divider',
    };

    useEffect(() => {
        if (errorText) {
            toast.error(errorText);
        }
    }, [errorText]);
    
    return (
        <List>
            <Input type="file" style={{ display: "none" }} onChange={handleAddFiles} inputRef={fileInputRef}
                   inputProps={{ multiple: true, accept: accept }}/>
            <FileDragAndDropComponent files={fileMetadataList} onUploadButtonClick={onUploadButtonClick}
                                      maxFiles={maxFiles} maxSize={maxSize}/>
            { fileMetadataList.map((file) => (
                <ListItem
                    key={file.key}
                    sx={{ ...listItemSx, cursor: file.status === 'success' ? 'pointer' : 'default' }}
                    onClick={() => file.status === 'success' && handleFileClick(file.key)}
                    secondaryAction={
                        <IconButton edge="end" aria-label="delete" onClick={(e) => {
                            e.stopPropagation();
                            handleRemoveFile(file.key);
                        }}
                                    disabled={file.status === 'uploading'}>
                            <Tooltip title={"Remove file"} slotProps={defaultTooltipSlotProps}>
                                <DeleteIcon/>
                            </Tooltip>
                        </IconButton>
                    }
                >
                    <ListItemIcon><FileStatusIcon file={file}/></ListItemIcon>

                    <ListItemText
                        primary={
                            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                                <Typography noWrap>{file.key}</Typography>
                                {file.status === 'uploading' && file.progress !== undefined && (
                                    <Box sx={{ width: '100%', mt: 1 }}>
                                        <Typography variant="caption" color="text.secondary">
                                            {file.progress}%
                                        </Typography>
                                    </Box>
                                )}
                            </Box>
                        }
                        secondary={formatFileSize(file.size)}
                        slotProps={{ primary: { noWrap: true } }}
                    />
                </ListItem>
            ))}

            <FilePreviewDialog open={previewDialogOpen} fileKey={selectedFileKey} onClose={handleClosePreview}/>
        </List>
    );
};
