import { ChangeEvent, FC } from "react";
import { TableFooter, TablePagination, TableRow } from "@mui/material";
import { ProblemTablePaginationActions } from "./ProblemTablePaginationActions";

type Props = {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (e: unknown, newPage: number) => void;
    onRowsPerPageChange: (e: ChangeEvent<HTMLInputElement>) => void;
    colSpan?: number;
    rowsPerPageOptions?: number[];
};

export const ProblemTablePagination: FC<Props> = ({
    count,
    page,
    rowsPerPage,
    onPageChange,
    onRowsPerPageChange,
    colSpan = 3,
    rowsPerPageOptions = [10, 25, 50, 100],
}) => {
    return (
        <TableFooter>
            <TableRow>
                <TablePagination
                    rowsPerPageOptions={rowsPerPageOptions}
                    colSpan={colSpan}
                    count={count}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    slotProps={{ select: { inputProps: { "aria-label": "rows per page" }, native: true } }}
                    onPageChange={onPageChange}
                    onRowsPerPageChange={onRowsPerPageChange}
                    ActionsComponent={ProblemTablePaginationActions}
                    labelDisplayedRows={({ from, to, count }) =>
                        `${String(from)}–${String(to)} of ${String(count)} problems`
                    }
                    sx={{
                        "& .MuiTablePagination-spacer": { flex: 0 },
                        "& .MuiTablePagination-toolbar": { position: "relative" },
                        "& .MuiTablePagination-displayedRows": { marginLeft: "auto", mr: 2 },
                    }}
                />
            </TableRow>
        </TableFooter>
    );
};
