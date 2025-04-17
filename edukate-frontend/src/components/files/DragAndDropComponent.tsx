import { Box, Button, Paper } from "@mui/material";
import { useState } from "react";
import { SelectedFilesComponent } from "./SelectedFilesComponent";

interface DragAndDropComponentProps {
    accept?: string;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (fileKeys: string[]) => void;
}

export function DragAndDropComponent(
    { accept = "*", maxSize = 50 * 1024 * 1024, maxFiles = 5, onSubmit }: DragAndDropComponentProps
) {
    const [uploadedFileKeys, setUploadedFileKeys] = useState<string[]>([]);
    const addFileKey = (fileKey: string) => {
        setUploadedFileKeys(prevState => [...prevState, fileKey]);
    };
    const deleteFileKey = (fileKey: string) => {
        setUploadedFileKeys(prevState => prevState.filter(key => key !== fileKey));
    };
    return (
        <Paper elevation={2} sx={{p: 1, borderRadius: 2, width: "50%"}}>
            <Box gap={2}>
                <SelectedFilesComponent onTempFileUploaded={addFileKey} onTempFileDeleted={deleteFileKey} accept={accept}
                                        maxFiles={maxFiles} maxSize={maxSize}/>
                { onSubmit && uploadedFileKeys.length > 0 && (
                    <Button variant={"outlined"} color={"secondary"} sx={{ mt: 2 }}
                        onClick={() => onSubmit(uploadedFileKeys)}>Submit</Button>
                )}
            </Box>
        </Paper>
    );
}
