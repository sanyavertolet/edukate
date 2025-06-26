import { Box, Button, Paper } from "@mui/material";
import { useState } from "react";
import { FileInputComponent } from "./FileInputComponent";

interface FileUploadComponentProps {
    accept?: string;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (fileKeys: string[]) => void;
}

export function FileUploadComponent(
    { accept = "*", maxSize = 50 * 1024 * 1024, maxFiles = 5, onSubmit }: FileUploadComponentProps
) {
    const [uploadedFileKeys, setUploadedFileKeys] = useState<string[]>([]);
    const addFileKey = (fileKey: string) => {
        setUploadedFileKeys(prevState => [...prevState, fileKey]);
    };
    const deleteFileKey = (fileKey: string) => {
        setUploadedFileKeys(prevState => prevState.filter(key => key !== fileKey));
    };
    return (
        <Paper elevation={2} sx={{p: 1, borderRadius: 2, width: "60%"}}>
            <Box gap={2}>
                <FileInputComponent onTempFileUploaded={addFileKey} onTempFileDeleted={deleteFileKey} accept={accept}
                                    maxFiles={maxFiles} maxSize={maxSize}/>
                { onSubmit && uploadedFileKeys.length > 0 && (
                    <Button variant={"text"} color={"secondary"} sx={{ mb: 1 }}
                        onClick={() => onSubmit(uploadedFileKeys)}>Submit</Button>
                )}
            </Box>
        </Paper>
    );
}
