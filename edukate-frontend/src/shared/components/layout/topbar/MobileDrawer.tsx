import { FC } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Box, Divider, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText } from "@mui/material";
import LoginOutlined from "@mui/icons-material/LoginOutlined";
import PersonAddOutlined from "@mui/icons-material/PersonAddOutlined";
import { mobileNavigationElements } from "./NavigationElement";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { useAuthContext } from "@/features/auth/context";

interface MobileDrawerComponentProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

export const MobileDrawerComponent: FC<MobileDrawerComponentProps> = ({ isOpen, setIsOpen }) => {
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const { pageSpecificNavigation } = useDeviceContext();
    const { isAuthorized } = useAuthContext();

    const isNavActive = (href: string) => pathname === href;

    return (
        <Drawer
            open={isOpen}
            onClose={() => {
                setIsOpen(false);
            }}
        >
            <Box
                sx={{ width: "min(250px, 80vw)" }}
                role="presentation"
                onClick={() => {
                    setIsOpen(false);
                }}
            >
                <List>
                    {mobileNavigationElements.map((element) => (
                        <ListItem key={element.text} disablePadding>
                            <ListItemButton
                                onClick={() => {
                                    void navigate(element.href);
                                }}
                                selected={isNavActive(element.href)}
                            >
                                {element.icon && <ListItemIcon>{element.icon}</ListItemIcon>}
                                <ListItemText primary={element.text} />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>

                {pageSpecificNavigation.length != 0 && <Divider />}

                {pageSpecificNavigation.length != 0 && (
                    <List>
                        {pageSpecificNavigation.map((element) => (
                            <ListItem key={element.text} disablePadding>
                                <ListItemButton
                                    onClick={() => {
                                        element.onClick();
                                    }}
                                    selected={element.isSelected}
                                >
                                    {element.icon && <ListItemIcon>{element.icon}</ListItemIcon>}
                                    <ListItemText primary={element.text} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                )}

                {!isAuthorized && (
                    <>
                        <Divider />
                        <List>
                            <ListItem disablePadding>
                                <ListItemButton
                                    onClick={() => {
                                        void navigate("/sign-in");
                                    }}
                                    selected={pathname === "/sign-in"}
                                >
                                    <ListItemIcon>
                                        <LoginOutlined />
                                    </ListItemIcon>
                                    <ListItemText primary="Sign In" />
                                </ListItemButton>
                            </ListItem>
                            <ListItem disablePadding>
                                <ListItemButton
                                    onClick={() => {
                                        void navigate("/sign-up");
                                    }}
                                    selected={pathname === "/sign-up"}
                                >
                                    <ListItemIcon>
                                        <PersonAddOutlined />
                                    </ListItemIcon>
                                    <ListItemText primary="Sign Up" />
                                </ListItemButton>
                            </ListItem>
                        </List>
                    </>
                )}
            </Box>
        </Drawer>
    );
};
