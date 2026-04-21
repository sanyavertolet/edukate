import { FC } from "react";
import { Box, Select, TableCell, TableFooter, TableRow, Typography } from "@mui/material";
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
    colSpan = 4,
    rowsPerPageOptions = [10, 25, 50, 100],
}) => {
    const from = count === 0 ? 0 : page * rowsPerPage + 1;
    const to = Math.min(count, (page + 1) * rowsPerPage);

    return (
        <TableFooter>
            <TableRow>
                <TableCell colSpan={colSpan} sx={{ py: 1 }}>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <Box sx={{ flex: 1, display: "flex", alignItems: "center", gap: 1 }}>
                            <Typography variant="body2" color="text.secondary">
                                Rows per page:
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
                                {from}–{to} of {count} problems
                            </Typography>
                        </Box>
                    </Box>
                </TableCell>
            </TableRow>
        </TableFooter>
    );
};
