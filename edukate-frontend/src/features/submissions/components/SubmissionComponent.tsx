import { FC, ReactNode, useState } from "react";
import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Divider,
    Paper,
    Skeleton,
    Stack,
    Tooltip,
    Typography,
} from "@mui/material";
import { alpha } from "@mui/material/styles";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import AssignmentLateOutlinedIcon from "@mui/icons-material/AssignmentLateOutlined";
import { Submission, SubmissionStatus } from "@/features/submissions/types";
import { useCheckResultsRequest, useRequestCheckMutation } from "@/features/checks/api";
import { CheckType } from "@/features/checks/types";
import { CheckResultInfoList } from "@/features/checks/components/CheckResultInfoList";
import { CheckResultDetailDialog } from "@/features/checks/components/CheckResultDetailDialog";
import { useAuthContext } from "@/features/auth/context";
import { Role } from "@/features/auth/types";
import { formatDate } from "@/shared/utils/date";
import { ImageLightbox } from "@/shared/components/images/ImageLightbox";
import { useTranslation } from "react-i18next";

type PaletteColor = "success" | "warning" | "error";

type StatusVisuals = {
    icon: ReactNode;
    labelKey: string;
    paletteColor: PaletteColor;
};

type SubmissionComponentProps = {
    submission: Submission;
    isOwner?: boolean;
};

export const SubmissionComponent: FC<SubmissionComponentProps> = ({ submission, isOwner = true }) => {
    const requestCheckMutation = useRequestCheckMutation();
    const requestCheck = (checkType: CheckType) => {
        requestCheckMutation.mutate({ checkType, submissionId: String(submission.id), problemKey: submission.problemKey });
    };
    const { data: resultInfos, isLoading, error } = useCheckResultsRequest(String(submission.id));
    const { user } = useAuthContext();
    const [selectedCheckResultId, setSelectedCheckResultId] = useState<number | null>(null);
    const [lightboxIndex, setLightboxIndex] = useState(-1);

    const isSelfCheckDisabled = submission.status == "SUCCESS";
    const isAiCheckDisabled = !["MODERATOR" as Role, "ADMIN" as Role].some((role) => user?.roles.includes(role));

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, p: 2 }}>
            {isOwner && (
                <CheckResultDetailDialog
                    checkResultId={selectedCheckResultId}
                    onClose={() => {
                        setSelectedCheckResultId(null);
                    }}
                />
            )}

            {isOwner && (
                <ImageLightbox
                    images={submission.fileUrls}
                    index={lightboxIndex}
                    open={lightboxIndex >= 0}
                    onClose={() => {
                        setLightboxIndex(-1);
                    }}
                />
            )}

            <StatusHero submission={submission} />

            <DetailsSection submission={submission} />

            {isOwner && submission.fileUrls.length > 0 && (
                <FilesSection fileUrls={submission.fileUrls} onPreview={setLightboxIndex} />
            )}

            <CheckResultsSection
                isLoading={isLoading}
                error={error}
                resultInfos={resultInfos}
                onItemClick={isOwner ? setSelectedCheckResultId : undefined}
                isSelfCheckDisabled={isSelfCheckDisabled}
                isAiCheckDisabled={isAiCheckDisabled}
                isCheckPending={requestCheckMutation.isPending}
                pendingCheckType={requestCheckMutation.variables?.checkType}
                onRequestCheck={requestCheck}
                showActions={isOwner}
            />
        </Box>
    );
};

function StatusHero({ submission }: SubmissionComponentProps) {
    const { t, i18n } = useTranslation("submissions");
    const { icon, labelKey, paletteColor } = getStatusVisuals(submission.status);
    return (
        <Paper
            variant="outlined"
            sx={{
                bgcolor: (theme) => alpha(theme.palette[paletteColor].main, 0.08),
                borderColor: `${paletteColor}.main`,
                borderRadius: 2,
                py: 3,
                px: 2,
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 0.5,
            }}
        >
            <Box sx={{ color: `${paletteColor}.main`, display: "flex", fontSize: 48 }}>{icon}</Box>
            <Typography variant="h6" sx={{ color: `${paletteColor}.main`, fontWeight: 600 }}>
                {t(labelKey)}
            </Typography>
            <Typography variant="caption" color="text.secondary">
                {formatDate(submission.createdAt, { locale: i18n.language })}
            </Typography>
        </Paper>
    );
}

function DetailsSection({ submission }: SubmissionComponentProps) {
    const { t } = useTranslation("submissions");
    return (
        <Stack spacing={1}>
            <DetailRow label={t("problem_label")} value={submission.problemKey} />
            <DetailRow label={t("submitted_by_label")} value={submission.userName} />
        </Stack>
    );
}

function DetailRow({ label, value }: { label: string; value: string }) {
    return (
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", gap: 2 }}>
            <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0 }}>
                {label}
            </Typography>
            <Typography variant="body2" sx={{ textAlign: "right", wordBreak: "break-all" }}>
                {value}
            </Typography>
        </Box>
    );
}

