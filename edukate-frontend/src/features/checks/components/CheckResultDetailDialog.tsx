import { FC, ReactNode } from "react";
import {
    Avatar,
    Box,
    CircularProgress,
    Dialog,
    DialogContent,
    DialogTitle,
    Divider,
    IconButton,
    Stack,
    Tooltip,
    Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import InternalIcon from "@mui/icons-material/Storage";
import { useCheckResultDetailQuery } from "@/features/checks/api";
import { CheckResultDto, CheckResultDtoErrorType } from "@/features/checks/types";
import { formatDate } from "@/shared/utils/date";

type CheckResultDetailDialogProps = {
    checkResultId: number | null;
    onClose: () => void;
};

export const CheckResultDetailDialog: FC<CheckResultDetailDialogProps> = ({ checkResultId, onClose }) => {
    const { data, isLoading } = useCheckResultDetailQuery(checkResultId);

    return (
        <Dialog open={checkResultId !== null} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle component="div">
                <Stack direction="row" alignItems="center" justifyContent="space-between">
                    <Stack direction="row" alignItems="center" spacing={1.5}>
                        {data && <StatusAvatar status={data.status} />}
                        <Typography variant="h6">Check Result</Typography>
                    </Stack>
                    <IconButton onClick={onClose} size="small" aria-label="Close">
                        <CloseIcon />
                    </IconButton>
                </Stack>
            </DialogTitle>

            <Divider />

            <DialogContent>
                {isLoading && (
                    <Box display="flex" justifyContent="center" py={3}>
                        <CircularProgress />
                    </Box>
                )}

                {data && <CheckResultBody data={data} />}
            </DialogContent>
        </Dialog>
    );
};

function CheckResultBody({ data }: { data: CheckResultDto }) {
    return (
        <Stack spacing={2}>
            <Box
                sx={{
                    p: 2,
                    borderRadius: 1,
                    bgcolor: "action.hover",
                    borderLeft: 4,
                    borderColor: statusColor(data.status),
                }}
            >
                <Typography variant="body1" sx={{ whiteSpace: "pre-wrap" }}>
                    {data.explanation}
                </Typography>
            </Box>

            <Stack direction="row" spacing={3} flexWrap="wrap">
                <MetaItem label="Trust level" value={`${String(Math.round(data.trustLevel * 100))}%`} />
                {data.errorType !== "NONE" && <MetaItem label="Error type" value={formatErrorType(data.errorType)} />}
                <MetaItem label="Checked at" value={formatDate(data.createdAt)} />
            </Stack>
        </Stack>
    );
}

function MetaItem({ label, value }: { label: string; value: string }) {
    return (
        <Box>
            <Typography variant="caption" color="text.secondary" display="block">
                {label}
            </Typography>
            <Typography variant="body2">{value}</Typography>
        </Box>
    );
}

function StatusAvatar({ status }: { status: CheckResultDto["status"] }) {
    const { icon, color, tooltip } = statusVisuals(status);
    return (
        <Tooltip title={tooltip}>
            <Avatar sx={{ bgcolor: color, width: 32, height: 32 }}>{icon}</Avatar>
        </Tooltip>
    );
}

function statusVisuals(status: CheckResultDto["status"]): { icon: ReactNode; color: string; tooltip: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon fontSize="small" />, color: "success.main", tooltip: "Correct" };
        case "MISTAKE":
            return { icon: <ErrorIcon fontSize="small" />, color: "error.main", tooltip: "Contains a mistake" };
        case "INTERNAL_ERROR":
            return { icon: <InternalIcon fontSize="small" />, color: "warning.main", tooltip: "Checker error" };
        default:
            return { icon: <ErrorIcon fontSize="small" />, color: "grey.500", tooltip: "Unknown" };
    }
}

function statusColor(status: CheckResultDto["status"]): string {
    switch (status) {
        case "SUCCESS":
            return "success.main";
        case "MISTAKE":
            return "error.main";
        case "INTERNAL_ERROR":
            return "warning.main";
        default:
            return "grey.500";
    }
}

function formatErrorType(errorType: CheckResultDtoErrorType): string {
    return errorType.charAt(0) + errorType.slice(1).toLowerCase();
}
