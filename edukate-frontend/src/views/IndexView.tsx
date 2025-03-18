import { Box, Button, Card, CardContent, Typography } from '@mui/material';
import { useNavigate } from "react-router-dom";

export default function IndexView() {
    const navigate = useNavigate();
    const handleClick = () => { navigate('/problems') };

    return (
        <Box>
            <Typography color="primary" variant="h5">
                Welcome to Edukate
            </Typography>

            <Card sx={{
                textAlign: "center",
                marginTop: "2rem",
                padding: "1rem",
            }}>
                <CardContent>
                    <Typography variant="body1" marginBottom={ 4 }>
                        Edukate is a platform designed to bring engaging and interactive educational
                        experiences straight to your fingertips. Explore a variety of problems and challenges,
                        track your progress, and grow your knowledge!
                    </Typography>
                    <Button variant="contained" color="primary" onClick={ handleClick }>
                        Explore Problems
                    </Button>
                </CardContent>
            </Card>
        </Box>
    );
};
