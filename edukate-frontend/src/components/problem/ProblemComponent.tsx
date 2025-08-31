import { Alert, Box, CircularProgress, Stack } from "@mui/material";
import ProblemCardComponent from "./cards/ProblemCardComponent";
import SolutionCardComponent from "./cards/SolutionCardComponent";
import { useEffect, useState } from "react";
import { useProblemRequest } from "../../http/requests";
import { Problem } from "../../types/problem/Problem";
import SubmissionsCardComponent from "./cards/SubmissionsCardComponent";
import { AuthRequired } from "../auth/AuthRequired";

interface ProblemComponentProps {
    problemId: string;
    onLoaded?: (problem: Problem) => void;
}

export function ProblemComponent({ problemId, onLoaded }: ProblemComponentProps) {
    const [ shouldRefresh, setShouldRefresh ] = useState(false);
    const { data, isLoading, error } = useProblemRequest(problemId, shouldRefresh);
    const [ problem, setProblem ] = useState<Problem>();

    useEffect(() => { if (data && !isLoading && !error) {
        setProblem(data);
        if (onLoaded) {
            onLoaded(data);
        }
    }}, [data, isLoading, error]);

    // TODO: page is being reloaded after successful submission, needs to be fixed
    const refreshProblem = () => { setShouldRefresh((flag) => !flag); };

    return (
        <Box>
            {isLoading && (<Box display="flex" justifyContent="center"><CircularProgress/></Box>)}

            {!isLoading && !error && problem && (
                <Stack display="flex" justifyContent="center" spacing={ 2 }>
                    <ProblemCardComponent problem={ problem }/>
                    <AuthRequired>
                        <SolutionCardComponent problem={ problem } refreshProblem={refreshProblem}/>
                        <SubmissionsCardComponent problemId={problem.id}/>
                    </AuthRequired>
                </Stack>
            )}

            {!isLoading && !error && !problem && ( <Alert severity="info">Problem not found.</Alert> )}

            {error && ( <Alert severity="error">{(error as Error).message}</Alert> )}
        </Box>
    )
}
