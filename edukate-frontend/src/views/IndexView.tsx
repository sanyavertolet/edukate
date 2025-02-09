import { Box, Button, Container, Typography } from '@mui/material';
import {useNavigate} from "react-router-dom";

export default function IndexView() {

    const navigate = useNavigate()
    const handleClick = () => { navigate('/problems') }

    return (
        <Container>
            <Box sx={{
                textAlign: 'center',
                marginTop: '2rem',
            }}>
                <Typography variant="h4" gutterBottom>
                    Welcome to Edukate
                </Typography>
                <Typography variant="body1" sx={{ color: 'text.secondary', mb: 4 }}>
                    Edukate is a platform designed to bring engaging and interactive educational
                    experiences straight to your fingertips. Explore a variety of problems and challenges,
                    track your progress, and grow your knowledge!
                </Typography>
                <Button variant="contained" color="primary" onClick={handleClick}>
                    Explore Problems
                </Button>
            </Box>
        </Container>
    );
}
