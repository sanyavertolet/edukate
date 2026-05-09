import { FC } from "react";
import { Box, Select, TableCell, TableFooter, TableRow, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { ProblemTablePaginationActions } from "./ProblemTablePaginationActions";

type Props = {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (e: unknown, newPage: number) => void;
    onRowsPerPageChange: (value: number) => void;
    colSpan?: number;
    rowsPerPageOptions?: number[];
};

export const ProblemTablePagination: FC<Props> = ({
    count,
    page,
    rowsPerPage,
    onPageChange,
    onRowsPerPageChange,
    colSpan = 5,
    rowsPerPageOptions = [10, 25, 50, 100],
}) => {
    const { t } = useTranslation(["common", "problems"]);
    const from = count === 0 ? 0 : page * rowsPerPage + 1;
    const to = Math.min(count, (page + 1) * rowsPerPage);

    return (
        <TableFooter>
            <TableRow>
                <TableCell colSpan={colSpan} sx={{ py: 1 }}>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <Box sx={{ flex: 1, display: "flex", alignItems: "center", gap: 1 }}>
                            <Typography variant="body2" color="text.secondary" sx={{ display: { xs: "none", md: "block" } }}>
                                {t("rows_per_page", { ns: "common" })}
                            </Typography>
                            <Select
                                native
                                size="small"
                                value={rowsPerPage}
                                onChange={(e) => {
                                    onRowsPerPageChange(Number(e.target.value));
                                }}
                                inputProps={{ "aria-label": "rows per page" }}
                            >
                                {rowsPerPageOptions.map((opt) => (
                                    <option key={opt} value={opt}>
                                        {opt}
                                    </option>
                                ))}
                            </Select>
                        </Box>

                        <ProblemTablePaginationActions
                            count={count}
                            page={page}
                            rowsPerPage={rowsPerPage}
                            onPageChange={onPageChange}
                        />

                        <Box sx={{ flex: 1, display: "flex", justifyContent: "flex-end" }}>
                            <Typography variant="body2" color="text.secondary">
                                {from}–{to} of {count}
                                <Box component="span" sx={{ display: { xs: "none", md: "inline" } }}>
                                    {" "}
                                    {t("problems_label", { ns: "problems" })}
                                </Box>
                            </Typography>
                        </Box>
                    </Box>
                </TableCell>
            </TableRow>
        </TableFooter>
    );
};
