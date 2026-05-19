import { Box, Button, CircularProgress, Fab } from "@mui/material";
import { useState } from "react";
import { createPortal } from "react-dom";
import { SwipeableDrawer } from "@/shared/components/Styled";
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

    return (
        <Box>
            {createPortal(
                <Fab
                    color="primary"
                    aria-label="add"
                    onClick={toggleDrawer(true)}
                    sx={{ position: "fixed", bottom: 16, right: 16, display: isDrawerOpen ? "none" : "flex" }}
                >
                    <AddIcon />
                </Fab>,
                document.body,
            )}

            <SwipeableDrawer
                anchor="bottom"
                open={isDrawerOpen}
                onClose={toggleDrawer(false)}
                onOpen={toggleDrawer(true)}
                swipeAreaWidth={25}
                disableSwipeToOpen={false}
                ModalProps={{ keepMounted: true }}
                sx={{ "& .MuiDrawer-paper": { height: "auto", overflow: "visible" } }}
            >
                <Box
                    sx={{
                        px: 1,
                        pt: 1,
                        pb: "max(24px, env(safe-area-inset-bottom))",
                        height: "100%",
                        overflowX: "hidden",
                        overflowY: "auto",
                    }}
                >
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
