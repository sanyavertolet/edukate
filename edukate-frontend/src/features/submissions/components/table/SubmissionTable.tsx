import { FC, ReactNode } from "react";
import { Paper, Table, TableBody, TableContainer, TableHead, TableRow, TableCell } from "@mui/material";

type SubmissionTableProps = {
    headerCells: ReactNode[];
    toolbar?: ReactNode;
    children: ReactNode;
    footer?: ReactNode;
};

export const SubmissionTable: FC<SubmissionTableProps> = ({ headerCells, toolbar, children, footer }) => {
    return (
        <TableContainer component={Paper} sx={{ borderRadius: 3, overflow: "hidden" }}>
            {toolbar}
            <Table aria-label="submissions table">
                <TableHead>
                    <TableRow>
                        {headerCells.map((h, i) => (
                            <TableCell
                                key={`header-${String(i)}`}
                                align={i === 0 ? "center" : undefined}
                                sx={i === 0 ? { minWidth: 44, maxWidth: 44, width: 44, p: 0.5 } : undefined}
                            >
                                {h}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>{children}</TableBody>
                {footer}
            </Table>
        </TableContainer>
    );
};
