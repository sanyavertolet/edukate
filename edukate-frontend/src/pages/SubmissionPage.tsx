import { useParams } from "react-router-dom";
import { Alert, Box, CircularProgress, Stack, Typography } from "@mui/material";
import { getApiErrorMessage } from "@/lib/api-error";
import { SubmissionComponent } from "@/features/submissions/components/SubmissionComponent";
import { useSubmissionQuery } from "@/features/submissions/api";
import { useTranslation } from "react-i18next";

export default function SubmissionPage() {
    const { id: submissionId } = useParams();
    const { t } = useTranslation("submissions");

    const submissionQuery = useSubmissionQuery(submissionId);
    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={2} alignItems="center" paddingBottom="2rem">
                <Typography component="h1" variant="h5" color="primary">
                    {t("submission_heading", { id: submissionId })}
                </Typography>
            </Stack>

            {submissionQuery.isLoading && (
                <Box display="flex" justifyContent="center" pt={4}>
                    <CircularProgress />
                </Box>
            )}

            {submissionQuery.isError && <Alert severity="error">{getApiErrorMessage(submissionQuery.error)}</Alert>}

            {submissionQuery.data && <SubmissionComponent submission={submissionQuery.data} />}
        </Box>
    );
}
