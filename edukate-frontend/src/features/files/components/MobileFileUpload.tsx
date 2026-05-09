import { Box, Button, CircularProgress, Fab, SwipeableDrawer } from "@mui/material";
import { useState, useRef } from "react";
import { MobileFileInput } from "./MobileFileInput";
import AddIcon from "@mui/icons-material/Add";
import { useTranslation } from "react-i18next";

interface MobileFileUploadProps {
    accept?: string;
    maxSize?: number;
    maxFiles?: number;
    onSubmit?: (fileNames: string[]) => void;
    isSubmitting?: boolean;
}

export function MobileFileUpload({
    accept = "*",
    maxSize = 50 * 1024 * 1024,
    maxFiles = 5,
    onSubmit,
    isSubmitting,
}: MobileFileUploadProps) {
    const { t } = useTranslation();
    const [uploadedFileNames, setUploadedFileNames] = useState<string[]>([]);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);

    const addFileKey = (fileKey: string) => {
        setUploadedFileNames((prevState) => [...prevState, fileKey]);
    };

    const deleteFileKey = (fileKey: string) => {
        setUploadedFileNames((prevState) => prevState.filter((key) => key !== fileKey));
    };

    const toggleDrawer = (open: boolean) => () => {
        setIsDrawerOpen(open);
    };

    const handleSubmit = () => {
        if (onSubmit && uploadedFileNames.length > 0) {
            onSubmit(uploadedFileNames);
        }
    };

    const containerRef = useRef<HTMLDivElement>(null);
    return (
        <Box ref={containerRef} sx={{ position: "relative", height: "100%" }}>
            <Fab
                color="primary"
                aria-label="add"
                onClick={toggleDrawer(true)}
                sx={{ position: "fixed", bottom: 16, right: 16, zIndex: 10000, display: isDrawerOpen ? "none" : "flex" }}
            >
                <AddIcon />
            </Fab>

            <SwipeableDrawer
                container={containerRef.current}
                anchor="bottom"
                open={isDrawerOpen}
                onClose={toggleDrawer(false)}
                onOpen={toggleDrawer(true)}
                swipeAreaWidth={25}
                disableSwipeToOpen={false}
                ModalProps={{ keepMounted: true }}
                sx={{ "& .MuiDrawer-paper": { height: "auto", overflow: "visible" } }}
            >
                <Box sx={{ px: 1, pt: 1, height: "100%", overflowX: "hidden", overflowY: "auto" }}>
                    <MobileFileInput
                        onTempFileUploaded={addFileKey}
                        onTempFileDeleted={deleteFileKey}
                        accept={accept}
                        maxFiles={maxFiles}
                        maxSize={maxSize}
                    />
                    {onSubmit && uploadedFileNames.length > 0 && (
                        <Button
                            color="secondary"
                            onClick={handleSubmit}
                            disabled={isSubmitting}
                            startIcon={isSubmitting ? <CircularProgress size={20} /> : undefined}
                            sx={{ my: 1, width: "100%" }}
                        >
                            {t("submit_button")}
                        </Button>
                    )}
                </Box>
            </SwipeableDrawer>
        </Box>
    );
}
