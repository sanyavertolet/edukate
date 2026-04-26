import { FC } from "react";
import { Box, Chip, Divider, FormControlLabel, LinearProgress, Stack, Switch, Tooltip, Typography } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemSetUserManagement } from "./ProblemSetUserManagement";
import { useProblemSetChangeVisibilityMutation } from "@/features/problem-sets/api";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { useCopyToClipboard } from "@/shared/hooks/useCopyToClipboard";

interface ProblemSetSettingsTabProps {
    problemSet: ProblemSet;
}

export const ProblemSetSettingsTab: FC<ProblemSetSettingsTabProps> = ({ problemSet }) => {
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
                            {total} problems
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
                            {solved}/{total} solved
                        </Typography>
                    </Stack>
                </Box>

                <Divider />

                {/* Share Code + Visibility */}
                <Stack spacing={1.5}>
                    <Stack direction="row" alignItems="center" spacing={1}>
                        <Typography variant="body2" color="text.secondary">
                            Share code:
                        </Typography>
                        <Tooltip slotProps={defaultTooltipSlotProps} title="Copy share code">
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
                            Visibility:
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
                            label={<Typography variant="body2">{problemSet.isPublic ? "Public" : "Private"}</Typography>}
                            sx={{ ml: 0 }}
                        />
                    </Stack>
                    <Typography variant="caption" color="text.secondary" sx={{ pl: 0.5 }}>
                        {problemSet.isPublic
                            ? "Anyone can find and join this problem set."
                            : "Only invited users can access this problem set."}
                    </Typography>
                </Stack>

                <Divider />

                {/* Users */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        Users
                    </Typography>
                    <ProblemSetUserManagement shareCode={problemSet.shareCode} />
                </Box>
            </Stack>
        </Box>
    );
};
