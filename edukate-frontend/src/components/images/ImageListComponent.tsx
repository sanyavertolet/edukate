import { useState } from 'react';
import { Container, ImageList, ImageListItem } from '@mui/material';
import { ZoomingImageDialog } from "./ZoomingImageDialog";

interface ImageListComponentProps {
    images: string[];
}

export function ImageListComponent({ images }: ImageListComponentProps) {
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const imageListItemSx = {
        justifyContent: 'center',
        alignContent: 'center',
        cursor: 'pointer',
        '&:hover': { opacity: 0.8, transition: 'opacity 0.3s ease-in-out' }
    };

    const handleImageClick = (imageUrl: string) => { setSelectedImage(imageUrl); };
    const handleClose = () => { setSelectedImage(null); };

    return (
        <Container sx={{ display: "flex", justifyContent: "center", alignItems: "center", mt: 2 }}>
            <ImageList gap={3} cols={images.length} variant="woven">
                {images.map((imageUrl, index) => (
                    <ImageListItem key={index} onClick={() => handleImageClick(imageUrl)} sx={imageListItemSx}>
                        <img srcSet={imageUrl} alt={`Image ${index + 1}`} loading="lazy" style={{ maxWidth: '25rem' }}/>
                    </ImageListItem>
                ))}
            </ImageList>

            <ZoomingImageDialog selectedImage={selectedImage} handleClose={handleClose}/>
        </Container>
    );
}
