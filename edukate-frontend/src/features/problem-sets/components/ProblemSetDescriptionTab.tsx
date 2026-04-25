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

interface ProblemSetDescriptionTabProps {
    problemSet: ProblemSet;
}

type StatusCounts = Record<ProblemMetadataStatus, number>;

export const ProblemSetDescriptionTab: FC<ProblemSetDescriptionTabProps> = ({ problemSet }) => {
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
                        Progress
                    </Typography>
                    <LinearProgress
                        variant="determinate"
                        value={percentage}
                        color={percentage === 100 ? "success" : "primary"}
                        sx={{ height: 8, borderRadius: 4, mt: 0.5 }}
                    />
                    <Typography variant="body2" color="text.secondary" align="center" sx={{ mt: 0.5 }}>
                        {solved} of {total} problems solved ({Math.round(percentage)}%)
                    </Typography>
                </Box>

                {/* Status Breakdown */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        Status Breakdown
                    </Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap justifyContent="center" sx={{ mt: 0.5 }}>
                        <Chip
                            size="small"
                            icon={<DoneOutlinedIcon />}
                            label={`${String(statusCounts.SOLVED)} Solved`}
                            color="success"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<CloseOutlinedIcon />}
                            label={`${String(statusCounts.FAILED)} Failed`}
                            color="error"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<PendingOutlinedIcon />}
                            label={`${String(statusCounts.SOLVING)} Pending`}
                            color="warning"
                            variant="outlined"
                        />
                        <Chip
                            size="small"
                            icon={<RadioButtonUncheckedIcon />}
                            label={`${String(statusCounts.NOT_SOLVED)} Todo`}
                            variant="outlined"
                        />
                    </Stack>
                </Box>

                <Divider />

                {/* Details */}
                <Box>
                    <Typography variant="overline" color="text.secondary">
                        Details
                    </Typography>
                    <Stack spacing={1.5} sx={{ mt: 0.5 }}>
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
                            <PublicityIcon isPublic={problemSet.isPublic} />
                            <Typography variant="body2">{problemSet.isPublic ? "Public" : "Private"}</Typography>
                        </Stack>

                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Typography variant="body2" color="text.secondary">
                                Admins:
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
