import { FC } from "react";
import { Box, Chip, Divider, FormControlLabel, LinearProgress, Stack, Switch, Tooltip, Typography } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemSetUserManagement } from "./ProblemSetUserManagement";
import { useProblemSetChangeVisibilityMutation } from "@/features/problem-sets/api";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { useCopyToClipboard } from "@/shared/hooks/useCopyToClipboard";
import { useTranslation } from "react-i18next";

interface ProblemSetSettingsTabProps {
    problemSet: ProblemSet;
}

export const ProblemSetSettingsTab: FC<ProblemSetSettingsTabProps> = ({ problemSet }) => {
    const { t } = useTranslation("problem-sets");
    const visibilityMutation = useProblemSetChangeVisibilityMutation();

    const total = problemSet.problems.length;
    const solved = problemSet.problems.filter((p) => p.status === "SOLVED").length;
    const percentage = total > 0 ? (solved / total) * 100 : 0;

    const handleVisibilityToggle = () => {
        visibilityMutation.mutate({ shareCode: problemSet.shareCode, isPublic: !problemSet.isPublic });
    };

    const copyToClipboard = useCopyToClipboard();

    return (
        <Box sx={{ p: 3 }}>
            <Stack spacing={2}>
                {/* Summary Header */}
                <Box>
                    <Stack direction="row" justifyContent="space-between" alignItems="baseline">
                        <Typography variant="h6" color="primary">
                            {problemSet.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {t("problems_count", { count: total })}
                        </Typography>
                    </Stack>
                    <Stack direction="row" alignItems="center" spacing={1.5} sx={{ mt: 1 }}>
                        <LinearProgress
                            variant="determinate"
                            value={percentage}
                            color={percentage === 100 ? "success" : "primary"}
                            sx={{ flexGrow: 1 }}
                        />
                        <Typography variant="caption" color="text.secondary" noWrap>
                            {t("solved_count", { solved, total })}
                        </Typography>
                    </Stack>
                </Box>

                <Divider />

                {/* Share Code + Visibility */}
                <Stack spacing={1.5}>
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
                        <FormControlLabel
                            control={
                                <Switch
                                    size="small"
                                    checked={problemSet.isPublic}
                                    onChange={handleVisibilityToggle}
                                    disabled={visibilityMutation.isPending}
                                />
                            }
                            label={
                                <Typography variant="body2">
                                    {problemSet.isPublic ? t("visibility_public") : t("visibility_private")}
                                </Typography>
                            }
                            sx={{ ml: 0 }}
                        />
                    </Stack>
                    <Typography variant="caption" color="text.secondary" sx={{ pl: 0.5 }}>
                        {problemSet.isPublic ? t("visibility_public_description") : t("visibility_private_description")}
                    </Typography>
                </Stack>

                <Divider />

                {/* Users */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        {t("users_label")}
                    </Typography>
                    <ProblemSetUserManagement shareCode={problemSet.shareCode} />
                </Box>
            </Stack>
        </Box>
    );
};
