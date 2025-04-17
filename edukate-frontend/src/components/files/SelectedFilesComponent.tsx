import {FC, useState, ChangeEvent, useRef} from "react";
import { IconButton, List, ListItem, ListItemIcon, ListItemText, Tooltip, LinearProgress, Box, Typography, Input } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { defaultTooltipSlotProps, formatFileSize } from "../../utils/utils";
import { SelectedFilesHeaderComponent } from "./SelectedFilesHeaderComponent";
import { ExtendedFile } from "./ExtendedFile";
import { ExtendedFileStatusIcon } from "./ExtendedFileStatusIcon";
import { usePostTempFileMutation, useDeleteTempFileMutation } from "../../http/files";

type SelectedFilesComponentProps = {
    onTempFileUploaded: (fileKey: string) => void;
    onTempFileDeleted: (fileKey: string) => void;
    maxFiles?: number;
    maxSize?: number;
    accept?: string;
};

export const SelectedFilesComponent: FC<SelectedFilesComponentProps> = (
    { onTempFileUploaded, onTempFileDeleted, maxFiles = 5, maxSize = 50 * 1024 * 1024, accept = "*" }
) => {
    const [files, setFiles] = useState<ExtendedFile[]>([]);

    const postTempFileMutation = usePostTempFileMutation();
    const handleAddFiles = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files) return;

        const newExtendedFiles: ExtendedFile[] = Array.from(event.target.files).map(file => ({
            content: file, status: 'pending', progress: 0,
        }));

        setFiles(prev => [...prev, ...newExtendedFiles]);

        newExtendedFiles.forEach(async (extendedFile) => {
            const file = extendedFile.content;
            try {
                setFiles(prev => prev.map(extendedFile =>
                    extendedFile.content === file ? { ...extendedFile, status: 'uploading' } : extendedFile
                ));

                postTempFileMutation.mutate(
                    { file, onProgress: (progress) => {
                        setFiles(prev => prev.map(extendedFile =>
                            extendedFile.content === file ? { ...extendedFile, progress, status: 'uploading' } : extendedFile
                        ));
                    }},
                    {
                        onSuccess: (key) => {
                            setFiles(prev => prev.map(extendedFile =>
                                extendedFile.content === file
                                    ? { ...extendedFile, status: 'success', key, progress: 100 }
                                    : extendedFile
                            ));

                            onTempFileUploaded(key);
                        },
                        onError: (error) => {
                            setFiles(prev => prev.map(extendedFile =>
                                extendedFile.content === file
                                    ? { ...extendedFile, status: 'error', error: error as Error, progress: 0 }
                                    : extendedFile
                            ));

                            console.error(`Failed to upload file ${file.name}:`, error);
                        }
                    }
                );
            } catch (error) {
                setFiles(prev => prev.map(extendedFile =>
                    extendedFile.content === file
                        ? { ...extendedFile, status: 'error', error: error as Error, progress: 0 }
                        : extendedFile
                ));

                console.error(`Failed to upload file ${file.name}:`, error);
            }
        });

        if (event.target) {
            event.target.value = "";
        }
    };

    const deleteTempFileMutation = useDeleteTempFileMutation();
    const handleRemoveFile = (index: number) => {
        const file = files[index];
        if (file.status === 'success' && file.key) {
            deleteTempFileMutation.mutate(file.key, {
                onSuccess: (key) => {
                    onTempFileDeleted(key)
                    console.log(`client key: ${file.key}`)
                    console.log(`server key: ${key}`)
                    setFiles(prev => prev.filter((file) => file.key !== key));
                }
            });
        } else {
            setFiles(prev => prev.filter((_, i) => i !== index));
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

            <SelectedFilesHeaderComponent files={files} onUploadButtonClick={onUploadButtonClick}
                                          maxFiles={maxFiles} maxSize={maxSize}/>

            {files.length > 0 && files.map((extendedFile, index) => {
                return (
                    <ListItem
                        key={`${index}-${extendedFile.content.name}`}
                        sx={listItemSx}
                        secondaryAction={
                            <IconButton edge="end" aria-label="delete" onClick={() => handleRemoveFile(index)}
                                        disabled={extendedFile.status === 'uploading'}>
                                <Tooltip title={"Remove file"} slotProps={defaultTooltipSlotProps}>
                                    <DeleteIcon/>
                                </Tooltip>
                            </IconButton>
                        }
                    >
                        <ListItemIcon><ExtendedFileStatusIcon extendedFile={extendedFile}/></ListItemIcon>

                        <ListItemText
                            primary={
                                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                                    <Typography noWrap>{extendedFile.content.name}</Typography>
                                    {extendedFile.status === 'uploading' && (
                                        <Box sx={{ width: '100%', mt: 1 }}>
                                            <LinearProgress variant="determinate" value={extendedFile.progress}/>
                                            <Typography variant="caption" color="text.secondary">
                                                {extendedFile.progress}%
                                            </Typography>
                                        </Box>
                                    )}
                                </Box>
                            }
                            secondary={formatFileSize(extendedFile.content.size)}
                            slotProps={{ primary: { noWrap: true } }}
                        />
                    </ListItem>
                );
            })}
        </List>
    );
};
