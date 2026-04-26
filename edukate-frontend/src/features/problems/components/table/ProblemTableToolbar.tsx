import { FC, ReactNode } from "react";
import {
    Toolbar,
    Box,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    FormControlLabel,
    Checkbox,
    Chip,
} from "@mui/material";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import CloseIcon from "@mui/icons-material/CloseOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import { useAuthContext } from "@/features/auth/context";
import { ProblemStatus } from "@/features/problems/types";

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
        <Box>
            <Toolbar sx={{ display: "flex", gap: 2, justifyContent: "space-between", flexWrap: "wrap" }}>
                <Box sx={{ display: "flex", gap: 2, alignItems: "center", flexWrap: "wrap" }}>
                    <TextField
                        label="Search by prefix"
                        size="small"
                        value={prefix}
                        onChange={(e) => {
                            onPrefixChange(e.target.value);
                        }}
                    />

                    {isAuthorized && (
                        <FormControl size="small" sx={{ minWidth: 160 }}>
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

                    <FormControl size="small" sx={{ minWidth: 130 }}>
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

                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={!!hasPictures}
                                onChange={(e) => {
                                    onHasPicturesChange(e.target.checked);
                                }}
                            />
                        }
                        label="With pictures"
                    />

                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={!!hasResult}
                                onChange={(e) => {
                                    onHasResultChange(e.target.checked);
                                }}
                            />
                        }
                        label="With answer"
                    />
                </Box>

                <Box sx={{ marginLeft: "auto" }}>{rightSlot}</Box>
            </Toolbar>

            {bookSlug && (
                <Box sx={{ px: 3, pb: 1, display: "flex", justifyContent: "flex-start" }}>
                    <Chip
                        label={`Book: ${bookSlug}`}
                        onDelete={() => {
                            onBookSlugChange(undefined);
                        }}
                        color="primary"
                        variant="outlined"
                        size="small"
                    />
                </Box>
            )}
        </Box>
    );
};
