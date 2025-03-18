import { ProblemMetadata } from '../types/ProblemMetadata';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Skeleton,
    Box,
    Chip, TableFooter, TablePagination
} from '@mui/material';
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useProblemCountRequest, useProblemListRequest } from "../http/requests";
import TablePaginationActions from "@mui/material/TablePagination/TablePaginationActions";
import { ProblemStatusIcon } from "./problem/ProblemStatusIcon";

export default function ProblemListComponent() {
    const [ problemList, setProblemList ] = useState<ProblemMetadata[]>([]);
    const [ page, setPage ] = useState(0);
    const [ rowsPerPage, setRowsPerPage ] = useState(10);
    const [ problemLength, setProblemLength ] = useState(0);
    const problemListQuery = useProblemListRequest(page, rowsPerPage);
    const problemCountQuery = useProblemCountRequest();
    const navigate = useNavigate();

    const navigateToProblem = (problemName: string) => { navigate(`/problems/${problemName}`) };

    useEffect(() => {
        if (problemListQuery.data && !problemListQuery.isLoading && !problemListQuery.error) {
            setProblemList(problemListQuery.data)
        }}, [problemListQuery.data, problemListQuery.isLoading, problemListQuery.error]
    );

    useEffect(() => {
        if (problemCountQuery.data && !problemCountQuery.isLoading && !problemCountQuery.error) {
            setProblemLength(problemCountQuery.data)
        }
    }, [problemCountQuery.data, problemCountQuery.isLoading, problemCountQuery.error]);

    const tableRows = problemList?.map((item) => (
        <TableRow
            key={ item.name }
            sx={{ cursor: 'pointer', textcolor: '#ffffff' }}
            onClick={navigateToProblem.bind(null, item.name)}
            hover
        >
            <TableCell key={`${item.name}-status`}>{ <ProblemStatusIcon status={item.status}/> }</TableCell>
            <TableCell key={`${item.name}-id`}>{ item.name } { item.isHard && "*" } </TableCell>
            <TableCell key={`${item.name}-tags`}>{ item.tags.map(tag =>
                <Chip key={`${item.name}-tag-${tag}`} label={tag} size="small" sx={{ mx: 1 }} variant="outlined"/>
            )}</TableCell>
        </TableRow>
    ));

    const tableRowsPlaceholder = Array(5).fill(0).map((_, i) => (
       <TableRow key={ i }>
           <TableCell key={`${i}-status-placeholder`}>
               <Skeleton variant="rounded" />
           </TableCell>
            <TableCell key={`${i}-name-placeholder`}>
                <Skeleton variant="rounded" />
            </TableCell>
           <TableCell key={`${i}-tags-placeholder`}>
               <Skeleton variant="rounded" />
           </TableCell>
       </TableRow>
    ));

    const handleChangePage = (_: unknown, newPage: number) => {
        setPage(newPage);
    };
    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0)
    };

    return (
        <Box paddingTop="2rem">
            <TableContainer component={ Paper }>
                <Table size="medium" aria-label="problem table">
                    <TableHead key={"table-headers"}>
                        <TableRow key={"table-headers-row"}>
                            <TableCell key={"table-headers-status"}>Status</TableCell>
                            <TableCell key={"table-headers-id"}>Name</TableCell>
                            <TableCell key={"table-headers-tags"}>Tags</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody key={"table-body"}>
                        { !problemListQuery.isLoading && !problemListQuery.error
                        && !problemCountQuery.isLoading && !problemCountQuery.error
                            ?
                            tableRows :
                            tableRowsPlaceholder
                        }
                    </TableBody>
                    <TableFooter key={"table-footer"}>
                        <TableRow key={"table-footer-row"}>
                            <TablePagination
                                key={"table-pagination"}
                                rowsPerPageOptions={[10, 25, 50, 100]}
                                colSpan={3}
                                count={ problemLength }
                                rowsPerPage={ rowsPerPage }
                                page={ page }
                                slotProps={{
                                    select: {
                                        inputProps: { 'aria-label': 'rows per page' },
                                        native: true,
                                    },
                                }}
                                onPageChange={handleChangePage}
                                onRowsPerPageChange={handleChangeRowsPerPage}
                                ActionsComponent={TablePaginationActions}
                            />
                        </TableRow>
                    </TableFooter>
                </Table>
            </TableContainer>
        </Box>
    );
}
