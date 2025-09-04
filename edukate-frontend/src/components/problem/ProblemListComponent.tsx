import { useEffect, useState } from 'react';
import { Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { ProblemMetadata } from '../../types/problem/ProblemMetadata';
import { useProblemCountRequest, useProblemListRequest } from '../../http/requests';
import { ProblemTable } from './table/ProblemTable';
import { ProblemTableToolbar } from './table/ProblemTableToolbar';
import { ProblemTableRows } from './table/ProblemTableRows';
import { ProblemTablePagination } from './table/ProblemTablePagination';
import { useProblemTableParams, DEFAULT_PAGE_SIZE } from '../../hooks/useProblemTableParams';
import { RandomProblemButton } from './table/RandomProblemButton';

export default function ProblemListComponent() {
    const navigate = useNavigate();
    const navigateToProblem = (problemName: string) => navigate(`/problems/${problemName}`);

    const { page, rowsPerPage, status, prefix, handlers } = useProblemTableParams();

    const [problemList, setProblemList] = useState<ProblemMetadata[]>([]);
    const problemListQuery = useProblemListRequest(page, rowsPerPage);
    useEffect(() => {
        if (problemListQuery.data && !problemListQuery.isLoading && !problemListQuery.error) {
            setProblemList(problemListQuery.data);
        }
    }, [problemListQuery.data, problemListQuery.isLoading, problemListQuery.error]);

    const [problemLength, setProblemLength] = useState(0);
    const problemCountQuery = useProblemCountRequest();
    useEffect(() => {
        if (problemCountQuery.data && !problemCountQuery.isLoading && !problemCountQuery.error) {
            setProblemLength(problemCountQuery.data);
        }
    }, [problemCountQuery.data, problemCountQuery.isLoading, problemCountQuery.error]);

    const isLoading = problemListQuery.isLoading || problemCountQuery.isLoading;
    const hasError = !!(problemListQuery.error || problemCountQuery.error);

    return (
        <Box>
            <ProblemTable
                headerCells={["", "Name", "Tags"]}
                toolbar={
                    <ProblemTableToolbar
                        status={status}
                        onStatusChange={handlers.onChangeStatus}
                        prefix={prefix}
                        onPrefixChange={handlers.onChangePrefix}
                        rightSlot={<RandomProblemButton />}
                    />
                }
                footer={
                    <ProblemTablePagination
                        count={problemLength}
                        page={page}
                        rowsPerPage={rowsPerPage}
                        onPageChange={handlers.onChangePage}
                        onRowsPerPageChange={handlers.onChangeRowsPerPage}
                        rowsPerPageOptions={[DEFAULT_PAGE_SIZE, 25, 50, 100]}
                    />
                }
            >
                <ProblemTableRows
                    items={problemList}
                    loading={isLoading}
                    error={hasError}
                    onRowClick={navigateToProblem}
                />
            </ProblemTable>
        </Box>
    );
}
