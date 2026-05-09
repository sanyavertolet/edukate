import { ProblemSet } from "@/features/problem-sets/types";
import { FC, useMemo } from "react";
import { AvatarGroup, Box, Chip, Divider, LinearProgress, Stack, Tooltip, Typography } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import DoneOutlinedIcon from "@mui/icons-material/DoneOutlined";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";
import PendingOutlinedIcon from "@mui/icons-material/PendingOutlined";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import { PublicityIcon } from "./PublicityIcon";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { UserAvatar } from "@/shared/components/UserAvatar";
import { useCopyToClipboard } from "@/shared/hooks/useCopyToClipboard";
import { ProblemMetadataStatus } from "@/features/problems/types";
import { useTranslation } from "react-i18next";

interface ProblemSetDescriptionTabProps {
    problemSet: ProblemSet;
}

type StatusCounts = Record<ProblemMetadataStatus, number>;

export const ProblemSetDescriptionTab: FC<ProblemSetDescriptionTabProps> = ({ problemSet }) => {
    const { t } = useTranslation("problem-sets");
    const statusCounts = useMemo<StatusCounts>(
        () =>
            problemSet.problems.reduce<StatusCounts>(
                (acc, p) => {
                    acc[p.status] = (acc[p.status] || 0) + 1;
                    return acc;
                },
                { SOLVED: 0, FAILED: 0, SOLVING: 0, NOT_SOLVED: 0 },
            ),
        [problemSet.problems],
    );

    const total = problemSet.problems.length;
    const solved = statusCounts.SOLVED;
    const percentage = total > 0 ? (solved / total) * 100 : 0;

    const copyToClipboard = useCopyToClipboard();

    return (
        <Box sx={{ p: 3 }}>
            <Stack spacing={3}>
                {/* Description */}
                {problemSet.description && (
                    <Typography variant="body1" color="text.secondary">
                        {problemSet.description}
                    </Typography>
                )}

                {/* Progress */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("progress_label")}
                    </Typography>
                    <LinearProgress
                        variant="determinate"
                        value={percentage}
                        color={percentage === 100 ? "success" : "primary"}
                        sx={{ height: 8, borderRadius: 4, mt: 0.5 }}
                    />
                    <Typography variant="body2" color="text.secondary" align="center" sx={{ mt: 0.5 }}>
                        {t("progress_text", { solved, total, percentage: Math.round(percentage) })}
                    </Typography>
                </Box>

                {/* Status Breakdown */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("status_breakdown_label")}
                    </Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap justifyContent="center" sx={{ mt: 0.5 }}>
                        <Chip
                            size="small"
                            icon={<DoneOutlinedIcon />}
                            label={t("status_solved", { count: statusCounts.SOLVED })}
                            color="success"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<CloseOutlinedIcon />}
                            label={t("status_failed", { count: statusCounts.FAILED })}
                            color="error"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<PendingOutlinedIcon />}
                            label={t("status_pending", { count: statusCounts.SOLVING })}
                            color="warning"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<RadioButtonUncheckedIcon />}
                            label={t("status_todo", { count: statusCounts.NOT_SOLVED })}
                            variant="outlined"
                        />
                    </Stack>
                </Box>

                <Divider />

                {/* Details */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("details_label")}
                    </Typography>
                    <Stack spacing={1.5} sx={{ mt: 0.5 }}>
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Typography variant="body2" color="text.secondary">
                                {t("share_code_label")}
                            </Typography>
                            <Tooltip slotProps={defaultTooltipSlotProps} title={t("copy_share_code_tooltip")}>
                                <Chip
                                    size="small"
                                    icon={<ContentCopyIcon fontSize="small" />}
                                    label={problemSet.shareCode}
                                    onClick={() => {
                                        copyToClipboard(problemSet.shareCode);
                                    }}
                                    variant="outlined"
                                />
                            </Tooltip>
                        </Stack>

                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Typography variant="body2" color="text.secondary">
                                {t("visibility_label")}
                            </Typography>
                            <PublicityIcon isPublic={problemSet.isPublic} />
                            <Typography variant="body2">
                                {problemSet.isPublic ? t("visibility_public") : t("visibility_private")}
                            </Typography>
                        </Stack>

                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Typography variant="body2" color="text.secondary">
                                {t("admins_label")}
                            </Typography>
                            <AvatarGroup max={4} sx={{ "& .MuiAvatar-root": { width: 28, height: 28, fontSize: 12 } }}>
                                {problemSet.admins.map((admin) => (
                                    <Tooltip slotProps={defaultTooltipSlotProps} key={admin} title={admin}>
                                        <UserAvatar name={admin} size="small" />
                                    </Tooltip>
                                ))}
                            </AvatarGroup>
                        </Stack>
                    </Stack>
                </Box>
            </Stack>
        </Box>
    );
};
