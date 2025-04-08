import { FC } from "react";
import { useNavigate } from "react-router-dom";
import { Box, Divider, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText } from "@mui/material";
import { mobileNavigationElements } from "./NavigationElement";
import { useDeviceContext } from "./DeviceContextProvider";

interface MobileDrawerComponentProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

export const MobileDrawerComponent: FC<MobileDrawerComponentProps> = ({isOpen, setIsOpen}) => {
    const navigate = useNavigate();
    const { pageSpecificNavigation } = useDeviceContext();

    return (
        <Drawer open={isOpen} onClose={() => setIsOpen(false)}>
            <Box sx={{ width: 250 }} role="presentation" onClick={() => setIsOpen(false)}>
                <List>
                    { mobileNavigationElements.map((element) => (
                        <ListItem key={element.text} disablePadding>
                            <ListItemButton onClick={() => navigate(element.href)}
                                            selected={window.location.href.endsWith(element.href)}>
                                { element.icon && <ListItemIcon>{element.icon}</ListItemIcon>}
                                <ListItemText primary={element.text} />
                            </ListItemButton>
                        </ListItem>
                    ))}
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
