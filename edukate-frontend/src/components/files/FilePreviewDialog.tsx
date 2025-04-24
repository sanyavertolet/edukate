import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import { useDeviceContext } from "../topbar/DeviceContextProvider";
import { useGetTempFile } from "../../http/files";
import { useEffect, useState } from "react";

interface FilePreviewDialogProps {
    open: boolean;
    fileKey: string | undefined;
    onClose: () => void;
}

export function FilePreviewDialog({ open, fileKey, onClose }: FilePreviewDialogProps) {
    const { isMobile } = useDeviceContext();
    const { data: fileBlob } = useGetTempFile(fileKey);
    const [objectUrl, setObjectUrl] = useState<string>();

    useEffect(() => {
        if (fileBlob) {
            const url = URL.createObjectURL(fileBlob);
            setObjectUrl(url);
            return () => { URL.revokeObjectURL(url); setObjectUrl(undefined); };
        }
    }, [fileBlob]);

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
            <DialogTitle>File Preview</DialogTitle>
            <DialogContent>
                { objectUrl && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                        <img src={objectUrl} alt="File Preview" style={{ maxWidth: '100%', maxHeight: '70vh' }}/>
                    </Box>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} fullWidth>Close</Button>
            </DialogActions>
        </Dialog>
    );
}
