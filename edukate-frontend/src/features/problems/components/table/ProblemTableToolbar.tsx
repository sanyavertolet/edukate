import { FC, ReactNode } from "react";
import { Box, FormControl, InputLabel, Select, MenuItem, FormControlLabel, Checkbox, Stack } from "@mui/material";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import CloseIcon from "@mui/icons-material/CloseOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import { useAuthContext } from "@/features/auth/context";
import { ProblemStatus } from "@/features/problems/types";
import { ClearableTextField } from "@/shared/components/ClearableTextField";

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
                            label="Book"
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
                            label="Problem code"
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
                                    Status
                                </InputLabel>
                                <Select
                                    labelId="status-filter-label"
                                    size={"small"}
                                    label="Status"
                                    value={status ?? "ALL"}
                                    onChange={(e) => {
                                        onStatusChange((e.target.value || "ALL") as StatusFilter);
                                    }}
                                >
                                    <MenuItem value="ALL">All</MenuItem>
                                    <MenuItem value="SOLVED">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <DoneIcon color="success" fontSize="small" />
                                            Solved
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="SOLVING">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <PendingIcon color="warning" fontSize="small" />
                                            Solving
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="FAILED">
                                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                            <CloseIcon color="error" fontSize="small" />
                                            Failed
                                        </Box>
                                    </MenuItem>
                                    <MenuItem value="NOT_SOLVED">Not solved</MenuItem>
                                </Select>
                            </FormControl>
                        )}

                        <FormControl size="small" sx={{ minWidth: { md: 130 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}>
                            <InputLabel size={"small"} id="difficulty-filter-label">
                                Difficulty
                            </InputLabel>
                            <Select
                                labelId="difficulty-filter-label"
                                size={"small"}
                                label="Difficulty"
                                value={difficultyToSelectValue(isHard)}
                                onChange={(e) => {
                                    onIsHardChange(selectValueToDifficulty(e.target.value));
                                }}
                            >
                                <MenuItem value="ANY">Any</MenuItem>
                                <MenuItem value="HARD">Hard</MenuItem>
                                <MenuItem value="MEDIUM">Medium</MenuItem>
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
                        label="With pictures"
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
                        label="With answer"
                        sx={{ mr: 0 }}
                    />
                </Stack>
            </Stack>
        </Box>
    );
};
