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
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { getProblemList } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";
import { ClearableTextField } from "@/shared/components/ClearableTextField";

const PAGE_SIZE = 20;

interface ProblemSetProblemPickerProps {
    selectedKeys: string[];
    onSelectionChange: (keys: string[]) => void;
}

export function ProblemSetProblemPicker({ selectedKeys, onSelectionChange }: ProblemSetProblemPickerProps) {
    const { t } = useTranslation("problem-sets");
    const [page, setPage] = useState(0);
    const [bookSlug, setBookSlug] = useState("");
    const [prefix, setPrefix] = useState("");

    const { data = [], isLoading } = useQuery({
        queryKey: queryKeys.problems.list(
            page,
            PAGE_SIZE,
            prefix || undefined,
            undefined,
            undefined,
            undefined,
            undefined,
            bookSlug || undefined,
        ),
        queryFn: ({ signal }) =>
            getProblemList({ page, size: PAGE_SIZE, bookSlug: bookSlug || undefined, prefix: prefix || undefined }, signal),
    });

    const toggle = (key: string) => {
        if (selectedKeys.includes(key)) {
            onSelectionChange(selectedKeys.filter((k) => k !== key));
        } else {
            onSelectionChange([...selectedKeys, key]);
        }
    };

    const handleBookSlugChange = (value: string) => {
        setBookSlug(value);
        setPage(0);
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
                        setPage(0);
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
                <List dense disablePadding sx={{ border: 1, borderColor: "divider", borderRadius: 1 }}>
                    {data.map((problem) => (
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
                            <ListItemText primary={problem.key} secondary={problem.isHard ? "★ Hard" : undefined} />
                        </ListItemButton>
                    ))}
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
