import { FC, useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
    Box,
    Dialog,
    DialogContent,
    DialogTitle,
    Divider,
    IconButton,
    LinearProgress,
    Stack,
    Typography,
    useMediaQuery,
    useTheme,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import {
    closestCenter,
    DndContext,
    DragEndEvent,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
} from "@dnd-kit/core";
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from "@dnd-kit/sortable";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";
import { ProblemSet } from "@/features/problem-sets/types";
import { updateSettings } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";
import { SortableProblemRow } from "./SortableProblemRow";
import { ProblemSetProblemPicker } from "./ProblemSetProblemPicker";

const REORDER_DEBOUNCE_MS = 300;

type ActionKind = "reorder" | "add" | "remove";

interface ProblemSetProblemsEditorDialogProps {
    problemSet: ProblemSet;
    open: boolean;
    onClose: () => void;
}

export const ProblemSetProblemsEditorDialog: FC<ProblemSetProblemsEditorDialogProps> = ({ problemSet, open, onClose }) => {
    const { t } = useTranslation("problem-sets");
    const theme = useTheme();
    const fullScreen = useMediaQuery(theme.breakpoints.down("sm"));
    const queryClient = useQueryClient();
    const problems = problemSet.problems;
    const shareCode = problemSet.shareCode;
    const detailQueryKey = useMemo(() => queryKeys.problemSets.detail(shareCode), [shareCode]);

    const mutation = useMutation({
        mutationFn: ({ keys }: { keys: string[]; kind: ActionKind }) => updateSettings(shareCode, { problemKeys: keys }),
        onMutate: async ({ keys, kind }) => {
            await queryClient.cancelQueries({ queryKey: detailQueryKey });
            const previous = queryClient.getQueryData<ProblemSet>(detailQueryKey);
            if (previous && (kind === "reorder" || kind === "remove")) {
                const byKey = new Map(previous.problems.map((p) => [p.key, p]));
                const reordered = keys.map((k) => byKey.get(k)).filter((p): p is NonNullable<typeof p> => !!p);
                queryClient.setQueryData<ProblemSet>(detailQueryKey, { ...previous, problems: reordered });
            }
            return { previous, kind };
        },
        onError: (_err, _vars, ctx) => {
            if (ctx?.previous) queryClient.setQueryData(detailQueryKey, ctx.previous);
            const messageKey =
                ctx?.kind === "reorder"
                    ? "reorder_failed_toast"
                    : ctx?.kind === "add"
                      ? "add_failed_toast"
                      : "remove_failed_toast";
            toast.error(t(messageKey));
        },
        onSettled: () => {
            void queryClient.invalidateQueries({ queryKey: detailQueryKey });
        },
    });

    const reorderTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const pendingReorderRef = useRef<string[] | null>(null);
    // Tracks the gap between drag-end and the debounced PATCH actually firing — without
    // this, the user gets no feedback for 300ms after releasing the drag.
    const [isReorderPending, setIsReorderPending] = useState(false);

    const flushReorder = useCallback(() => {
        if (reorderTimerRef.current) {
            clearTimeout(reorderTimerRef.current);
            reorderTimerRef.current = null;
        }
        if (pendingReorderRef.current) {
            const keys = pendingReorderRef.current;
            pendingReorderRef.current = null;
            setIsReorderPending(false);
            mutation.mutate({ keys, kind: "reorder" });
        } else {
            setIsReorderPending(false);
        }
    }, [mutation]);

    useEffect(
        () => () => {
            flushReorder();
        },
        [flushReorder],
    );

    const handleDragEnd = (event: DragEndEvent) => {
        const { active, over } = event;
        if (!over || active.id === over.id) return;
        const oldIndex = problems.findIndex((p) => p.key === active.id);
        const newIndex = problems.findIndex((p) => p.key === over.id);
        if (oldIndex === -1 || newIndex === -1) return;
        const reordered = arrayMove(problems, oldIndex, newIndex);
        const newKeys = reordered.map((p) => p.key);

        // Optimistic cache write for instant feedback
        const previous = queryClient.getQueryData<ProblemSet>(detailQueryKey);
        if (previous) {
            queryClient.setQueryData<ProblemSet>(detailQueryKey, { ...previous, problems: reordered });
        }

        // Debounce the network PATCH
        pendingReorderRef.current = newKeys;
        setIsReorderPending(true);
        if (reorderTimerRef.current) clearTimeout(reorderTimerRef.current);
        reorderTimerRef.current = setTimeout(() => {
            flushReorder();
        }, REORDER_DEBOUNCE_MS);
    };

    const handleRemove = (key: string) => {
        flushReorder();
        const newKeys = problems.filter((p) => p.key !== key).map((p) => p.key);
        if (newKeys.length === 0) {
            // Backend rejects an empty list (@AssertTrue). Surface a friendly toast and abort.
            toast.error(t("remove_failed_toast"));
            return;
        }
        mutation.mutate({ keys: newKeys, kind: "remove" });
    };

    const handlePickerChange = (picked: string[]) => {
        if (picked.length === 0) return;
        flushReorder();
        const newKeys = [...problems.map((p) => p.key), ...picked];
        mutation.mutate({ keys: newKeys, kind: "add" });
    };

    const sensors = useSensors(
        useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
        useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
    );

    const currentKeys = useMemo(() => problems.map((p) => p.key), [problems]);

    const handleClose = () => {
        flushReorder();
        onClose();
    };

    const isSaving = isReorderPending || mutation.isPending;

    return (
        <Dialog open={open} onClose={handleClose} fullScreen={fullScreen} fullWidth maxWidth="md">
            <DialogTitle sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", pr: 1 }}>
                <span>{t("manage_problems_title")}</span>
                <IconButton aria-label="close" onClick={handleClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>
            {/* Reserve 4px so the indicator never causes a vertical layout shift */}
            <Box sx={{ height: 4 }}>
                {isSaving && <LinearProgress aria-label={t("saving_indicator_label")} role="progressbar" />}
            </Box>
            <DialogContent dividers>
                <Stack spacing={2}>
                    <Box>
                        <Typography variant="overline" color="text.secondary">
                            {t("selected_problems_header", { count: problems.length })}
                        </Typography>
                        {problems.length === 0 ? (
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                {t("no_problems_in_set")}
                            </Typography>
                        ) : (
                            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                                <SortableContext items={currentKeys} strategy={verticalListSortingStrategy}>
                                    <Stack spacing={1} sx={{ mt: 1 }}>
                                        {problems.map((problem) => (
                                            <SortableProblemRow
                                                key={problem.key}
                                                problem={problem}
                                                onRemove={handleRemove}
                                                disabled={mutation.isPending}
                                            />
                                        ))}
                                    </Stack>
                                </SortableContext>
                            </DndContext>
                        )}
                    </Box>

                    <Divider />

                    <Box>
                        <Typography variant="overline" color="text.secondary">
                            {t("add_problems_header")}
                        </Typography>
                        <Box sx={{ mt: 1 }}>
                            <ProblemSetProblemPicker
                                selectedKeys={[]}
                                onSelectionChange={handlePickerChange}
                                excludeKeys={currentKeys}
                            />
                        </Box>
                    </Box>
                </Stack>
            </DialogContent>
        </Dialog>
    );
};
