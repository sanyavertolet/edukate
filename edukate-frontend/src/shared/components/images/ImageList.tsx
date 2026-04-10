import { useState } from "react";
import { Container, ImageList, ImageListItem } from "@mui/material";
import { ZoomingImageDialog } from "./ZoomingImageDialog";

const imageListItemSx = {
    justifyContent: "center",
    alignContent: "center",
    cursor: "pointer",
    "&:hover": { opacity: 0.8, transition: "opacity 0.3s ease-in-out" },
} as const;

const containerSx = { display: "flex", justifyContent: "center", alignItems: "center", mt: 2 } as const;

interface ImageListComponentProps {
    images: string[];
}

export function ImageListComponent({ images }: ImageListComponentProps) {
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const handleImageClick = (imageUrl: string) => {
        setSelectedImage(imageUrl);
    };
    const handleClose = () => {
        setSelectedImage(null);
    };

    return (
        <Container sx={containerSx}>
            <ImageList gap={3} cols={images.length} variant="woven">
                {images.map((imageUrl, index) => (
                    <ImageListItem
                        key={imageUrl}
                        role="button"
                        tabIndex={0}
                        sx={imageListItemSx}
                        onClick={() => { handleImageClick(imageUrl); }}
                        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") handleImageClick(imageUrl); }}
                    >
                        <img srcSet={imageUrl} alt={`Image ${String(index + 1)}`} loading="lazy" style={{ maxWidth: "25rem" }} />
                    </ImageListItem>
                ))}
            </ImageList>

            <ZoomingImageDialog selectedImage={selectedImage} handleClose={handleClose} />
        </Container>
    );
}
