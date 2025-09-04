import { FC, ReactNode } from 'react';
import { Paper, Table, TableBody, TableContainer, TableHead, TableRow, TableCell } from '@mui/material';

type ProblemTableProps = {
    headerCells: ReactNode[];
    toolbar?: ReactNode;
    children: ReactNode;
    footer?: ReactNode;
};

export const ProblemTable: FC<ProblemTableProps> = ({ headerCells, toolbar, children, footer }) => {
    const headerTableCellSx = { minWidth: 44, maxWidth: 44, width: 44, p: 0.5 };
    return (
        <TableContainer component={Paper}>
            {toolbar}
            <Table aria-label="problem table">
                <TableHead>
                    <TableRow>
                        {headerCells.map((h, i) => (
                            <TableCell
                                key={`header-${i}`} align={i === 0 ? 'center' : undefined}
                                sx={i === 0 ? headerTableCellSx : undefined}
                            >
                                {h}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {children}
                </TableBody>
                {footer}
            </Table>
        </TableContainer>
    );
};
