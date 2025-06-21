import React, { FC, useState } from "react";
import { Badge, Box, IconButton } from "@mui/material";
import { useNotificationsCountRequest } from "../../http/requests";
import { NotificationMenuComponent } from "./NotificationMenuComponent";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useAuthContext } from "../auth/AuthContextProvider";

interface NotificationExpandableMenuProps {
    px?: number;
}

export const NotificationButton: FC<NotificationExpandableMenuProps> = ({ px = 2 }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | undefined>(undefined);

    const { isAuthorized } = useAuthContext();
    const { data: notificationsCount } = useNotificationsCountRequest(false);

    const handleClose = () => setAnchorEl(undefined);
    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    return (
        <Box hidden={ !isAuthorized } px={px}>
            <NotificationMenuComponent anchorEl={anchorEl} onClose={handleClose}/>
            <IconButton aria-label="show notifications" aria-haspopup="true"
                color={"primary"} edge="end" onClick={handleOpen}>
                <Badge badgeContent={ notificationsCount || 0 } color="primary">
                    <NotificationsIcon />
                </Badge>
            </IconButton>
        </Box>
    );
};
