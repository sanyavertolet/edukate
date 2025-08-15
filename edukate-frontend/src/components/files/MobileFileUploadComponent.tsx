import { Box, Fab, Paper, SwipeableDrawer, Button } from "@mui/material";
import { useState, useRef } from "react";
import { MobileFileInputComponent } from "./MobileFileInputComponent";
import AddIcon from "@mui/icons-material/Add";

interface MobileFileUploadComponentProps {
    accept?: string;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (fileNames: string[]) => void;
}

export function MobileFileUploadComponent(
    { accept = "*", maxSize = 50 * 1024 * 1024, maxFiles = 5, onSubmit }: MobileFileUploadComponentProps
) {
    const [uploadedFileNames, setUploadedFileNames] = useState<string[]>([]);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);

    const addFileKey = (fileKey: string) => {
        setUploadedFileNames(prevState => [...prevState, fileKey]);
    };

    const deleteFileKey = (fileKey: string) => {
        setUploadedFileNames(prevState => prevState.filter(key => key !== fileKey));
    };

    const toggleDrawer = (open: boolean) => () => { setIsDrawerOpen(open); };

    const handleSubmit = () => {
        if (onSubmit && uploadedFileNames.length > 0) {
            onSubmit(uploadedFileNames);
        }
    };

    const containerRef = useRef<HTMLDivElement>(null);
    return (
        <Box ref={containerRef} sx={{ position: 'relative', height: '100%' }}>
            <Fab color="primary" aria-label="add" onClick={toggleDrawer(true)}
                 sx={{ position: 'fixed', bottom: 16, right: 16, zIndex: 10000, display: isDrawerOpen ? "none" : "flex" }}><AddIcon/></Fab>

            <SwipeableDrawer
                container={containerRef.current} anchor="bottom"
                open={isDrawerOpen} onClose={toggleDrawer(false)} onOpen={toggleDrawer(true)}
                swipeAreaWidth={25} disableSwipeToOpen={false} ModalProps={{ keepMounted: true }}
                sx={{ '& .MuiDrawer-paper': { height: 'auto', overflow: 'visible' } }}
            >
                <Box sx={{ px: 2, height: '100%', overflow: 'auto' }}>
                    <Paper elevation={0} sx={{ p: 1, borderRadius: 2, width: "100%" }}>
                        <Box gap={2} pt={1}>
                            <MobileFileInputComponent 
                                onTempFileUploaded={addFileKey} onTempFileDeleted={deleteFileKey}
                                accept={accept} maxFiles={maxFiles} maxSize={maxSize}/>
                            {onSubmit && uploadedFileNames.length > 0 && (
                                <Button color="secondary" onClick={handleSubmit} sx={{ my: 1, width: '100%' }}>
                                    Submit
                                </Button>
                            )}
                        </Box>
                    </Paper>
                </Box>
            </SwipeableDrawer>
        </Box>
    );
}
