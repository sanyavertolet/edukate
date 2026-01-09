import { Box, Button, Paper } from "@mui/material";
import { useState } from "react";
import { FileInputComponent } from "./FileInputComponent";

interface FileUploadComponentProps {
    accept?: string;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (fileNames: string[]) => void;
}

export function FileUploadComponent(
    { accept = "*", maxSize = 50 * 1024 * 1024, maxFiles = 5, onSubmit }: FileUploadComponentProps
) {
    const [uploadedFileNames, setUploadedFileNames] = useState<string[]>([]);
    const addFileKey = (fileKey: string) => {
        setUploadedFileNames(prevState => [...prevState, fileKey]);
    };
    const deleteFileKey = (fileKey: string) => {
        setUploadedFileNames(prevState => prevState.filter(key => key !== fileKey));
    };
    return (
        <Paper elevation={2} sx={{p: 1, borderRadius: 2, width: "80%"}}>
            <Box gap={2}>
                <FileInputComponent onTempFileUploaded={addFileKey} onTempFileDeleted={deleteFileKey} accept={accept}
                                    maxFiles={maxFiles} maxSize={maxSize}/>
                { onSubmit && uploadedFileNames.length > 0 && (
                    <Button variant={"text"} color={"secondary"} sx={{ mb: 1 }}
                        onClick={() => onSubmit(uploadedFileNames)}>Submit</Button>
                )}
            </Box>
        </Paper>
    );
}
