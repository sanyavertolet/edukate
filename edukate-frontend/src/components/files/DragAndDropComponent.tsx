import { Box, Button, Paper, CircularProgress } from "@mui/material";
import { ChangeEvent, useEffect, useRef, useState } from "react";
import { SelectedFilesComponent } from "./SelectedFilesComponent";

interface DragAndDropComponentProps {
    onFilesSelected?: (files: File[]) => void;
    accept?: string;
    isLoading?: boolean;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (files: File[]) => void;
    onUploadButtonClick?: () => void;
}

export function DragAndDropComponent(
    {
        onFilesSelected,
        accept = "*",
        maxSize = 50 * 1024 * 1024,
        maxFiles = 5,
        isLoading = false,
        onSubmit,
    }: DragAndDropComponentProps
) {
    const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => { onFilesSelected && onFilesSelected(selectedFiles); }, [selectedFiles]);

    const handleAddFiles = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files) return;
        const updatedFiles = [...selectedFiles, ...Array.from(event.target.files)];
        setSelectedFiles(updatedFiles);
        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    };

    const handleBrowseClick = () => { fileInputRef.current?.click(); };
    const handleRemoveFile = (index: number) => {
        setSelectedFiles(selectedFiles.filter((_, i) => i !== index));
    };

    return (
        <Paper elevation={2} sx={{p: 1, borderRadius: 2, width: "50%"}}>
            <input type={"file"} multiple accept={accept} ref={fileInputRef} style={{ display: "none" }} onChange={handleAddFiles}/>

            <Box gap={2}>
                <SelectedFilesComponent onUploadButtonClick={handleBrowseClick} files={selectedFiles}
                                        isLoading={isLoading} handleRemoveFile={handleRemoveFile}
                                        maxFiles={maxFiles} maxSize={maxSize}/>

                { isLoading && (
                    <Box display="flex" justifyContent="center" my={2}><CircularProgress size={24}/></Box>
                )}

                { onSubmit && (
                    <Button variant={"outlined"} color={"secondary"} onClick={()=>onSubmit(selectedFiles)}>
                        Submit
                    </Button>
                )}
            </Box>
        </Paper>
    );
}
