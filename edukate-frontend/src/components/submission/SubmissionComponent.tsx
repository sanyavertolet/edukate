import { FC } from "react";
import { Box, Button, Card, CardContent, CardHeader, Tooltip, Typography } from "@mui/material";
import { Submission } from "../../types/submission/Submission";
import { useCheckResultsRequest, useRequestCheckMutation } from "../../http/requests/checkResults";
import { CheckType } from "../../types/check/CheckRequest";
import { CheckResultInfoList } from "../check/CheckResultInfoList";
import { useAuthContext } from "../auth/AuthContextProvider";
import { Role } from "../../types/user/Role"

type SubmissionComponentProps = {
    submission: Submission
};

export const SubmissionComponent: FC<SubmissionComponentProps> = ({submission}) => {
    const requestCheckMutation = useRequestCheckMutation();
    const requestCheck = (checkType: CheckType) => {
        requestCheckMutation.mutate({checkType, submissionId: submission.id});
    };
    const { data: resultInfos, isLoading, error } = useCheckResultsRequest(submission.id);
    const { user } = useAuthContext();

    const isSelfCheckDisabled = submission.status == "SUCCESS";
    const isAiCheckDisabled = !["MODERATOR" as Role, "ADMIN" as Role].some(role => user?.roles?.includes(role))
    return (
        <Box sx={{ p: 2 }}>
            <SubmissionDetails submission={submission} />
            <Card>
                <CardHeader title={"Check Results"}/>
                <CardContent>

                    <Tooltip title={isSelfCheckDisabled ? "Already solved" : "Self check"}>
                        <span>
                            <Button
                                size="small" variant="text" disabled={isSelfCheckDisabled}
                                onClick={() => requestCheck("self")}
                            >
                                Consider as Solved
                            </Button>
                        </span>
                    </Tooltip>
                    <Tooltip title={isAiCheckDisabled
                        ? "WIP"
                        : "We will provide you the results as soon as possible"}
                    >
                        <span>
                            <Button
                                size="small" variant="text" disabled={isAiCheckDisabled}
                                onClick={() => requestCheck("ai")}
                            >
                                Request AI Check
                            </Button>
                        </span>
                    </Tooltip>
                    { isLoading && <Typography>Loading check results...</Typography> }
                    { error && <Typography color="error">Error loading check results</Typography> }
                    { resultInfos && resultInfos.length == 0 && <Typography>No check results found</Typography> }
                    { resultInfos && resultInfos.length > 0 && <CheckResultInfoList data={resultInfos}/> }
                </CardContent>
            </Card>
        </Box>
    );
};

function SubmissionDetails({submission}: SubmissionComponentProps) {
    return (
        <Card sx={{ mb: 2 }}>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Submission Details
                </Typography>
                <Typography variant="body1">
                    <strong>Problem ID:</strong> {submission.problemId}
                </Typography>
                <Typography variant="body1">
                    <strong>Status:</strong> {submission.status}
                </Typography>
                <Typography variant="body1">
                    <strong>Created At:</strong> {new Date(submission.createdAt).toLocaleString()}
                </Typography>
            </CardContent>
        </Card>
    );
}
