import { ProblemSetMetadata } from "@/features/problem-sets/types";
import { FC } from "react";
import { Box, LinearProgress, ListItemButton, Stack, Typography } from "@mui/material";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { useNavigate } from "react-router-dom";
import { PublicityIcon } from "./PublicityIcon";
import { useAuthContext } from "@/features/auth/context";

interface ProblemSetListItemProps {
    problemSetMetadata: ProblemSetMetadata;
}

export const ProblemSetListItem: FC<ProblemSetListItemProps> = ({ problemSetMetadata }) => {
    const navigate = useNavigate();
    const { isAuthorized } = useAuthContext();

    const handleClick = () => {
        void navigate(`/problem-sets/${problemSetMetadata.shareCode}`);
    };

    const percentage = problemSetMetadata.size > 0 ? (problemSetMetadata.solvedCount / problemSetMetadata.size) * 100 : 0;
    const isComplete = problemSetMetadata.solvedCount === problemSetMetadata.size && problemSetMetadata.size > 0;

    const adminLabel =
        problemSetMetadata.admins.length > 1
            ? `by ${problemSetMetadata.admins[0]} (+${String(problemSetMetadata.admins.length - 1)})`
            : problemSetMetadata.admins.length === 1
              ? `by ${problemSetMetadata.admins[0]}`
              : undefined;

    return (
        <ListItemButton onClick={handleClick} sx={{ py: 1.5, px: 2 }}>
            <Box sx={{ display: "flex", flexDirection: "column", gap: 0.5, width: "100%" }}>
                {/* Row 1: Icon + Name + Progress + Arrow */}
                <Stack direction="row" alignItems="center" spacing={1.5}>
                    <PublicityIcon isPublic={problemSetMetadata.isPublic} disableTooltip />

                    <Typography variant="subtitle1" fontWeight={600} noWrap sx={{ flexShrink: 1, minWidth: 0 }}>
                        {problemSetMetadata.name}
                    </Typography>

                    <Box sx={{ flexGrow: 1 }} />

                    {isAuthorized && problemSetMetadata.solvedCount > 0 ? (
                        <Stack direction="row" alignItems="center" spacing={1} sx={{ flexShrink: 0 }}>
                            <LinearProgress
                                variant="determinate"
                                value={percentage}
                                color={isComplete ? "success" : "primary"}
                                sx={{ width: { xs: 60, sm: 100, md: 120 }, height: 6, borderRadius: 3 }}
                            />
                            <Typography variant="caption" color="text.secondary" noWrap>
                                {problemSetMetadata.solvedCount}/{problemSetMetadata.size} solved
                            </Typography>
                        </Stack>
                    ) : (
                        <Typography variant="caption" color="text.secondary" noWrap sx={{ flexShrink: 0 }}>
                            {problemSetMetadata.size} problems
                        </Typography>
                    )}

                    <ChevronRightIcon color="action" sx={{ flexShrink: 0 }} />
                </Stack>

                {/* Row 2: Description + Admin label */}
                <Stack direction="row" alignItems="center" spacing={1} sx={{ pl: 4.5 }}>
                    <Typography variant="body2" color="text.secondary" noWrap sx={{ flexGrow: 1, minWidth: 0 }}>
                        {problemSetMetadata.description || "No description"}
                    </Typography>

                    {adminLabel && (
                        <Typography variant="caption" color="text.secondary" noWrap sx={{ flexShrink: 0 }}>
                            {adminLabel}
                        </Typography>
                    )}
                </Stack>
            </Box>
        </ListItemButton>
    );
};
