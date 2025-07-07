import { ProblemMetadata } from '../../types/problem/ProblemMetadata';
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
    TableFooter,
    TablePagination,
    Stack
} from '@mui/material';
import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useProblemCountRequest, useProblemListRequest } from "../../http/requests";
import TablePaginationActions from "@mui/material/TablePagination/TablePaginationActions";
import { ProblemStatusIcon } from "./ProblemStatusIcon";
import { TagChip } from "../basic/TagChip";

function getSearchParamAsInt(searchParams: URLSearchParams, key: string, defaultValue: number) {
    return searchParams.get(key) ? parseInt(searchParams.get(key)!) : defaultValue;
}

const DEFAULT_PAGE_SIZE = 10;

export default function ProblemListComponent() {
    const navigate = useNavigate();
    const navigateToProblem = (problemName: string) => { navigate(`/problems/${problemName}`) };

    const [ searchParams, setSearchParams ] = useSearchParams()
    const [ page, setPage ] = useState(getSearchParamAsInt(searchParams, 'page', 0));
    const [ rowsPerPage, setRowsPerPage ] = useState(getSearchParamAsInt(searchParams, 'pageSize', DEFAULT_PAGE_SIZE));

    const [ problemList, setProblemList ] = useState<ProblemMetadata[]>([]);
    const problemListQuery = useProblemListRequest(page, rowsPerPage);
    useEffect(() => {
        if (problemListQuery.data && !problemListQuery.isLoading && !problemListQuery.error) {
            setProblemList(problemListQuery.data)
        }}, [problemListQuery.data, problemListQuery.isLoading, problemListQuery.error]
    );

    const [ problemLength, setProblemLength ] = useState(0);
    const problemCountQuery = useProblemCountRequest();
    useEffect(() => {
        if (problemCountQuery.data && !problemCountQuery.isLoading && !problemCountQuery.error) {
            setProblemLength(problemCountQuery.data)
        }
    }, [problemCountQuery.data, problemCountQuery.isLoading, problemCountQuery.error]);

    const tableRows = problemList?.map((item) => (
        <TableRow
            key={ item.name }
            sx={{ cursor: 'pointer', textcolor: '#ffffff' }}
            onClick={ navigateToProblem.bind(null, item.name) }
            hover
        >
            <TableCell key={`${item.name}-status`}>{ <ProblemStatusIcon status={item.status}/> }</TableCell>
            <TableCell key={`${item.name}-id`}>{ item.name + (item.isHard ? "*" : "") } </TableCell>
            <TableCell key={`${item.name}-tags`}>
                <Stack direction={{ xs: "column", md: "row" }} spacing={{xs: 0.5, md: 1} }>
                    { item.tags.map(tag =>
                        <TagChip label={tag}/>
                    )}
                </Stack>
            </TableCell>
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

    const updateSearchParams = (pageNumber?: string, pageSize?: string) => {
        const isPageNumber = pageNumber != undefined && pageNumber != "0"
        const isPageSize = pageSize != undefined && pageSize != DEFAULT_PAGE_SIZE.toString();
        if (isPageNumber && isPageSize) {
            setSearchParams({ page: pageNumber, pageSize: pageSize });
        } else if (isPageNumber) {
            setSearchParams({ page: pageNumber });
        } else if (isPageSize) {
            setSearchParams({ pageSize: pageSize });
        } else {
            setSearchParams();
        }
    };

    useEffect(() => {
        const pageNumber = searchParams.get("page");
        const pageSize = searchParams.get("pageSize");
        setPage(() => pageNumber ? parseInt(pageNumber) : 0);
        setRowsPerPage(() => pageSize ? parseInt(pageSize) : DEFAULT_PAGE_SIZE);
    }, [searchParams.get("page"), searchParams.get("pageSize")]);

    const handleChangePage = (_: unknown, newPage: number) => {
        updateSearchParams(newPage.toString())
    };
    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        updateSearchParams("0", event.target.value);
    };

    return (
        <Box>
            <TableContainer component={ Paper }>
                <Table aria-label="problem table">
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
                            ? tableRows : tableRowsPlaceholder
                        }
                    </TableBody>
                    <TableFooter key={"table-footer"}>
                        <TableRow key={"table-footer-row"}>
                            <TablePagination
                                key={"table-pagination"}
                                rowsPerPageOptions={[DEFAULT_PAGE_SIZE, 25, 50, 100]}
                                colSpan={ 3 }
                                count={ problemLength }
                                rowsPerPage={ rowsPerPage }
                                page={ page }
                                slotProps={{
                                    select: { inputProps: { 'aria-label': 'rows per page' }, native: true },
                                }}
                                onPageChange={ handleChangePage }
                                onRowsPerPageChange={ handleChangeRowsPerPage }
                                ActionsComponent={ TablePaginationActions }
                            />
                        </TableRow>
                    </TableFooter>
                </Table>
            </TableContainer>
        </Box>
    );
}
