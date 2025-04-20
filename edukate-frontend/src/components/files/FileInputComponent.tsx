import { FC, useState, ChangeEvent, useRef, useEffect } from "react";
import { IconButton, List, ListItem, ListItemIcon, ListItemText, Tooltip, Box, Typography, Input, Dialog, DialogTitle, DialogContent, DialogActions, Button } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { defaultTooltipSlotProps, formatFileSize } from "../../utils/utils";
import { FileDragAndDropComponent } from "./FileDragAndDropComponent";
import { FileStatusIcon } from "./FileStatusIcon";
import { usePostTempFileMutation, useDeleteTempFileMutation, useGetTempFiles, useGetTempFile } from "../../http/files";
import { FileMetadata } from "../../types/FileMetadata";
import { ErrorSnackbar } from "../snackbars/ErrorSnackbar";

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
    const [fileMetadataList, setFileMetadataList] = useState<FileMetadata[]>([]);
    const [selectedFileKey, setSelectedFileKey] = useState<string | null>(null);
    const [previewDialogOpen, setPreviewDialogOpen] = useState(false);

    const { data: tempFiles, isLoading, error } = useGetTempFiles();

    useEffect(() => {
        if (tempFiles && tempFiles.length > 0 && !isLoading && !error) {
            setFileMetadataList(prev => {
                const newFiles = tempFiles.filter(tempFile =>
                    !prev.some(existingFile => existingFile.key === tempFile.key)
                );

                const filesWithStatus = newFiles.map(file => ({
                    ...file,
                    status: 'success' as const,
                    progress: 100
                }));

                return [...prev, ...filesWithStatus];
            });

            tempFiles.forEach(file => {
                if (file.key) {
                    onTempFileUploaded(file.key);
                }
            });
        }
        // eslint-disable-next-line
    }, [tempFiles, isLoading, error]);

    const { data: previewFile } = useGetTempFile(selectedFileKey || '');

    const handleFileClick = (key: string) => {
        setSelectedFileKey(key);
        setPreviewDialogOpen(true);
    };

    const handleClosePreview = () => {
        setPreviewDialogOpen(false);
        setSelectedFileKey(null);
    };

    const [errorText, setErrorText] = useState<string>()
    const postTempFileMutation = usePostTempFileMutation();
    const handleAddFiles = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files) return;

        const newFiles: FileMetadata[] = Array.from(event.target.files).map(file => ({
            key: file.name,
            authorName: '',
            lastModified: new Date().toISOString(),
            size: file.size,
            status: 'pending',
            progress: 0,
            _file: file
        }));

        const currentFilesLength = fileMetadataList.length + event.target.files.length
        if (currentFilesLength > maxFiles) {
            setErrorText(`You can upload no more than ${maxFiles} files. You got ${currentFilesLength}.`);
            return;
        }
        const oldSize = fileMetadataList.reduce((sum, metadata) => sum + metadata.size, 0);
        const newSize = newFiles.reduce((sum, metadata) => sum + metadata.size, 0)
        if (oldSize + newSize > maxSize) {
            setErrorText(
                `You can upload no more than ${formatFileSize(maxSize)}, you have ${formatFileSize(oldSize + newSize)}.`
            );
            return;
        }

        setFileMetadataList(prev => [...prev, ...newFiles]);

        newFiles.forEach(async (metadata) => {
            if (!metadata._file) return;
            const file = metadata._file;
            try {
                setFileMetadataList(prev => prev.map(m =>
                    m.key === metadata.key ? { ...m, status: 'uploading' } : m
                ));

                postTempFileMutation.mutate(
                    { file, onProgress: (progress) => {
                        setFileMetadataList(prev => prev.map(m =>
                            m.key === metadata.key ? { ...m, progress, status: 'uploading' } : m
                        ));
                    }},
                    {
                        onSuccess: (key) => {
                            setFileMetadataList(prev => prev.map(m =>
                                m.key === metadata.key
                                    ? { ...m, key, status: 'success', progress: 100 }
                                    : m
                            ));

                            onTempFileUploaded(key);
                        },
                        onError: (error) => {
                            setFileMetadataList(prev => prev.map(m =>
                                m.key === metadata.key
                                    ? { ...m, status: 'error', error: error as Error, progress: 0 }
                                    : m
                            ));

                            console.error(`Failed to upload file ${file.name}:`, error);
                        }
                    }
                );
            } catch (error) {
                setFileMetadataList(prev => prev.map(m =>
                    m.key === metadata.key
                        ? { ...m, status: 'error', error: error as Error, progress: 0 }
                        : m
                ));

                console.error(`Failed to upload file ${file.name}:`, error);
            }
        });

        if (event.target) {
            event.target.value = "";
        }
    };

    const deleteTempFileMutation = useDeleteTempFileMutation();
    const handleRemoveFile = (key: string) => {
        const file = fileMetadataList.find(file => file.key === key);
        if (file && file.status === 'success') {
            deleteTempFileMutation.mutate(key, {
                onSuccess: (serverKey) => {
                    console.log(serverKey);
                    onTempFileDeleted(serverKey);
                    setFileMetadataList(prev => prev.filter(file => file.key !== serverKey));
                }
            });
        } else {
            setFileMetadataList(prev => prev.filter(file => file.key !== key));
        }
    };

    const fileInputRef = useRef<HTMLInputElement>(null);
    const onUploadButtonClick = () => { fileInputRef.current?.click(); };
    const listItemSx = {
        backgroundColor: 'background.paper', mb: 1, borderRadius: 1, border: '1px solid', borderColor: 'divider',
    };
    return (
        <List>
            <Input type="file" style={{ display: "none" }} onChange={handleAddFiles} inputRef={fileInputRef}
                   inputProps={{ multiple: true, accept: accept }}/>

            <FileDragAndDropComponent files={fileMetadataList} onUploadButtonClick={onUploadButtonClick}
                                      maxFiles={maxFiles} maxSize={maxSize}/>

            {fileMetadataList.map((file) => {
                return (
                    <ListItem
                        key={file.key}
                        sx={{
                            ...listItemSx,
                            cursor: file.status === 'success' ? 'pointer' : 'default'
                        }}
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
                );
            })}

            <Dialog open={previewDialogOpen} onClose={handleClosePreview} maxWidth="md" fullWidth>
                <DialogTitle>File Preview</DialogTitle>
                <DialogContent>
                    {previewFile && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                            <img 
                                src={URL.createObjectURL(previewFile)} 
                                alt="File Preview" 
                                style={{ maxWidth: '100%', maxHeight: '70vh' }}
                                onLoad={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    return () => URL.revokeObjectURL(target.src);
                                }}
                            />
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClosePreview}>Close</Button>
                </DialogActions>
            </Dialog>
            <ErrorSnackbar errorText={errorText}/>
        </List>
    );
};
