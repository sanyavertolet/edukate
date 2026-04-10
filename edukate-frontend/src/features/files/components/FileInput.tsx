import { FC, useEffect, useRef } from "react";
import { IconButton, List, ListItem, ListItemIcon, ListItemText, Tooltip, Box, Typography, Input } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { defaultTooltipSlotProps, formatFileSize } from "@/shared/utils/utils";
import { FileDragAndDrop } from "./FileDragAndDrop";
import { FileStatusIcon } from "./FileStatusIcon";
import { useFileUpload } from "@/features/files/hooks/useFileUpload";
import { FilePreviewDialog } from "./FilePreviewDialog";
import { toast } from "react-toastify";

type FileInputProps = {
    onTempFileUploaded: (fileKey: string) => void;
    onTempFileDeleted: (fileKey: string) => void;
    maxFiles?: number;
    maxSize?: number;
    accept?: string;
};

const listItemSx = {
    backgroundColor: "background.paper",
    mb: 1,
    borderRadius: 1,
    border: "1px solid",
    borderColor: "divider",
} as const;

export const FileInput: FC<FileInputProps> = ({
    onTempFileUploaded,
    onTempFileDeleted,
    maxFiles = 5,
    maxSize = 50 * 1024 * 1024,
    accept = "*",
}) => {
    const {
        fileMetadataList,
        previewDialogOpen,
        selectedFileKey,
        errorText,
        handleFileClick,
        handleClosePreview,
        handleAddFiles,
        handleRemoveFile,
    } = useFileUpload({ onTempFileUploaded, onTempFileDeleted, maxFiles, maxSize });

    const fileInputRef = useRef<HTMLInputElement>(null);
    const onUploadButtonClick = () => {
        fileInputRef.current?.click();
    };

    useEffect(() => {
        if (errorText) {
            toast.error(errorText);
        }
    }, [errorText]);

    return (
        <List>
            <Input
                type="file"
                style={{ display: "none" }}
                onChange={handleAddFiles}
                inputRef={fileInputRef}
                inputProps={{ multiple: true, accept: accept }}
            />
            <FileDragAndDrop
                files={fileMetadataList}
                onUploadButtonClick={onUploadButtonClick}
                maxFiles={maxFiles}
                maxSize={maxSize}
            />
            {fileMetadataList.map((file) => (
                <ListItem
                    key={file.key}
                    role={file.status === "success" ? "button" : undefined}
                    tabIndex={file.status === "success" ? 0 : undefined}
                    sx={{ ...listItemSx, cursor: file.status === "success" ? "pointer" : "default" }}
                    onClick={() => {
                        if (file.status === "success") handleFileClick(file.key);
                    }}
                    onKeyDown={(e) => {
                        if (file.status === "success" && (e.key === "Enter" || e.key === " ")) handleFileClick(file.key);
                    }}
                    secondaryAction={
                        <IconButton
                            edge="end"
                            aria-label="delete"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleRemoveFile(file.key);
                            }}
                            disabled={file.status === "uploading"}
                        >
                            <Tooltip title={"Remove file"} slotProps={defaultTooltipSlotProps}>
                                <DeleteIcon />
                            </Tooltip>
                        </IconButton>
                    }
                >
                    <ListItemIcon>
                        <FileStatusIcon file={file} />
                    </ListItemIcon>

                    <ListItemText
                        primary={
                            <Box sx={{ display: "flex", flexDirection: "column" }}>
                                <Typography noWrap>{file.key}</Typography>
                                {file.status === "uploading" && file.progress !== undefined && (
                                    <Box sx={{ width: "100%", mt: 1 }}>
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

            <FilePreviewDialog open={previewDialogOpen} fileKey={selectedFileKey} onClose={handleClosePreview} />
        </List>
    );
};
