import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import { useDeviceContext } from "../topbar/DeviceContextProvider";
import { useGetTempFile } from "../../http/files";

interface FilePreviewDialogProps {
    open: boolean;
    fileKey: string | undefined;
    onClose: () => void;
}

export function FilePreviewDialog({ open, fileKey, onClose }: FilePreviewDialogProps) {
    const { isMobile } = useDeviceContext();

    const { data: fileBlob } = useGetTempFile(fileKey);
    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
            <DialogTitle>File Preview</DialogTitle>
            <DialogContent>
                { fileBlob && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                        <img
                            src={ URL.createObjectURL(fileBlob) }
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
                <Button onClick={onClose} fullWidth>Close</Button>
            </DialogActions>
        </Dialog>
    );
}
