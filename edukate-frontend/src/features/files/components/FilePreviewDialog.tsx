import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { useGetTempFile } from "@/features/files/api";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

interface FilePreviewDialogProps {
    open: boolean;
    fileKey: string | undefined;
    onClose: () => void;
}

export function FilePreviewDialog({ open, fileKey, onClose }: FilePreviewDialogProps) {
    const { isMobile } = useDeviceContext();
    const { t } = useTranslation("common");
    const { data: fileBlob } = useGetTempFile(fileKey);
    const [objectUrl, setObjectUrl] = useState<string>();

    useEffect(() => {
        if (fileBlob) {
            const url = URL.createObjectURL(fileBlob);
            setObjectUrl(url);
            return () => {
                URL.revokeObjectURL(url);
                setObjectUrl(undefined);
            };
        }
    }, [fileBlob]);

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
            <DialogTitle>{t("file_preview_title")}</DialogTitle>
            <DialogContent>
                {objectUrl && (
                    <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
                        <img src={objectUrl} alt={t("file_preview_title")} style={{ maxWidth: "100%", maxHeight: "70vh" }} />
                    </Box>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} fullWidth>
                    {t("close_button")}
                </Button>
            </DialogActions>
        </Dialog>
    );
}
