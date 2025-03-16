import { Problem } from "../../types/Problem";
import { Button, Card, CardContent, Link, Stack, Typography } from "@mui/material";
import DragAndDropComponent from "./DragAndDropComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { useSubmitMutation, useMySubmissionsRequest } from "../../http/requests";
import { useEffect, useState } from "react";
import { Submission } from "../../types/Submission";

interface SolutionCardComponentProps {
    problem: Problem;
}

export default function SolutionCardComponent({ problem }: SolutionCardComponentProps) {
    const [submissions, setSubmissions] = useState<Submission[]>([]);

    const { data, isLoading, error } = useMySubmissionsRequest(problem.id);
    useEffect(() => {
        if (data && !isLoading && !error) {
            setSubmissions(data);
        }
    }, [data, isLoading, error]);

    const submitMutation = useSubmitMutation(problem.id);
    useEffect(() => {
        if (submitMutation.isSuccess && submitMutation.data) {
            setSubmissions(old => [...(old || []), submitMutation.data]);
        }
    }, [submitMutation.isSuccess, submitMutation.data]);

    const handleClick = () => { submitMutation.mutate() };
    const { user } = useAuthContext();
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6">
                    Solution
                </Typography>
                { user?.status == "ACTIVE" && (
                    <Stack direction="column" spacing={2} alignItems="center">
                        <Typography variant="body1" color="primary">The answer will be provided soon.</Typography>
                        {submissions.length == 0 && (
                            <Button variant="outlined" color="primary" onClick={handleClick}>Mark as done</Button>
                        )}
                        {/* todo: will be removed later */}
                        {/*<ProblemInputFormComponent problem={ problem }/>*/}
                        <DragAndDropComponent hidden={true}/>
                    </Stack>
                )}
                { user?.status == "PENDING" && (
                    <Typography variant="body1" color="primary">Account pending approval.</Typography>
                )}
                { user == null && (
                    <Typography variant="body1" color="primary">
                        <Link href={"/sign-in"}>Sign in</Link>/<Link href={"/sign-up"}>sign up</Link> to solve.
                    </Typography>
                )}
            </CardContent>
        </Card>
    );
}
