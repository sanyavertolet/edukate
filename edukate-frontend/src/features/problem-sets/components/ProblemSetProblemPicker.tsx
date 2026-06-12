import {
    Checkbox,
    Chip,
    CircularProgress,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Stack,
    TablePagination,
    TextField,
    Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getProblemList } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";
import { ClearableTextField } from "@/shared/components/ClearableTextField";
import { useDebounce } from "@/shared/hooks/useDebounce";

const PAGE_SIZE = 20;
const SEARCH_DEBOUNCE_MS = 500;

interface ProblemSetProblemPickerProps {
    selectedKeys: string[];
    onSelectionChange: (keys: string[]) => void;
    excludeKeys?: string[];
}

export function ProblemSetProblemPicker({ selectedKeys, onSelectionChange, excludeKeys }: ProblemSetProblemPickerProps) {
    const { t } = useTranslation("problem-sets");
    const [page, setPage] = useState(0);
    const [bookSlug, setBookSlug] = useState("");
    const [prefix, setPrefix] = useState("");
    const debouncedBookSlug = useDebounce(bookSlug, SEARCH_DEBOUNCE_MS);
    const debouncedPrefix = useDebounce(prefix, SEARCH_DEBOUNCE_MS);

    // Reset to page 0 when the *applied* (debounced) filter changes, not on every keystroke —
    // otherwise an old-filter page-0 refetch would fire before the new-filter one.
    useEffect(() => {
        setPage(0);
    }, [debouncedBookSlug, debouncedPrefix]);

    const { data: rawData = [], isLoading } = useQuery({
        queryKey: queryKeys.problems.list(
            page,
            PAGE_SIZE,
            debouncedPrefix || undefined,
            undefined,
            undefined,
            undefined,
            undefined,
            debouncedBookSlug || undefined,
        ),
        queryFn: ({ signal }) =>
            getProblemList(
                {
                    page,
                    size: PAGE_SIZE,
                    bookSlugPrefix: debouncedBookSlug || undefined,
                    prefix: debouncedPrefix || undefined,
                },
                signal,
            ),
    });

    const data = excludeKeys && excludeKeys.length > 0 ? rawData.filter((p) => !excludeKeys.includes(p.key)) : rawData;

    const toggle = (key: string) => {
        if (selectedKeys.includes(key)) {
            onSelectionChange(selectedKeys.filter((k) => k !== key));
        } else {
            onSelectionChange([...selectedKeys, key]);
        }
    };

    const handleBookSlugChange = (value: string) => {
        setBookSlug(value);
    };

    return (
        <Stack spacing={1}>
            {selectedKeys.length > 0 && (
                <Chip
                    label={t("picker_selected_count", { count: selectedKeys.length })}
                    size="small"
                    color="primary"
                    variant="outlined"
                    sx={{ alignSelf: "flex-start" }}
                />
            )}
            <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
                <ClearableTextField
                    label={t("book_label")}
                    value={bookSlug}
                    size="small"
                    onChange={handleBookSlugChange}
                    onClear={() => {
                        handleBookSlugChange("");
                    }}
                />
                <TextField
                    label={t("picker_search_placeholder")}
                    value={prefix}
                    size="small"
                    onChange={(e) => {
                        setPrefix(e.target.value);
                    }}
                    sx={{ flex: 1 }}
                />
            </Stack>
            {isLoading ? (
                <CircularProgress size={24} sx={{ mx: "auto" }} />
            ) : data.length === 0 ? (
                <Typography color="text.secondary" align="center" py={2}>
                    {t("picker_no_problems")}
                </Typography>
            ) : (
                <List dense disablePadding>
                    {data.map((problem) => {
                        const hasTags = problem.tags.length > 0;
                        return (
                            <ListItemButton
                                key={problem.key}
                                onClick={() => {
                                    toggle(problem.key);
                                }}
                                dense
                            >
                                <ListItemIcon sx={{ minWidth: 36 }}>
                                    <Checkbox
                                        checked={selectedKeys.includes(problem.key)}
                                        edge="start"
                                        tabIndex={-1}
                                        disableRipple
                                        size="small"
                                    />
                                </ListItemIcon>
                                <ListItemText
                                    primary={
                                        <>
                                            {problem.key}
                                            {problem.isHard && (
                                                <Typography
                                                    component="span"
                                                    variant="caption"
                                                    color="warning.main"
                                                    sx={{ ml: 1 }}
                                                >
                                                    ★ {t("hard_label")}
                                                </Typography>
                                            )}
                                        </>
                                    }
                                    slotProps={{ secondary: { component: "div" } }}
                                    secondary={
                                        hasTags ? (
                                            <Stack
                                                direction="row"
                                                spacing={0.5}
                                                sx={{
                                                    flexWrap: "wrap",
                                                    rowGap: 0.5,
                                                    alignItems: "center",
                                                    mt: 0.25,
                                                }}
                                            >
                                                {problem.tags.slice(0, 3).map((tag) => (
                                                    <Chip key={tag} label={tag} size="small" variant="outlined" />
                                                ))}
                                                {problem.tags.length > 3 && (
                                                    <Chip
                                                        label={`+${String(problem.tags.length - 3)}`}
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                )}
                                            </Stack>
                                        ) : undefined
                                    }
                                />
                            </ListItemButton>
                        );
                    })}
                </List>
            )}
            <TablePagination
                component="div"
                count={data.length < PAGE_SIZE ? page * PAGE_SIZE + data.length : -1}
                page={page}
                onPageChange={(_, newPage) => {
                    setPage(newPage);
                }}
                rowsPerPage={PAGE_SIZE}
                rowsPerPageOptions={[PAGE_SIZE]}
            />
        </Stack>
    );
}
