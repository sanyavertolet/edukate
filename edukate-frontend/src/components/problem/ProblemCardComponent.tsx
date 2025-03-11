import { Backdrop, Card, CardContent, Container, Divider, ImageList, ImageListItem, Typography } from "@mui/material";
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
                transitionDuration={ 400 }
                open={ selectedImage != undefined }
                onClick={ () => setSelectedImage(undefined) }
                sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            >
                { selectedImage?
                    <img srcSet={ selectedImage } alt={ `Problem Image ${problem.id}` } loading="eager"
                         style={{ maxWidth: "50rem" }}
                    /> : undefined
                }
            </Backdrop>
            <CardContent>
                <Typography color="secondary" variant="h6">
                    Terms
                </Typography>

                <Typography variant="body1">
                    { problem.text }
                </Typography>

                <Divider sx={{ my: 2 }} />

                { problem.images?.length > 0 && (
                    <Container sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                        <ImageList cols={ problem.images?.length } gap={ 8 }>
                            { problem.images.map((imageUrl, index) => (
                                <ImageListItem key={index} onClick={ () => setSelectedImage(imageUrl) } sx={{ justifyItems: "center" }}>
                                    <img style={{ maxWidth: "25rem" }}
                                         srcSet={ imageUrl }
                                         alt={`Problem Image ${index}`}
                                         loading="lazy"
                                    />
                                </ImageListItem>
                            ))}
                        </ImageList>
                    </Container>
                )}
            </CardContent>
        </Card>
    );
}
