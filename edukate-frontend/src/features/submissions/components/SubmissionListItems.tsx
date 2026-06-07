import { Submission, SubmissionStatus } from "@/features/submissions/types";
import { getApiErrorMessage } from "@/lib/api-error";
import { MouseEvent, ReactNode, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    Avatar,
    Badge,
    Box,
    CircularProgress,
    IconButton,
    ListItem,
    ListItemAvatar,
    ListItemButton,
    ListItemText,
    Skeleton,
    Stack,
    Tooltip,
    Typography,
} from "@mui/material";
import { formatDate } from "@/shared/utils/date";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import AutoAwesomeOutlinedIcon from "@mui/icons-material/AutoAwesomeOutlined";
import SupervisorAccountOutlinedIcon from "@mui/icons-material/SupervisorAccountOutlined";
import { useNavigate } from "react-router-dom";

export type RowCheckType = "self" | "ai" | "supervisor";

export function SubmissionListItem({
    submission,
    openImages,
    onSelect,
    onSelfCheck,
    onSmartCheck,
    onSupervisorCheck,
    isAiCheckDisabled,
    pendingCheckType,
    isAnyCheckPending,
}: {
    submission: Submission;
    openImages: (images: string[], index: number) => void;
    onSelect?: (submission: Submission) => void;
    onSelfCheck: (submission: Submission) => void;
    onSmartCheck: (submission: Submission) => void;
    onSupervisorCheck: (submission: Submission) => void;
    isAiCheckDisabled: boolean;
    pendingCheckType: RowCheckType | null;
    isAnyCheckPending: boolean;
}) {
    const { t, i18n } = useTranslation("submissions");
    const { icon, color } = getStatusVisuals(submission.status);

    const attachments = useMemo(() => submission.fileUrls, [submission.fileUrls]);
    const navigate = useNavigate();
    return (
        <ListItem
            disablePadding
            secondaryAction={
                <RowActions
                    attachments={attachments}
                    submission={submission}
                    openImages={openImages}
                    onSelfCheck={onSelfCheck}
                    onSmartCheck={onSmartCheck}
                    onSupervisorCheck={onSupervisorCheck}
                    isAiCheckDisabled={isAiCheckDisabled}
                    pendingCheckType={pendingCheckType}
                    isAnyCheckPending={isAnyCheckPending}
                />
            }
        >
            <ListItemButton
                onClick={() => {
                    if (onSelect) {
                        onSelect(submission);
                    } else {
                        void navigate(`/submissions/${String(submission.id)}`);
                    }
                }}
            >
                <ListItemAvatar>
                    <Avatar sx={{ bgcolor: color, color: "common.white" }}>{icon}</Avatar>
                </ListItemAvatar>
                <ListItemText
                    primary={t(primaryTextKey(submission.status))}
                    secondary={formatDate(submission.createdAt, { locale: i18n.language })}
                />
            </ListItemButton>
        </ListItem>
    );
}

