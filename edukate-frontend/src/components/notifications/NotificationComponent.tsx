import { BaseNotification } from "../../types/notifications/BaseNotification";
import React, { FC } from "react";
import { ListItem, ListItemAvatar, ListItemText } from "@mui/material";
import { SimpleNotificationComponent } from "./SimpleNotificationComponent";
import { SimpleNotification } from "../../types/notifications/SimpleNotification";
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';

interface NotificationComponentProps {
    notification: BaseNotification;
    onNotificationClick: (uuid: string) => void;
}

export const NotificationComponent: FC<NotificationComponentProps> = ({ notification, onNotificationClick }) => {
    if (notification._type === "simple") {
        return <SimpleNotificationComponent
            notification={ notification as SimpleNotification }
            onNotificationClick={onNotificationClick}
        />
    }
    return <BaseNotificationComponent notification={notification} onNotificationClick={onNotificationClick}/>
};

const BaseNotificationComponent: FC<NotificationComponentProps> = ({notification}) => {
    return (
        <ListItem alignItems="flex-start">
            <ListItemAvatar>
                <HelpOutlineIcon/>
            </ListItemAvatar>
            <ListItemText
                primary={notification._type}
                secondary={
                    <React.Fragment>
                        {"Unknown notification"}
                        { notification.createdAt.toISOString() }
                    </React.Fragment>
                }
            />
        </ListItem>
    );
};
