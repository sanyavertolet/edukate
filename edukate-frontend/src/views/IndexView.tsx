import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function IndexView() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        position: 'relative',
        minHeight: { xs: '100vh', md: '110vh' },
        overflowX: 'hidden',
        px: { xs: 2, sm: 4, md: 8 },
        py: { xs: 8, md: 12 },
      }}
    >

      {/* Left text */}
      <Box sx={{ position: 'relative', zIndex: 1, maxWidth: { xs: '100%', md: '60%' } }}>
        <Typography
          component="h1"
          sx={{
            fontFamily: '"Inter", Arial, sans-serif',
            fontWeight: 900,
            lineHeight: 1.15,
            letterSpacing: '-0.02em',
            textAlign: 'left',
            fontSize: { xs: '42px', sm: '60px', md: '86px', lg: '100px' },
          }}
        >
          <Box component="span" sx={{ display: 'block', mb: 1.5 }}>
            Welcome
          </Box>
          <Box component="span" sx={{ display: 'block' }}>
            to{' '}
            <Box
              component="span"
              sx={{
                color: 'primary.main',
                fontWeight: 900,
                fontSize: { xs: '1.05em', md: '1.15em', lg: '1.2em' },
              }}
            >
              Edukate
            </Box>
          </Box>
        </Typography>

        <Typography
          sx={{
            mt: { xs: 2, md: 3 },
            maxWidth: 640,
            opacity: 1,
            fontSize: { xs: '16px', md: '18px' },
            fontFamily: '"Inter", Arial, sans-serif',
            textAlign: 'left',
            lineHeight: 1.6,
            fontWeight: 500,
            color: '#222',
          }}
        >
          Edukate is a platform designed to bring engaging and interactive educational
          experiences straight to your fingertips. Explore a variety of problems and
          challenges, track your progress, and grow your knowledge!
        </Typography>
      </Box>

      {/* Button */}
      <Box
        sx={{
          position: 'relative',
          zIndex: 1,
          display: 'flex',
          justifyContent: 'center',
          mt: { xs: 8, md: 10 },
        }}
      >
        <Button
          onClick={() => navigate('/problems')}
          variant="contained"
          size="large"
          sx={{
            px: 4,
            py: 1.5,
            borderRadius: 999,
            textTransform: 'uppercase',
            fontWeight: 700,
            letterSpacing: 0.6,
            bgcolor: 'primary.main',
            '&:hover': { bgcolor: 'primary.dark' },
            boxShadow: 3,
          }}
        >
          Explore Problems
        </Button>
      </Box>
    </Box>
  );
}
