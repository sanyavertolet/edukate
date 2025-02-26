import {
    Backdrop,
    Box,
    Card,
    CardContent,
    Container,
    Divider,
    ImageList,
    ImageListItem,
    Typography
} from "@mui/material";
import { Problem } from "../../types/Problem";
import { useState } from "react";

interface ProblemCardComponentProps {
    problem: Problem;
}

export default function ProblemCardComponent({problem}: ProblemCardComponentProps) {
    const [selectedImage, setSelectedImage] = useState<string>();

    return (
        <Card>
            <Backdrop
                open={ selectedImage != undefined }
                onClick={() => setSelectedImage(undefined)}
                sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            >
                { selectedImage?
                    <img
                    srcSet={selectedImage}
                    alt={`Problem Image ${problem.id}`}
                    loading="eager"
                    /> : undefined
                }
            </Backdrop>
            <CardContent>
                <Typography variant="h6">
                    Terms
                </Typography>

                <Typography variant="body1">
                    {problem.text}
                </Typography>

                <Divider sx={{ my: 2 }} />

                {problem.images?.length > 0 && (
                    <Container sx={{ justifyItems: "center" }}>
                        <Box width={1/3}>
                            <ImageList cols={problem.images?.length} gap={8}>
                                {problem.images.map((imageUrl, index) => (
                                    <ImageListItem key={index} onClick={() => setSelectedImage(imageUrl)}>
                                        <img
                                            srcSet={imageUrl}
                                            alt={`Problem Image ${index}`}
                                            loading="lazy"
                                        />
                                    </ImageListItem>
                                ))}
                            </ImageList>
                        </Box>
                    </Container>
                )}
            </CardContent>
        </Card>
    );
}
