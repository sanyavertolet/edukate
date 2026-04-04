import { Box } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useProblemCountRequest, useProblemListRequest } from "@/features/problems/api";
import { ProblemTable } from "./table/ProblemTable";
import { ProblemTableToolbar } from "./table/ProblemTableToolbar";
import { ProblemTableRows } from "./table/ProblemTableRows";
import { ProblemTablePagination } from "./table/ProblemTablePagination";
import { useProblemTableParams, DEFAULT_PAGE_SIZE } from "@/features/problems/hooks/useProblemTableParams";
import { RandomProblemButton } from "./table/RandomProblemButton";

export default function ProblemListComponent() {
    const navigate = useNavigate();
    const navigateToProblem = (problemName: string) => navigate(`/problems/${problemName}`);

    const { page, rowsPerPage, status, prefix, handlers } = useProblemTableParams();

    const { data: problemList, isLoading: isListLoading, error: listError } = useProblemListRequest(page, rowsPerPage);
    const { data: problemCount, isLoading: isCountLoading, error: countError } = useProblemCountRequest();

    const isLoading = isListLoading || isCountLoading;
    const hasError = !!(listError || countError);

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
                        count={problemCount ?? 0}
                        page={page}
                        rowsPerPage={rowsPerPage}
                        onPageChange={handlers.onChangePage}
                        onRowsPerPageChange={handlers.onChangeRowsPerPage}
                        rowsPerPageOptions={[DEFAULT_PAGE_SIZE, 25, 50, 100]}
                    />
                }
            >
                <ProblemTableRows
                    items={problemList ?? []}
                    loading={isLoading}
                    error={hasError}
                    onRowClick={navigateToProblem}
                />
            </ProblemTable>
        </Box>
    );
}
