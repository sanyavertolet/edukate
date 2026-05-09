import { FC } from "react";
import { Box, FormControl, InputLabel, Select, MenuItem, Stack } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import { ClearableTextField } from "@/shared/components/ClearableTextField";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { useTranslation } from "react-i18next";
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
    const { t } = useTranslation("submissions");
    return (
        <Box>
            <Stack
                direction={{ xs: "column", md: "row" }}
                spacing={2}
                sx={{ px: { xs: 1, md: 2 }, pt: 1, alignItems: { md: "center" }, flexWrap: "wrap" }}
            >
                <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                    <ClearableTextField
                        label={t("username_label")}
                        value={userName}
                        onChange={onUserNameChange}
                        onClear={() => {
                            onUserNameChange("");
                        }}
                        sx={{ minWidth: { md: 140 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}
                    />

                    <FormControl size="small" sx={{ minWidth: { md: 160 }, flex: { xs: "1 1 0", md: "0 1 auto" } }}>
                        <InputLabel size="small" id="submission-status-filter-label">
                            {t("status_label")}
                        </InputLabel>
                        <Select
                            labelId="submission-status-filter-label"
                            size="small"
                            label={t("status_label")}
                            value={status ?? "ALL"}
                            onChange={(e) => {
                                onStatusChange((e.target.value || "ALL") as StatusFilter);
                            }}
                        >
                            <MenuItem value="ALL">{t("status_all")}</MenuItem>
                            <MenuItem value="SUCCESS">
                                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                    <CheckCircleOutlineIcon color="success" fontSize="small" />
                                    {t("status_success")}
                                </Box>
                            </MenuItem>
                            <MenuItem value="PENDING">
                                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                    <HourglassEmptyIcon color="warning" fontSize="small" />
                                    {t("status_pending")}
                                </Box>
                            </MenuItem>
                            <MenuItem value="FAILED">
                                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                    <CancelOutlinedIcon color="error" fontSize="small" />
                                    {t("status_failed")}
                                </Box>
                            </MenuItem>
                        </Select>
                    </FormControl>
                </Stack>

                <Stack direction="row" spacing={2} sx={{ alignItems: "center" }}>
                    <ClearableTextField
                        label={t("book_label")}
                        value={bookSlug}
                        onChange={onBookSlugChange}
                        onClear={() => {
                            onBookSlugChange("");
                        }}
                        sx={{ minWidth: { md: 120 }, flexGrow: { xs: 1, md: 0 } }}
                    />

                    <ClearableTextField
                        label={t("problem_code_label")}
                        value={problemCode}
                        onChange={onProblemCodeChange}
                        onClear={() => {
                            onProblemCodeChange("");
                        }}
                        sx={{ minWidth: { md: 140 }, flexGrow: { xs: 1, md: 0 } }}
                    />
                </Stack>
            </Stack>
        </Box>
    );
};
