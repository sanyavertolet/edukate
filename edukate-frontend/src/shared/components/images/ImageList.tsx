import { useState } from "react";
import { Container, ImageList, ImageListItem } from "@mui/material";
import { ImageLightbox } from "./ImageLightbox";

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
    const [selectedIndex, setSelectedIndex] = useState<number>(-1);

    return (
        <Container sx={containerSx}>
            <ImageList gap={3} cols={images.length} variant="woven">
                {images.map((imageUrl, index) => (
                    <ImageListItem
                        key={imageUrl}
                        role="button"
                        tabIndex={0}
                        sx={imageListItemSx}
                        onClick={() => {
                            setSelectedIndex(index);
                        }}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" || e.key === " ") setSelectedIndex(index);
                        }}
                    >
                        <img
                            srcSet={imageUrl}
                            alt={`Image ${String(index + 1)}`}
                            loading="lazy"
                            style={{ maxWidth: "25rem" }}
                        />
                    </ImageListItem>
                ))}
            </ImageList>

            <ImageLightbox
                images={images}
                index={selectedIndex}
                open={selectedIndex >= 0}
                onClose={() => {
                    setSelectedIndex(-1);
                }}
            />
        </Container>
    );
}
