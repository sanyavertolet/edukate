import { Problem } from "../../types/Problem";
import { Button, Paper, Stack, Typography } from "@mui/material";
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
    return ( user?.status == "ACTIVE" &&
        <Stack component={ Paper } direction="column" spacing={ 2 } alignItems="center" p={ 2 }>
            <Typography variant="h6" color="primary">The answer will be provided soon.</Typography>
            { submissions.length == 0 && (
                <Button variant="outlined" color="primary" onClick={ handleClick }>Mark as done</Button>
            )}
            {/* todo: will be removed later */}
            {/*<ProblemInputFormComponent problem={ problem }/>*/}
            <DragAndDropComponent hidden={ true }/>
        </Stack>
    );
}
