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
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { CheckResultDto, CheckResultDtoErrorType } from "@/features/checks/types";
import { formatDate } from "@/shared/utils/date";
import { useTranslation } from "react-i18next";

type CheckResultDetailDialogProps = {
    checkResultId: number | null;
    onClose: () => void;
};

export const CheckResultDetailDialog: FC<CheckResultDetailDialogProps> = ({ checkResultId, onClose }) => {
    const { data, isLoading } = useCheckResultDetailQuery(checkResultId);
    const { isMobile } = useDeviceContext();
    const { t } = useTranslation("checks");

    return (
        <Dialog open={checkResultId !== null} onClose={onClose} maxWidth="sm" fullWidth fullScreen={isMobile}>
            <DialogTitle component="div">
                <Stack direction="row" alignItems="center" justifyContent="space-between">
                    <Stack direction="row" alignItems="center" spacing={1.5}>
                        {data && <StatusAvatar status={data.status} />}
                        <Typography variant="h6">{t("check_result_title")}</Typography>
                    </Stack>
                    <IconButton onClick={onClose} size="small" aria-label={t("close_label")}>
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
    const { t, i18n } = useTranslation("checks");
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
                <MetaItem label={t("trust_level_label")} value={`${String(Math.round(data.trustLevel * 100))}%`} />
                {data.errorType !== "NONE" && (
                    <MetaItem label={t("error_type_label")} value={formatErrorType(data.errorType)} />
                )}
                <MetaItem label={t("checked_at_label")} value={formatDate(data.createdAt, { locale: i18n.language })} />
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
    const { t } = useTranslation("checks");
    const { icon, color, tooltipKey } = statusVisuals(status);
    return (
        <Tooltip title={t(tooltipKey)}>
            <Avatar sx={{ bgcolor: color, width: 32, height: 32 }}>{icon}</Avatar>
        </Tooltip>
    );
}

function statusVisuals(status: CheckResultDto["status"]): { icon: ReactNode; color: string; tooltipKey: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon fontSize="small" />, color: "success.main", tooltipKey: "correct_tooltip" };
        case "MISTAKE":
            return { icon: <ErrorIcon fontSize="small" />, color: "error.main", tooltipKey: "mistake_tooltip" };
        case "INTERNAL_ERROR":
            return { icon: <InternalIcon fontSize="small" />, color: "warning.main", tooltipKey: "checker_error_tooltip" };
        default:
            return { icon: <ErrorIcon fontSize="small" />, color: "grey.500", tooltipKey: "unknown_tooltip" };
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
