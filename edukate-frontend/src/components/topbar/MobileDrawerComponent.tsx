import { FC } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Box, Divider, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText } from "@mui/material";
import { mobileNavigationElements } from "./NavigationElement";
import { useDeviceContext } from "./DeviceContextProvider";

interface MobileDrawerComponentProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

export const MobileDrawerComponent: FC<MobileDrawerComponentProps> = ({isOpen, setIsOpen}) => {
    const navigate = useNavigate();
    const location = useLocation();
    const { pageSpecificNavigation } = useDeviceContext();

    return (
        <Drawer open={isOpen} onClose={() => setIsOpen(false)}>
            <Box sx={{ width: 250 }} role="presentation" onClick={() => setIsOpen(false)}>
                <List>
                    { mobileNavigationElements.map((element) => {
                        const isActive = location.pathname === element.href || 
                            (element.href === "/problems" && location.pathname.startsWith("/problems")) ||
                            (element.href === "/bundles" && location.pathname.startsWith("/bundles"));
                        
                        return (
                            <ListItem key={element.text} disablePadding>
                                <ListItemButton onClick={() => navigate(element.href)}
                                                selected={isActive}>
                                    { element.icon && <ListItemIcon>{element.icon}</ListItemIcon>}
                                    <ListItemText 
                                        primary={element.text} 
                                        sx={{
                                            color: isActive ? 'primary.main' : 'text.primary',
                                            fontWeight: isActive ? 600 : 500,
                                            fontSize: '1.1rem',
                                            letterSpacing: '0.5px',
                                            textTransform: 'uppercase'
                                        }}
                                    />
                                </ListItemButton>
                            </ListItem>
                        );
                    })}
                </List>

                { pageSpecificNavigation.length != 0 && <Divider /> }

                { pageSpecificNavigation.length != 0 && (
                    <List>
                        { pageSpecificNavigation.map(element => (
                            <ListItem key={element.text} disablePadding>
                                <ListItemButton onClick={() => element.onClick()} selected={element.isSelected}>
                                    { element.icon && <ListItemIcon>{element.icon}</ListItemIcon>}
                                    <ListItemText primary={element.text} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                )}
            </Box>
        </Drawer>
    );
}
