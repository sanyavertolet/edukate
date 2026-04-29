import { FC } from "react";
import { Toolbar, Box, TextField, FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { StatusFilter } from "@/features/submissions/hooks/useSubmissionTableParams";

type Props = {
    status: StatusFilter;
    onStatusChange: (status: StatusFilter) => void;
    userName: string;
    onUserNameChange: (userName: string) => void;
    bookSlug: string;
    onBookSlugChange: (bookSlug: string) => void;
    problemCode: string;
    onProblemCodeChange: (problemCode: string) => void;
};

export const SubmissionTableToolbar: FC<Props> = ({
    status,
    onStatusChange,
    userName,
    onUserNameChange,
    bookSlug,
    onBookSlugChange,
    problemCode,
    onProblemCodeChange,
}) => {
    return (
        <Box>
            <Toolbar sx={{ display: "flex", gap: 2, justifyContent: "flex-start", flexWrap: "wrap" }}>
                <TextField
                    label="Username"
                    size="small"
                    value={userName}
                    onChange={(e) => {
                        onUserNameChange(e.target.value);
                    }}
                />

                <TextField
                    label="Book"
                    size="small"
                    value={bookSlug}
                    onChange={(e) => {
                        onBookSlugChange(e.target.value);
                    }}
                />

                <TextField
                    label="Problem code"
                    size="small"
                    value={problemCode}
                    onChange={(e) => {
                        onProblemCodeChange(e.target.value);
                    }}
                />

                <FormControl size="small" sx={{ minWidth: 160 }}>
                    <InputLabel size="small" id="submission-status-filter-label">
                        Status
                    </InputLabel>
                    <Select
                        labelId="submission-status-filter-label"
                        size="small"
                        label="Status"
                        value={status ?? "ALL"}
                        onChange={(e) => {
                            onStatusChange((e.target.value || "ALL") as StatusFilter);
                        }}
                    >
                        <MenuItem value="ALL">All</MenuItem>
                        <MenuItem value="SUCCESS">
                            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                <CheckCircleOutlineIcon color="success" fontSize="small" />
                                Success
                            </Box>
                        </MenuItem>
                        <MenuItem value="PENDING">
                            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                <HourglassEmptyIcon color="warning" fontSize="small" />
                                Pending
                            </Box>
                        </MenuItem>
                        <MenuItem value="FAILED">
                            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                <CancelOutlinedIcon color="error" fontSize="small" />
                                Failed
                            </Box>
                        </MenuItem>
                    </Select>
                </FormControl>
            </Toolbar>
        </Box>
    );
};
