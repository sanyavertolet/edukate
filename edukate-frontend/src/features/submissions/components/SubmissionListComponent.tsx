import { useState } from "react";
import { Box } from "@mui/material";
import { useTranslation } from "react-i18next";
import { useSubmissionSearchQuery } from "@/features/submissions/api";
import { Submission } from "@/features/submissions/types";
import { SubmissionDrawer } from "@/features/submissions/components/SubmissionDrawer";
import { useAuthContext } from "@/features/auth/context";
import { SubmissionTable } from "./table/SubmissionTable";
import { SubmissionTableToolbar } from "./table/SubmissionTableToolbar";
import { SubmissionTableRows } from "./table/SubmissionTableRows";
import { SubmissionTablePagination } from "./table/SubmissionTablePagination";
import { useSubmissionTableParams, DEFAULT_PAGE_SIZE } from "@/features/submissions/hooks/useSubmissionTableParams";

export default function SubmissionListComponent() {
    const [selectedSubmission, setSelectedSubmission] = useState<Submission | null>(null);
    const { user } = useAuthContext();
    const { t } = useTranslation("submissions");

    const { page, rowsPerPage, status, userName, bookSlug, problemCode, handlers } = useSubmissionTableParams();

    const effectiveStatus = status === "ALL" ? undefined : status;
    const {
        data: pageResponse,
        isLoading,
        error,
    } = useSubmissionSearchQuery(
        page,
        rowsPerPage,
        userName || undefined,
        bookSlug || undefined,
        problemCode || undefined,
        effectiveStatus,
    );

    return (
        <Box>
            <SubmissionTable
                headerCells={["", t("book_label"), t("problem_column"), t("user_column"), t("updated_column")]}
                toolbar={
                    <SubmissionTableToolbar
                        status={status}
                        onStatusChange={handlers.onChangeStatus}
                        userName={userName}
                        onUserNameChange={handlers.onChangeUserName}
                        bookSlug={bookSlug}
                        onBookSlugChange={handlers.onChangeBookSlug}
                        problemCode={problemCode}
                        onProblemCodeChange={handlers.onChangeProblemCode}
                    />
                }
                footer={
                    <SubmissionTablePagination
                        count={pageResponse?.totalElements ?? 0}
                        page={page}
                        rowsPerPage={rowsPerPage}
                        onPageChange={handlers.onChangePage}
                        onRowsPerPageChange={handlers.onChangeRowsPerPage}
                        rowsPerPageOptions={[DEFAULT_PAGE_SIZE, 25, 50, 100]}
                    />
                }
            >
                <SubmissionTableRows
                    items={pageResponse?.content}
                    loading={isLoading}
                    error={error}
                    onRowClick={setSelectedSubmission}
                    onBookSlugClick={handlers.onChangeBookSlug}
                    onUserNameClick={handlers.onChangeUserName}
                />
            </SubmissionTable>
            <SubmissionDrawer
                submission={selectedSubmission}
                onClose={() => {
                    setSelectedSubmission(null);
                }}
                isOwner={selectedSubmission?.userName === user?.name}
            />
        </Box>
    );
}
