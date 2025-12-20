import { useMySubmissionsQuery } from "../../http/requests/submissions";
import { ReactNode, useMemo, useState } from "react";
import { Avatar, Box, Button, List, ListItem, ListItemAvatar, ListItemText, Skeleton, Typography } from "@mui/material";
import { ZoomingImageDialog } from "../images/ZoomingImageDialog";
import { Submission, SubmissionStatus } from "../../types/submission/Submission";
import { formatDate } from "../../utils/date";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";

interface SubmissionListComponentProps {
    problemId?: string;
}

export function SubmissionListComponent({ problemId }: SubmissionListComponentProps) {
    const { data: submissions, isLoading, isError, error } = useMySubmissionsQuery(problemId);
    const [selectedImage, setSelectedImage] = useState<string | null>(null);

    const showEmpty = !isLoading && !isError && submissions && submissions.length === 0;

    return (
        <Box>
            <ZoomingImageDialog selectedImage={selectedImage} handleClose={() => setSelectedImage(null)} />
            <List>
                {isLoading && (
                    <>
                        {Array.from({ length: 3 }).map((_, i) => (
                            <StubListItem key={`stub-${i}`} />
                        ))}
                    </>
                )}

                {isError && (
                    <ListItem>
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
                )}

                {showEmpty && <EmptySubmissionListStub />}

                {!isLoading && !isError && submissions && submissions.length > 0 &&
                    submissions.map((submission) => (
                        <SubmissionListItem
                            key={composeSubmissionKey(submission)}
                            submission={submission}
                            setImage={setSelectedImage}
                        />
                    ))}
            </List>
        </Box>
    );
}

function composeSubmissionKey(s: Submission) {
    return `${s.problemId}:${s.userName}:${s.createdAt}`;
}

function SubmissionListItem({ submission, setImage }: { submission: Submission; setImage: (image: string) => void }) {
    const { icon, color } = getStatusVisuals(submission.status);

    const attachments = useMemo(() => submission.fileUrls ?? [], [submission.fileUrls]);

    return (
        <ListItem
            secondaryAction={
                attachments.map((fileUrl, i) => (
                    <Button
                        key={`${composeSubmissionKey(submission)}:file:${i}`}
                        variant="text"
                        size="small"
                        aria-label={`Open attachment ${i + 1}`}
                        onClick={() => setImage(fileUrl)}
                    >
                        {i + 1}
                    </Button>
                ))
            }
        >
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: color, color: "common.white" }}>{icon}</Avatar>
            </ListItemAvatar>
            <ListItemText primary={primaryText(submission.status)} secondary={formatDate(submission.createdAt)} />
        </ListItem>
    );
}

function EmptySubmissionListStub() {
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

function StubListItem() {
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
            return "Ok";
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

function exhaustiveGuard(x: never): never {
    throw new Error(`Unhandled status: ${x as unknown as string}`);
}