function RowActions({
    attachments,
    submission,
    openImages,
    onSelfCheck,
    onSmartCheck,
    onSupervisorCheck,
    isAiCheckDisabled,
    pendingCheckType,
    isAnyCheckPending,
}: {
    attachments: string[];
    submission: Submission;
    openImages: (images: string[], index: number) => void;
    onSelfCheck: (submission: Submission) => void;
    onSmartCheck: (submission: Submission) => void;
    onSupervisorCheck: (submission: Submission) => void;
    isAiCheckDisabled: boolean;
    pendingCheckType: RowCheckType | null;
    isAnyCheckPending: boolean;
}) {
    const { t } = useTranslation("submissions");
    const showSelf = submission.status !== "SUCCESS";
    const showSmart = !isAiCheckDisabled;

    const stopAnd = (handler: () => void) => (e: MouseEvent) => {
        e.stopPropagation();
        e.preventDefault();
        handler();
    };

    const iconOrSpinner = (forType: RowCheckType, fallback: ReactNode): ReactNode =>
        pendingCheckType === forType ? <CircularProgress size={16} /> : fallback;

    return (
        <Stack direction="row" spacing={0.5} alignItems="center">
            {attachments.length > 0 && (
                <Tooltip title={t("attachments_tooltip", { count: attachments.length })}>
                    <IconButton
                        size="small"
                        aria-label={t("attachments_tooltip", { count: attachments.length })}
                        onClick={stopAnd(() => {
                            openImages(attachments, 0);
                        })}
                    >
                        <Badge
                            badgeContent={attachments.length}
                            color="primary"
                            overlap="circular"
                            invisible={attachments.length <= 1}
                        >
                            <AttachFileIcon fontSize="small" />
                        </Badge>
                    </IconButton>
                </Tooltip>
            )}
            {showSelf && (
                <Tooltip title={t("self_check_tooltip")}>
                    <span>
                        <IconButton
                            size="small"
                            color="success"
                            disabled={isAnyCheckPending}
                            aria-label={t("self_check_tooltip")}
                            onClick={stopAnd(() => {
                                onSelfCheck(submission);
                            })}
                        >
                            {iconOrSpinner("self", <CheckCircleOutlineIcon fontSize="small" />)}
                        </IconButton>
                    </span>
                </Tooltip>
            )}
            {showSmart && (
                <Tooltip title={t("smart_check_tooltip")}>
                    <span>
                        <IconButton
                            size="small"
                            color="primary"
                            disabled={isAnyCheckPending}
                            aria-label={t("smart_check_tooltip")}
                            onClick={stopAnd(() => {
                                onSmartCheck(submission);
                            })}
                        >
                            {iconOrSpinner("ai", <AutoAwesomeOutlinedIcon fontSize="small" />)}
                        </IconButton>
                    </span>
                </Tooltip>
            )}
            <Tooltip title={t("supervisor_check_tooltip")}>
                <span>
                    <IconButton
                        size="small"
                        disabled={isAnyCheckPending}
                        aria-label={t("supervisor_check_tooltip")}
                        onClick={stopAnd(() => {
                            onSupervisorCheck(submission);
                        })}
                    >
                        {iconOrSpinner("supervisor", <SupervisorAccountOutlinedIcon fontSize="small" />)}
                    </IconButton>
                </span>
            </Tooltip>
        </Stack>
    );
}

export function ErrorListItem({ error }: { error: unknown }) {
    const { t } = useTranslation("submissions");
    return (
        <ListItem key={"error"}>
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: "error.light", color: "error.contrastText" }}>
                    <ErrorIcon />
                </Avatar>
            </ListItemAvatar>
            <ListItemText
                primary={t("submissions_load_error")}
                secondary={
                    <Typography variant="body2" color="text.secondary">
                        {getApiErrorMessage(error)}
                    </Typography>
                }
            />
        </ListItem>
    );
}

export function EmptySubmissionListStub() {
    const { t } = useTranslation("submissions");
    return (
        <ListItem>
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: "action.selected", color: "text.secondary" }}>
                    <InboxOutlinedIcon />
                </Avatar>
            </ListItemAvatar>
            <ListItemText primary={t("no_submissions_yet")} secondary={t("no_submissions_yet_description")} />
        </ListItem>
    );
}

export function StubListItem() {
    return (
        <ListItem
            secondaryAction={
                <Box sx={{ minWidth: 160, display: "flex", gap: 1 }}>
                    <Skeleton variant="circular" width={28} height={28} />
                    <Skeleton variant="circular" width={28} height={28} />
                    <Skeleton variant="circular" width={28} height={28} />
                    <Skeleton variant="circular" width={28} height={28} />
                </Box>
            }
        >
            <ListItemAvatar>
                <Skeleton variant="circular" width={40} height={40} />
            </ListItemAvatar>
            <ListItemText
                primary={<Skeleton variant="text" width="40%" />}
                secondary={<Skeleton variant="text" width="25%" sx={{ mt: 0.5 }} />}
            />
        </ListItem>
    );
}

function primaryTextKey(status: SubmissionStatus) {
    switch (status) {
        case "SUCCESS":
            return "status_success";
        case "PENDING":
            return "status_pending";
        case "FAILED":
            return "status_failed";
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
