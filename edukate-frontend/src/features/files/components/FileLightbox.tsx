import { FC, useEffect, useState } from "react";
import { useGetTempFile } from "@/features/files/api";
import { ImageLightbox } from "@/shared/components/images/ImageLightbox";

interface FileLightboxProps {
    open: boolean;
    fileKey: string | undefined;
    onClose: () => void;
}

export const FileLightbox: FC<FileLightboxProps> = ({ open, fileKey, onClose }) => {
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

    if (!objectUrl) return null;

    return <ImageLightbox images={[objectUrl]} index={0} open={open} onClose={onClose} />;
};
