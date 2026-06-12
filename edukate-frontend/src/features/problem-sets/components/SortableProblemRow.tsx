import { FC } from "react";
import { Box, Chip, IconButton, Paper, Stack, Tooltip, Typography } from "@mui/material";
import DragIndicatorIcon from "@mui/icons-material/DragIndicator";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import StarIcon from "@mui/icons-material/Star";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { useTranslation } from "react-i18next";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "./ProblemStatusIcon";

interface SortableProblemRowProps {
    problem: ProblemMetadata;
    onRemove: (key: string) => void;
    disabled?: boolean;
}

export const SortableProblemRow: FC<SortableProblemRowProps> = ({ problem, onRemove, disabled = false }) => {
    const { t } = useTranslation("problem-sets");
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
        id: problem.key,
        disabled,
    });

    const style: React.CSSProperties = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
        zIndex: isDragging ? 1 : 0,
    };

    return (
        <Paper
            ref={setNodeRef}
            style={style}
            variant="outlined"
            sx={{
                px: 1,
                py: 0.75,
                display: "flex",
                alignItems: "center",
                gap: 1,
                bgcolor: isDragging ? "action.hover" : "background.paper",
            }}
        >
            <Tooltip title={t("drag_to_reorder")} placement="left">
                <Box
                    {...attributes}
                    {...listeners}
                    aria-label={t("drag_to_reorder")}
                    sx={{
                        display: "flex",
                        alignItems: "center",
                        cursor: disabled ? "default" : isDragging ? "grabbing" : "grab",
                        color: "text.secondary",
                        touchAction: "none",
                        "&:focus-visible": { outline: "2px solid", outlineColor: "primary.main", outlineOffset: 2 },
                    }}
                >
                    <DragIndicatorIcon fontSize="small" />
                </Box>
            </Tooltip>

            <ProblemStatusIcon status={problem.status} fontSize="small" />

            <Typography variant="body2" sx={{ fontFamily: "monospace", flexShrink: 0 }}>
                {problem.key}
            </Typography>

            {problem.isHard && (
                <Tooltip title="Hard">
                    <StarIcon fontSize="inherit" color="warning" sx={{ flexShrink: 0 }} />
                </Tooltip>
            )}

            <Stack direction="row" spacing={0.5} sx={{ flexGrow: 1, minWidth: 0, flexWrap: "wrap", overflow: "hidden" }}>
                {problem.tags.slice(0, 3).map((tag) => (
                    <Chip key={tag} label={tag} size="small" variant="outlined" />
                ))}
                {problem.tags.length > 3 && (
                    <Chip label={`+${String(problem.tags.length - 3)}`} size="small" variant="outlined" />
                )}
            </Stack>

            <Tooltip title={t("remove_problem")}>
                <span>
                    <IconButton
                        size="small"
                        edge="end"
                        aria-label={t("remove_problem")}
                        onClick={() => {
                            onRemove(problem.key);
                        }}
                        disabled={disabled}
                    >
                        <DeleteOutlineIcon fontSize="small" />
                    </IconButton>
                </span>
            </Tooltip>
        </Paper>
    );
};
