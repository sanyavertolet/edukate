import { FC, ReactNode } from "react";
import { Box, FormControl, InputLabel, Select, MenuItem, FormControlLabel, Checkbox, Stack } from "@mui/material";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import CloseIcon from "@mui/icons-material/CloseOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import { useAuthContext } from "@/features/auth/context";
import { ProblemStatus } from "@/features/problems/types";
import { ClearableTextField } from "@/shared/components/ClearableTextField";
import { useTranslation } from "react-i18next";

export type StatusFilter = ProblemStatus | "ALL" | undefined;
type DifficultyFilter = boolean | undefined;

type Props = {
    status: StatusFilter;
    onStatusChange: (status: StatusFilter) => void;
    prefix: string;
    onPrefixChange: (prefix: string) => void;
    isHard: DifficultyFilter;
    onIsHardChange: (value: DifficultyFilter) => void;
    hasPictures: boolean | undefined;
    onHasPicturesChange: (checked: boolean) => void;
    hasResult: boolean | undefined;
    onHasResultChange: (checked: boolean) => void;
    bookSlug: string | undefined;
    onBookSlugChange: (slug: string | undefined) => void;
    rightSlot?: ReactNode;
};

function difficultyToSelectValue(isHard: DifficultyFilter): string {
    if (isHard === true) return "HARD";
    if (isHard === false) return "MEDIUM";
    return "ANY";
}

function selectValueToDifficulty(value: string): DifficultyFilter {
    if (value === "HARD") return true;
    if (value === "MEDIUM") return false;
    return undefined;
}

export const ProblemTableToolbar: FC<Props> = ({
    status,
    onStatusChange,
    prefix,
    onPrefixChange,
    isHard,
    onIsHardChange,
    hasPictures,
    onHasPicturesChange,
    hasResult,
    onHasResultChange,
    bookSlug,
    onBookSlugChange,
    rightSlot,
}) => {
    const { isAuthorized } = useAuthContext();
    const { t } = useTranslation("problems");
    return (
        <Box sx={{ position: "relative" }}>
            {rightSlot && <Box sx={{ position: "absolute", top: 8, right: 8 }}>{rightSlot}</Box>}
            <Stack
                direction="column"
                spacing={2}
                sx={{
                    px: { xs: 1, md: 2 },
                    pt: 1,
                }}
            >
                {/* Row 1 on lg+: all four inputs. On xs/sm/md: book+code on one row, selectors on another */}
                <Stack direction={{ xs: "column", lg: "row" }} spacing={2} sx={{ alignItems: { lg: "center" } }}>
                    <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                        <ClearableTextField
                            label={t("filter_book")}
                            value={bookSlug ?? ""}
                            onChange={(v) => {
                                onBookSlugChange(v || undefined);
                            }}
                            onClear={() => {
                                onBookSlugChange(undefined);
                            }}
                            sx={{ minWidth: { md: 120 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}
                        />

                        <ClearableTextField
                            label={t("filter_problem_code")}
                            value={prefix}
                            onChange={onPrefixChange}
                            onClear={() => {
                                onPrefixChange("");
                            }}
                            sx={{ minWidth: { md: 140 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}
                        />
                    </Stack>

                    <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                        {isAuthorized && (
                            <FormControl size="small" sx={{ minWidth: { md: 160 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}>
                                <InputLabel size={"small"} id="status-filter-label">
                                    {t("filter_status")}
                                </InputLabel>
                                <Select
                                    labelId="status-filter-label"
                                    size={"small"}
                                    label={t("filter_status")}
                                    value={status ?? "ALL"}
                                    onChange={(e) => {
                                        onStatusChange((e.target.value || "ALL") as StatusFilter);
                                    }}
                                >
                                    <MenuItem value="ALL">{t("filter_status_all")}</MenuItem>
                                    <MenuItem value="SOLVED">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <DoneIcon color="success" fontSize="small" />
                                            {t("filter_status_solved")}
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="SOLVING">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <PendingIcon color="warning" fontSize="small" />
                                            {t("filter_status_solving")}
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="FAILED">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <CloseIcon color="error" fontSize="small" />
                                            {t("filter_status_failed")}
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="NOT_SOLVED">{t("filter_status_not_solved")}</MenuItem>
                                </Select>
                            </FormControl>
                        )}

                        <FormControl size="small" sx={{ minWidth: { md: 130 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}>
                            <InputLabel size={"small"} id="difficulty-filter-label">
                                {t("filter_difficulty")}
                            </InputLabel>
                            <Select
                                labelId="difficulty-filter-label"
                                size={"small"}
                                label={t("filter_difficulty")}
                                value={difficultyToSelectValue(isHard)}
                                onChange={(e) => {
                                    onIsHardChange(selectValueToDifficulty(e.target.value));
                                }}
                            >
                                <MenuItem value="ANY">{t("filter_difficulty_any")}</MenuItem>
                                <MenuItem value="HARD">{t("filter_difficulty_hard")}</MenuItem>
                                <MenuItem value="MEDIUM">{t("filter_difficulty_medium")}</MenuItem>
                            </Select>
                        </FormControl>
                    </Stack>
                </Stack>

                {/* Checkboxes — always on their own row */}
                <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={!!hasPictures}
                                onChange={(e) => {
                                    onHasPicturesChange(e.target.checked);
                                }}
                                size="small"
                            />
                        }
                        label={t("filter_with_pictures")}
                        sx={{ mr: 0 }}
                    />

                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={!!hasResult}
                                onChange={(e) => {
                                    onHasResultChange(e.target.checked);
                                }}
                                size="small"
                            />
                        }
                        label={t("filter_with_answer")}
                        sx={{ mr: 0 }}
                    />
                </Stack>
            </Stack>
        </Box>
    );
};
