import { FC } from "react";
import { Avatar, Badge, Box, Stack, Typography } from "@mui/material";
import { CheckedNotification } from "../../types/notification/CheckedNotification";
import { formatDate } from "../../utils/date";

interface CheckedNotificationComponentProps {
    notification: CheckedNotification;
}

export const CheckedNotificationComponent: FC<CheckedNotificationComponentProps> = ({ notification }) => {
    return (
        <Stack direction={"row"} alignItems={"center"}>
            <Badge color="secondary" variant="dot" invisible={notification.isRead} sx={{mr: 2}}>
                <Avatar alt="edukate" />
            </Badge>
            <Box>
                <Typography component={"span"} variant={"h6"} textAlign={"center"} color={"primary"}>
                    Submission Checked
                </Typography>
                <Box>
                    <Box>
                        <Typography component={"span"} variant={"body1"}>
                            Your submission for problem { notification.problemId } has been checked with status { notification.status }!
                        </Typography>
                    </Box>
                    <Box>
                        <Typography component="span" variant="caption">
                            { formatDate(notification.createdAt) }
                        </Typography>
                    </Box>
                </Box>
            </Box>
        </Stack>
    );
};