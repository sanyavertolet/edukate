import {FC, useEffect, useRef} from "react";
import {
    IconButton, List, ListItem, ListItemIcon, ListItemText, Box, Typography, Input, Tooltip
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import {defaultTooltipSlotProps, formatFileSize} from "../../utils/utils";
import { FileStatusIcon } from "./FileStatusIcon";
import { useFileUpload } from "../../hooks/useFileUpload";
import { useFileStatsDisplayValues } from "../../hooks/useFileStatsDisplayValues";
import UploadIcon from "@mui/icons-material/Upload";
import { FilePreviewDialog } from "./FilePreviewDialog";
import { toast } from "react-toastify";

type MobileFileInputComponentProps = {
    onTempFileUploaded: (fileKey: string) => void;
    onTempFileDeleted: (fileKey: string) => void;
    maxFiles?: number;
    maxSize?: number;
    accept?: string;
};

export const MobileFileInputComponent: FC<MobileFileInputComponentProps> = (
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

    const { primaryText, secondaryText } = useFileStatsDisplayValues({ files: fileMetadataList, maxFiles, maxSize });

    useEffect(() => {
        if (errorText) {
            toast.error(errorText);
        }
    }, [errorText]);
    return (
        <List sx={{ width: '100%', p: 0 }}>
            <Input type="file" style={{ display: "none" }} onChange={handleAddFiles} inputRef={fileInputRef}
                   inputProps={{ multiple: true, accept: accept }}
            />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                    <Typography variant="subtitle1" textAlign={"start"}>{ primaryText }</Typography>
                    <Typography variant="caption" textAlign={"start"} color="text.secondary">{ secondaryText }</Typography>
                </Box>
                <Tooltip title={"Upload files"} slotProps={defaultTooltipSlotProps}>
                    <IconButton color={"primary"} edge="end" aria-label="delete" onClick={onUploadButtonClick}
                                disabled={fileMetadataList.length == maxFiles}>
                        <UploadIcon/>
                    </IconButton>
                </Tooltip>
            </Box>

            { fileMetadataList.map((file) => (
                <ListItem
                    key={file.key}
                    sx={{ ...listItemSx, cursor: file.status === 'success' ? 'pointer' : 'default' }}
                    onClick={() => file.status === 'success' && handleFileClick(file.key)}
                    secondaryAction={
                        <IconButton
                            edge="end" aria-label="delete" disabled={file.status === 'uploading'}
                            onClick={(e) => {
                                e.stopPropagation();
                                handleRemoveFile(file.key);
                            }}
                        >
                            <DeleteIcon />
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
