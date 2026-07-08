import { FC, useCallback, useEffect, useState } from "react";
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Slider, Typography } from "@mui/material";
import Cropper, { Area } from "react-easy-crop";
import { useTranslation } from "react-i18next";

const OUTPUT_SIZE = 512;
const JPEG_QUALITY = 0.9;

async function cropToBlob(imageSrc: string, area: Area): Promise<Blob> {
    const image = await loadImage(imageSrc);
    const canvas = document.createElement("canvas");
    canvas.width = OUTPUT_SIZE;
    canvas.height = OUTPUT_SIZE;
    const ctx = canvas.getContext("2d");
    if (!ctx) throw new Error("Canvas 2D context unavailable");
    ctx.drawImage(image, area.x, area.y, area.width, area.height, 0, 0, OUTPUT_SIZE, OUTPUT_SIZE);
    return new Promise<Blob>((resolve, reject) => {
        canvas.toBlob(
            (blob) => {
                if (blob) resolve(blob);
                else reject(new Error("toBlob produced no data"));
            },
            "image/jpeg",
            JPEG_QUALITY,
        );
    });
}

function loadImage(src: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => {
            resolve(img);
        };
        img.onerror = () => {
            reject(new Error("Image load failed"));
        };
        img.src = src;
    });
}

interface AvatarCropDialogProps {
    /** The picked image as a data URL; the dialog is open whenever this is non-null. */
    imageSrc: string | null;
    onClose: () => void;
    onConfirm: (blob: Blob) => Promise<void> | void;
    submitting?: boolean;
}

export const AvatarCropDialog: FC<AvatarCropDialogProps> = ({ imageSrc, onClose, onConfirm, submitting }) => {
    const { t } = useTranslation("settings");
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedArea, setCroppedArea] = useState<Area | null>(null);

    // A fresh image resets the framing so the previous crop/zoom never carries over.
    useEffect(() => {
        setCrop({ x: 0, y: 0 });
        setZoom(1);
        setCroppedArea(null);
    }, [imageSrc]);

    const handleCropComplete = useCallback((_: Area, areaPx: Area) => {
        setCroppedArea(areaPx);
    }, []);

    const handleConfirm = async () => {
        if (!imageSrc || !croppedArea) return;
        const blob = await cropToBlob(imageSrc, croppedArea);
        await onConfirm(blob);
    };

    return (
        <Dialog open={imageSrc !== null} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>{t("avatar_crop_title")}</DialogTitle>
            <DialogContent>
                {imageSrc && (
                    <>
                        <Box sx={{ position: "relative", height: 320, bgcolor: "common.black", borderRadius: 1 }}>
                            <Cropper
                                image={imageSrc}
                                crop={crop}
                                zoom={zoom}
                                aspect={1}
                                cropShape="round"
                                showGrid={false}
                                onCropChange={setCrop}
                                onZoomChange={setZoom}
                                onCropComplete={handleCropComplete}
                            />
                        </Box>
                        <Box sx={{ display: "flex", alignItems: "center", gap: 2, mt: 2 }}>
                            <Typography variant="caption" sx={{ minWidth: 40 }}>
                                {t("avatar_zoom")}
                            </Typography>
                            <Slider
                                value={zoom}
                                min={1}
                                max={4}
                                step={0.05}
                                onChange={(_, v) => {
                                    setZoom(typeof v === "number" ? v : v[0]);
                                }}
                                aria-label="zoom"
                            />
                        </Box>
                    </>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>{t("cancel")}</Button>
                <Button
                    variant="contained"
                    onClick={() => void handleConfirm()}
                    disabled={!imageSrc || !croppedArea || submitting}
                >
                    {t("avatar_save")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
