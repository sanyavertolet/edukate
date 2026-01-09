import { styled, alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Container from '@mui/material/Container';
import MenuIcon from '@mui/icons-material/Menu';
import { SiteMark } from './SiteMark';
import { useNavigate } from "react-router-dom";
import { MobileDrawerComponent } from "./MobileDrawerComponent";
import { useState } from "react";
import { useAuthContext } from "../auth/AuthContextProvider";
import { NotificationButton } from "../notifications/NotificationButton";
import ThemeToggleButton from "../themes/ThemeToggleButton";
import { UserMenu } from "./UserMenu";

const StyledToolbar = styled(Toolbar)(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    flexShrink: 0,
    borderRadius: `calc(${theme.shape.borderRadius}px + 8px)`,
    backdropFilter: 'blur(24px)',
    border: '1px solid',
    borderColor: theme.palette.divider,
    backgroundColor: alpha(theme.palette.background.default, 0.4),
    boxShadow: theme.shadows[1],
    padding: '8px 12px',
}));

export function ScreenAdaptingTopBar() {
    const [open, setOpen] = useState(false);

    const toggleDrawer = (newOpen: boolean) => () => { setOpen(newOpen); };

    const appBarSx = {
        boxShadow: 0,
        bgcolor: 'transparent',
        backgroundImage: 'none',
        mt: 'calc(var(--template-frame-height, 0px) + 28px)',
    };

    const { isAuthorized } = useAuthContext();
    const navigate = useNavigate();
    return (
        <AppBar position="fixed" enableColorOnDark sx={appBarSx}>
            <Container maxWidth="lg">
                <StyledToolbar variant="regular" disableGutters>
                    <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', px: 0 }}>
                        <Box sx={{ display: { xs: 'flex', md: 'none' }, gap: 1 }}>
                            <IconButton aria-label="Menu button" onClick={toggleDrawer(true)}>
                                <MenuIcon />
                            </IconButton>
                            <MobileDrawerComponent isOpen={open} setIsOpen={setOpen}/>
                        </Box>
                        <SiteMark />
                        <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
                            <TopBarLink text="Problems" onClick={() => navigate('/problems')} />
                            <TopBarLink text="Bundles" onClick={() => navigate('/bundles')} />
                        </Box>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <ThemeToggleButton />

                        {isAuthorized
                            ? (<Box sx={{ display: 'flex', gap: 1, alignItems: 'center'}}>
                                <NotificationButton/>
                                <UserMenu/>
                            </Box>)
                            : (<Box sx={{display: 'flex', gap: 1, alignItems: 'center'}}>
                                <TopBarLink text={"Sign In"} onClick={() => navigate('/sign-in')}/>
                                <TopBarLink text={"Sign Up"} onClick={() => navigate('/sign-up')} variant="contained"/>
                            </Box>)
                        }
                    </Box>
                </StyledToolbar>
            </Container>
        </AppBar>
    );
}

type TopBarLinkProps = {
    text: string;
    onClick: () => void;
    variant?: 'text' | 'contained';
};

function TopBarLink({text, onClick, variant = "text"}: TopBarLinkProps) {
    return (
        <Button variant={variant} color="primary" size="small" onClick={onClick}>
            {text}
        </Button>
    );
}
