import { FC, ReactNode, useState } from "react";
import {
    Alert,
    Box,
    Button,
    Chip,
    Dialog,
    DialogContent,
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

type PaletteColor = "success" | "warning" | "error";

type StatusVisuals = {
    icon: ReactNode;
    label: string;
    paletteColor: PaletteColor;
};

type SubmissionComponentProps = {
    submission: Submission;
};

export const SubmissionComponent: FC<SubmissionComponentProps> = ({ submission }) => {
    const requestCheckMutation = useRequestCheckMutation();
    const requestCheck = (checkType: CheckType) => {
        requestCheckMutation.mutate({ checkType, submissionId: String(submission.id) });
    };
    const { data: resultInfos, isLoading, error } = useCheckResultsRequest(String(submission.id));
    const { user } = useAuthContext();
    const [selectedCheckResultId, setSelectedCheckResultId] = useState<number | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    const isSelfCheckDisabled = submission.status == "SUCCESS";
    const isAiCheckDisabled = !["MODERATOR" as Role, "ADMIN" as Role].some((role) => user?.roles.includes(role));

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, p: 2 }}>
            <CheckResultDetailDialog
                checkResultId={selectedCheckResultId}
                onClose={() => {
                    setSelectedCheckResultId(null);
                }}
            />

            <Dialog
                open={previewUrl !== null}
                onClose={() => {
                    setPreviewUrl(null);
                }}
                maxWidth="md"
            >
                <DialogContent sx={{ p: 1 }}>
                    {previewUrl && (
                        <Box
                            component="img"
                            src={previewUrl}
                            alt="Submitted file"
                            sx={{ maxWidth: "100%", display: "block" }}
                        />
                    )}
                </DialogContent>
            </Dialog>

            <StatusHero submission={submission} />

            <DetailsSection submission={submission} />

            {submission.fileUrls.length > 0 && <FilesSection fileUrls={submission.fileUrls} onPreview={setPreviewUrl} />}

            <CheckResultsSection
                isLoading={isLoading}
                error={error}
                resultInfos={resultInfos}
                onItemClick={setSelectedCheckResultId}
                isSelfCheckDisabled={isSelfCheckDisabled}
                isAiCheckDisabled={isAiCheckDisabled}
                onRequestCheck={requestCheck}
            />
        </Box>
    );
};

function StatusHero({ submission }: SubmissionComponentProps) {
    const { icon, label, paletteColor } = getStatusVisuals(submission.status);
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
                {label}
            </Typography>
            <Typography variant="caption" color="text.secondary">
                {formatDate(submission.createdAt)}
            </Typography>
        </Paper>
    );
}

function DetailsSection({ submission }: SubmissionComponentProps) {
    return (
        <Stack spacing={1}>
            <DetailRow label="Problem" value={submission.problemKey} />
            <DetailRow label="Submitted by" value={submission.userName} />
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

function FilesSection({ fileUrls, onPreview }: { fileUrls: string[]; onPreview: (url: string) => void }) {
    return (
        <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block", mb: 1 }}>
                Attached files
            </Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                {fileUrls.map((url, i) => (
                    <Chip
                        key={i}
                        icon={<AttachFileIcon />}
                        label={`File ${String(i + 1)}`}
                        size="small"
                        variant="outlined"
                        onClick={() => {
                            onPreview(url);
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
    onItemClick: (id: number) => void;
    isSelfCheckDisabled: boolean;
    isAiCheckDisabled: boolean;
    onRequestCheck: (checkType: CheckType) => void;
};

function CheckResultsSection({
    isLoading,
    error,
    resultInfos,
    onItemClick,
    isSelfCheckDisabled,
    isAiCheckDisabled,
    onRequestCheck,
}: CheckResultsSectionProps) {
    const count = resultInfos?.length ?? 0;

    return (
        <Box>
            <Typography variant="subtitle2" gutterBottom>
                Check Results{count > 0 ? ` (${String(count)})` : ""}
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
                    Failed to load check results
                </Alert>
            )}

            {!isLoading && !error && resultInfos && resultInfos.length === 0 && (
                <Stack alignItems="center" spacing={1} py={3} color="text.disabled">
                    <AssignmentLateOutlinedIcon sx={{ fontSize: 40 }} />
                    <Typography variant="body2">No check results yet</Typography>
                </Stack>
            )}

            {!isLoading && resultInfos && resultInfos.length > 0 && (
                <CheckResultInfoList data={resultInfos} onItemClick={onItemClick} />
            )}

            <Divider sx={{ my: 2 }}>
                <Typography variant="caption" color="text.secondary">
                    Actions
                </Typography>
            </Divider>

            <Stack direction="row" spacing={1}>
                <Tooltip title={isSelfCheckDisabled ? "Already solved" : "Mark this submission as solved"}>
                    <span>
                        <Button
                            size="small"
                            variant="outlined"
                            disabled={isSelfCheckDisabled}
                            onClick={() => {
                                onRequestCheck("self");
                            }}
                        >
                            Consider as Solved
                        </Button>
                    </span>
                </Tooltip>
                <Tooltip title={isAiCheckDisabled ? "Available for moderators only" : "Request an AI-powered check"}>
                    <span>
                        <Button
                            size="small"
                            variant="outlined"
                            disabled={isAiCheckDisabled}
                            onClick={() => {
                                onRequestCheck("ai");
                            }}
                        >
                            Request AI Check
                        </Button>
                    </span>
                </Tooltip>
            </Stack>
        </Box>
    );
}

function getStatusVisuals(status: SubmissionStatus): StatusVisuals {
    switch (status) {
        case "SUCCESS":
            return {
                icon: <CheckCircleOutlineIcon fontSize="inherit" />,
                label: "Solved",
                paletteColor: "success",
            };
        case "PENDING":
            return {
                icon: <HourglassEmptyIcon fontSize="inherit" />,
                label: "Pending review",
                paletteColor: "warning",
            };
        case "FAILED":
            return {
                icon: <CancelOutlinedIcon fontSize="inherit" />,
                label: "Failed",
                paletteColor: "error",
            };
        default:
            return exhaustiveGuard(status);
    }
}

function exhaustiveGuard(x: unknown): never {
    throw new Error(`Unhandled status: ${x as string}`);
}
