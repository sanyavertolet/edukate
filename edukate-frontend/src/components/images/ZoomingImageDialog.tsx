import { Box, Dialog, DialogContent, IconButton, SxProps, Theme } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";

interface ZoomingImageDialogProps {
    selectedImage: string | null;
    handleClose: () => void;
}

export function ZoomingImageDialog({selectedImage, handleClose}: ZoomingImageDialogProps) {
    const iconButtonSx: SxProps<Theme> = {
        position: 'absolute', right: 8, top: 8, zIndex: 1, color: (theme) => theme.palette.grey[500]
    };
    const imgSx: SxProps = { objectFit: "contain", width: "100%", height: "100vh", maxHeight: "100%" };

    const dialogContextSx: SxProps = {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        overflow: "hidden",
        height: "100vh",
        maxHeight: "100vh",
    };

    const paperSx: SxProps = {
        margin: 0,
        maxWidth: "80vw",
        maxHeight: "80vh"
    }

    return (
        <Dialog
            open={!!selectedImage}
            onClose={handleClose}
            maxWidth={false}
            fullWidth
            slotProps={{
                paper: {
                    sx: paperSx
                }
            }}
        >
            <IconButton aria-label="close" onClick={handleClose} sx={iconButtonSx}>
                <CloseIcon/>
            </IconButton>

            <DialogContent sx={dialogContextSx}>
                {selectedImage && (<Box component="img" src={ selectedImage } alt="Full Screen Image" sx={imgSx}/>)}
            </DialogContent>
        </Dialog>
    );
}