function FilesSection({ fileUrls, onPreview }: { fileUrls: string[]; onPreview: (index: number) => void }) {
    const { t } = useTranslation("submissions");
    return (
        <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block", mb: 1 }}>
                {t("attached_files_label")}
            </Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                {fileUrls.map((_, i) => (
                    <Chip
                        key={i}
                        icon={<AttachFileIcon />}
                        label={t("attached_file_label", { index: i + 1 })}
                        size="small"
                        variant="outlined"
                        color="primary"
                        onClick={() => {
                            onPreview(i);
                        }}
                        clickable
                    />
                ))}
            </Stack>
        </Box>
    );
}

type CheckResultsSectionProps = {
    isLoading: boolean;
    error: unknown;
    resultInfos: ReturnType<typeof useCheckResultsRequest>["data"];
    onItemClick?: (id: number) => void;
    isSelfCheckDisabled: boolean;
    isAiCheckDisabled: boolean;
    isCheckPending: boolean;
    pendingCheckType?: CheckType;
    onRequestCheck: (checkType: CheckType) => void;
    showActions?: boolean;
};

function CheckResultsSection({
    isLoading,
    error,
    resultInfos,
    onItemClick,
    isSelfCheckDisabled,
    isAiCheckDisabled,
    isCheckPending,
    pendingCheckType,
    onRequestCheck,
    showActions = true,
}: CheckResultsSectionProps) {
    const { t } = useTranslation("submissions");
    const count = resultInfos?.length ?? 0;

    return (
        <Box>
            <Typography variant="subtitle2" gutterBottom>
                {t("check_results_heading")}
                {count > 0 ? ` (${String(count)})` : ""}
            </Typography>

            {isLoading && (
                <Stack spacing={1}>
                    <Skeleton variant="rectangular" height={56} sx={{ borderRadius: 1 }} />
                    <Skeleton variant="rectangular" height={56} sx={{ borderRadius: 1 }} />
                    <Skeleton variant="rectangular" height={56} sx={{ borderRadius: 1 }} />
                </Stack>
            )}

            {!isLoading && !!error && (
                <Alert severity="error" sx={{ mb: 1 }}>
                    {t("check_results_load_error")}
                </Alert>
            )}

            {!isLoading && !error && resultInfos && resultInfos.length === 0 && (
                <Stack alignItems="center" spacing={1} py={3} color="text.disabled">
                    <AssignmentLateOutlinedIcon sx={{ fontSize: 40 }} />
                    <Typography variant="body2">{t("check_results_empty")}</Typography>
                </Stack>
            )}

            {!isLoading && resultInfos && resultInfos.length > 0 && (
                <CheckResultInfoList data={resultInfos} onItemClick={onItemClick} />
            )}

            {showActions && (
                <>
                    <Divider sx={{ my: 2 }}>
                        <Typography variant="caption" color="text.secondary">
                            Actions
                        </Typography>
                    </Divider>

                    <Stack direction="row" spacing={1}>
                        <Tooltip
                            title={
                                isSelfCheckDisabled ? t("consider_solved_disabled_tooltip") : t("consider_solved_tooltip")
                            }
                        >
                            <span>
                                <Button
                                    size="small"
                                    variant="outlined"
                                    disabled={isSelfCheckDisabled || isCheckPending}
                                    startIcon={
                                        isCheckPending && pendingCheckType === "self" ? (
                                            <CircularProgress size={16} />
                                        ) : undefined
                                    }
                                    onClick={() => {
                                        onRequestCheck("self");
                                    }}
                                >
                                    {t("consider_solved_button")}
                                </Button>
                            </span>
                        </Tooltip>
                        <Tooltip
                            title={
                                isAiCheckDisabled ? t("request_ai_check_disabled_tooltip") : t("request_ai_check_tooltip")
                            }
                        >
                            <span>
                                <Button
                                    size="small"
                                    variant="outlined"
                                    disabled={isAiCheckDisabled || isCheckPending}
                                    startIcon={
                                        isCheckPending && pendingCheckType === "ai" ? (
                                            <CircularProgress size={16} />
                                        ) : undefined
                                    }
                                    onClick={() => {
                                        onRequestCheck("ai");
                                    }}
                                >
                                    {t("request_ai_check_button")}
                                </Button>
                            </span>
                        </Tooltip>
                    </Stack>
                </>
            )}
        </Box>
    );
}

function getStatusVisuals(status: SubmissionStatus): StatusVisuals {
    switch (status) {
        case "SUCCESS":
            return {
                icon: <CheckCircleOutlineIcon fontSize="inherit" />,
                labelKey: "status_success",
                paletteColor: "success",
            };
        case "PENDING":
            return {
                icon: <HourglassEmptyIcon fontSize="inherit" />,
                labelKey: "status_pending",
                paletteColor: "warning",
            };
        case "FAILED":
            return {
                icon: <CancelOutlinedIcon fontSize="inherit" />,
                labelKey: "status_failed",
                paletteColor: "error",
            };
        default:
            return exhaustiveGuard(status);
    }
}

function exhaustiveGuard(x: unknown): never {
    throw new Error(`Unhandled status: ${x as string}`);
}
