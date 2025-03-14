import { Backdrop, Card, CardContent, Container, ImageList, ImageListItem, Typography } from "@mui/material";
import { Problem } from "../../types/Problem";
import { useState } from "react";
import { SubtasksComponent } from "./SubtasksComponent";

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
                { selectedImage && (
                        <img srcSet={ selectedImage } alt={ `Problem Image ${problem.id}` } loading="eager"
                             style={{ maxWidth: "50rem" }}
                        />
                )}
            </Backdrop>
            <CardContent>
                <Typography color="secondary" variant="h6">
                    Terms
                </Typography>

                { problem.text && (<Typography variant="body1"> { problem.text } </Typography>)}

                <SubtasksComponent subtasks={ problem.subtasks }/>
                { problem.images?.length > 0 && (
                    <Container sx={{ display: "flex", justifyContent: "center", alignItems: "center", mt: 2 }}>
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
