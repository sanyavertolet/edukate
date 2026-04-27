import { FC } from "react";
import { ListItem, ListItemAvatar, ListItemText, Skeleton } from "@mui/material";

interface NotificationSkeletonProps {
    count?: number;
}

export const NotificationSkeleton: FC<NotificationSkeletonProps> = ({ count = 5 }) => (
    <>
        {Array.from({ length: count }, (_, i) => (
            <ListItem key={i} sx={{ px: 2, py: 1 }}>
                <ListItemAvatar>
                    <Skeleton variant="circular" width={40} height={40} />
                </ListItemAvatar>
                <ListItemText
                    primary={<Skeleton variant="text" width="60%" />}
                    secondary={
                        <>
                            <Skeleton variant="text" width="80%" />
                            <Skeleton variant="text" width="30%" />
                        </>
                    }
                />
            </ListItem>
        ))}
    </>
);
