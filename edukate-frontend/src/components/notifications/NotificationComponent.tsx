import { BaseNotification } from "../../types/notification/BaseNotification";
import React, { FC } from "react";
import { ListItem, ListItemAvatar, ListItemText } from "@mui/material";
import { SimpleNotificationComponent } from "./SimpleNotificationComponent";
import { SimpleNotification } from "../../types/notification/SimpleNotification";
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { InviteNotification } from "../../types/notification/InviteNotification";
import { InviteNotificationComponent } from "./InviteNotificationComponent";

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
    } else if (notification._type === "invite") {
        return <InviteNotificationComponent
            notification={ notification as InviteNotification }
            markAsRead={onNotificationClick}
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
