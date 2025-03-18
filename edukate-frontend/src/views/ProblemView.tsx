import { useParams } from "react-router-dom";
import { Problem } from "../types/Problem";
import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress, Stack, Typography } from "@mui/material";
import ProblemCardComponent from "../components/problem/ProblemCardComponent";
import SolutionCardComponent from "../components/problem/SolutionCardComponent";
import { ProblemStatusIcon } from "../components/problem/ProblemStatusIcon";
import { useProblemRequest } from "../http/requests";

export default function ProblemView() {
    const { id } = useParams();
    const { data, isLoading, error } = useProblemRequest(id!);
    const [ problem, setProblem ] = useState<Problem>();

    useEffect(() => { if (data && !isLoading && !error) { setProblem(data); }}, [data, isLoading, error]);

    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={ 2 } alignItems="center" paddingBottom={"2rem"}>
                <Typography variant="h5" color="primary">
                    Problem { id }
                </Typography>
                <ProblemStatusIcon status={ problem?.status || null }/>
            </Stack>

            {isLoading && (<Box display="flex" justifyContent="center"><CircularProgress/></Box>)}

            {!isLoading && !error && problem && (
                <Stack display="flex" justifyContent="center" spacing={ 2 }>
                    <ProblemCardComponent problem={ problem }/>
                    <SolutionCardComponent problem={ problem }/>
                </Stack>
            )}

            {!isLoading && !error && !problem && ( <Alert severity="info">Problem not found.</Alert> )}

            {error && ( <Alert severity="error">{(error as Error).message}</Alert> )}
        </Box>
    );
};
