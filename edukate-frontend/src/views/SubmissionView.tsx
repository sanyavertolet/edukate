import { FC } from "react";
import { useParams } from "react-router-dom";
import { Box, Stack, Typography } from "@mui/material";
import { SubmissionComponent } from "../components/submission/SubmissionComponent";
import { useSubmissionQuery } from "../http/requests/submissions";

export const SubmissionView: FC = () => {
    const { id: submissionId } = useParams();

    const submissionQuery = useSubmissionQuery(submissionId);
    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={2} alignItems="center" paddingBottom="2rem">
                <Typography variant="h5" color="primary">
                    Submission { submissionId }
                </Typography>
            </Stack>

            { submissionQuery.data && <SubmissionComponent submission={ submissionQuery.data }/> }
            { submissionQuery.isLoading }
            { submissionQuery.isError }
        </Box>
    );
}