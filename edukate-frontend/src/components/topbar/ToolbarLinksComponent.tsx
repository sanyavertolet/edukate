import { useNavigate, useLocation } from "react-router-dom";
import { Box, Button, Typography } from "@mui/material";
import { desktopNavigationElements } from "./NavigationElement";

export const ToolbarLinksComponent = () => {
    const navigate = useNavigate();
    const location = useLocation();
    
    return (
        <Box sx={{ display: { sm: 'none', md: 'flex' }, paddingLeft: { md: "1rem" } }}>
            { desktopNavigationElements.map(({ text, href }) => {
                const isActive = location.pathname === href || 
                    (href === "/problems" && location.pathname.startsWith("/problems")) ||
                    (href === "/bundles" && location.pathname.startsWith("/bundles"));
                
                return (
                    <Button key={ text } onClick={ () => navigate(href) }>
                        <Typography 
                            variant={"body2"} 
                            sx={{ 
                                color: isActive ? 'primary.main' : 'text.primary',
                                fontWeight: isActive ? 600 : 400
                            }}
                        >
                            { text }
                        </Typography>
                    </Button>
                );
            })}
        </Box>
    );
}
