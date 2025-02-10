import { Box, Card, CardContent, Divider, ImageList, ImageListItem, Typography } from "@mui/material";
import { Problem } from "../types/Problem";

interface ProblemCardComponentProps {
    problem: Problem;
}

export default function ProblemCardComponent({problem}: ProblemCardComponentProps) {
    return (
        <Card>
            <CardContent>
                <Typography variant="h6">
                    Terms
                </Typography>

                <Divider sx={{ my: 2 }} />

                <Typography variant="body1">
                    {problem.text}
                </Typography>

                {problem.images?.length > 0 && (
                    <Box>
                        <Divider sx={{ my: 2 }} />
                        <Typography variant="h6" gutterBottom>
                            Problem Images
                        </Typography>
                        <ImageList cols={2} gap={8}>
                            {problem.images.map((imageUrl, index) => (
                                <ImageListItem key={index}>
                                    <img
                                        src={imageUrl}
                                        alt={`Problem Image ${index}`}
                                        loading="lazy"
                                    />
                                </ImageListItem>
                            ))}
                        </ImageList>
                    </Box>
                )}
            </CardContent>
        </Card>
    );
}
