import { Alert, Box, CircularProgress, Stack } from "@mui/material";
import ProblemCardComponent from "./ProblemCardComponent";
import SolutionCardComponent from "./SolutionCardComponent";
import { useEffect, useState } from "react";
import { useProblemRequest } from "../../http/requests";
import { Problem } from "../../types/Problem";

interface ProblemComponentProps {
    problemId?: string;
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

    // todo: page is being reloaded after successful submission, needs to be fixed
    const refreshProblem = () => { setShouldRefresh((flag) => !flag); };

    return (
        <Box>
            {isLoading && (<Box display="flex" justifyContent="center"><CircularProgress/></Box>)}

            {!isLoading && !error && problem && (
                <Stack display="flex" justifyContent="center" spacing={ 2 }>
                    <ProblemCardComponent problem={ problem }/>
                    <SolutionCardComponent problem={ problem } refreshProblem={refreshProblem}/>
                </Stack>
            )}

            {!isLoading && !error && !problem && ( <Alert severity="info">Problem not found.</Alert> )}

            {error && ( <Alert severity="error">{(error as Error).message}</Alert> )}
        </Box>
    )
}
