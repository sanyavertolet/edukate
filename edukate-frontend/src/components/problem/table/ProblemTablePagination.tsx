import { ChangeEvent, FC } from 'react';
import { TableFooter, TablePagination, TableRow } from '@mui/material';
import TablePaginationActions from '@mui/material/TablePagination/TablePaginationActions';

type Props = {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (e: unknown, newPage: number) => void;
    onRowsPerPageChange: (e: ChangeEvent<HTMLInputElement>) => void;
    colSpan?: number;
    rowsPerPageOptions?: number[];
};

export const ProblemTablePagination: FC<Props> = (
    {
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
                    slotProps={{ select: { inputProps: { 'aria-label': 'rows per page' }, native: true } }}
                    onPageChange={onPageChange}
                    onRowsPerPageChange={onRowsPerPageChange}
                    ActionsComponent={TablePaginationActions}
                />
            </TableRow>
        </TableFooter>
    );
};
