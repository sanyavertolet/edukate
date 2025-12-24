import { Submission, SubmissionStatus } from "../../types/submission/Submission";
import { ReactNode, useMemo } from "react";
import {
    Avatar,
    Box,
    Button,
    ListItem,
    ListItemAvatar,
    ListItemButton,
    ListItemText,
    Skeleton,
    Typography
} from "@mui/material";
import { formatDate } from "../../utils/date";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import { useNavigate } from "react-router-dom";

export function SubmissionListItem({ submission, setImage }: { submission: Submission; setImage: (image: string) => void }) {
    const { icon, color } = getStatusVisuals(submission.status);

    const attachments = useMemo(() => submission.fileUrls ?? [], [submission.fileUrls]);
    const navigate = useNavigate()
    return (
        <ListItem
            disablePadding
            onClick={() => navigate(`/submissions/${submission.id}`)}
            secondaryAction={ AttachmentButtonList(attachments, setImage) }
        >
            <ListItemButton onClick={() => navigate(`/submissions/${submission.id}`)}>
                <ListItemAvatar>
                    <Avatar sx={{ bgcolor: color, color: "common.white" }}>{icon}</Avatar>
                </ListItemAvatar>
                <ListItemText
                    primary={primaryText(submission.status)}
                    secondary={formatDate(submission.createdAt)}
                />
            </ListItemButton>
        </ListItem>
    );
}

export function AttachmentButtonList(attachments: string[] = [], openImage: (image: string) => void) {
    return (
        <Box sx={{ display: "flex", gap: 1 }}>
            { attachments.map((fileUrl, i) => (
                <Button
                    key={i}
                    variant="text"
                    size="small"
                    aria-label={`Open attachment ${i + 1}`}
                    onClick={ e => { e.stopPropagation(); openImage(fileUrl); }}
                >
                    {i + 1}
                </Button>
            ))}
        </Box>
    );
}

export function ErrorListItem({ error }: { error: Error }) {
    return (
        <ListItem key={"error"}>
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: "error.light", color: "error.contrastText" }}>
                    <ErrorIcon />
                </Avatar>
            </ListItemAvatar>
            <ListItemText
                primary="Failed to load submissions"
                secondary={
                    <Typography variant="body2" color="text.secondary">
                        {String((error)?.message ?? "Please try again later.")}
                    </Typography>
                }
            />
        </ListItem>
    );
}

export function EmptySubmissionListStub() {
    return (
        <ListItem>
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: "action.selected", color: "text.secondary" }}>
                    <InboxOutlinedIcon />
                </Avatar>
            </ListItemAvatar>
            <ListItemText primary="No submissions yet" secondary="Your submissions will appear here after you upload a solution." />
        </ListItem>
    );
}

export function StubListItem() {
    return (
        <ListItem
            secondaryAction={
                <Box sx={{ minWidth: 72, display: "flex", gap: 1 }}>
                    <Skeleton variant="rounded" width={32} height={28} />
                    <Skeleton variant="rounded" width={32} height={28} />
                </Box>
            }
        >
            <ListItemAvatar>
                <Skeleton variant="circular" width={40} height={40} />
            </ListItemAvatar>
            <ListItemText
                primary={<Skeleton variant="text" width="40%" />}
                secondary={
                    <Box sx={{ mt: 0.5 }}>
                        <Skeleton variant="text" width="25%" />
                    </Box>
                }
            />
        </ListItem>
    );
}

function primaryText(status: SubmissionStatus) {
    switch (status) {
        case "SUCCESS":
            return "Success";
        case "PENDING":
            return "Pending review";
        case "FAILED":
            return "Failed";
        default:
            return exhaustiveGuard(status);
    }
}

function getStatusVisuals(status: SubmissionStatus): { icon: ReactNode; color: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon />, color: "success.main" };
        case "PENDING":
            return { icon: <PendingIcon />, color: "warning.main" };
        case "FAILED":
            return { icon: <ErrorIcon />, color: "error.main" };
        default:
            return exhaustiveGuard(status);
    }
}

function exhaustiveGuard(x: unknown): never {
    throw new Error(`Unhandled status: ${x as string}`);
}
