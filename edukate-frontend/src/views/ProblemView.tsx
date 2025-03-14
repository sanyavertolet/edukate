import { useParams } from "react-router-dom";
import { Problem } from "../types/Problem";
import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress, Container, Stack, Typography } from "@mui/material";
import ProblemCardComponent from "../components/problem/ProblemCardComponent";
import SolutionCardComponent from "../components/problem/SolutionCardComponent";
import { useProblemRequest } from "../http/requests";

export default function ProblemView() {
    const { id } = useParams();
    const { data, isLoading, error } = useProblemRequest(id!);
    const [ problem, setProblem ] = useState<Problem>();

    useEffect(() => { if (data && !isLoading && !error) { setProblem(data); }}, [data, isLoading, error]);

    return (
        <Container sx={{ my: 4 }}>
            <Typography variant="h5" color="primary" gutterBottom>
                Problem { id }
            </Typography>

            {isLoading && (
                <Box display="flex" justifyContent="center">
                    <CircularProgress />
                </Box>
            )}

            {!isLoading && !error && problem && (
                <Stack display="flex" justifyContent="center" spacing={ 2 } padding={ 2 }>
                    <ProblemCardComponent problem={ problem }/>
                    { problem.hasResult && <SolutionCardComponent problem={ problem }/>}
                </Stack>
            )}

            {!isLoading && !error && !problem && (
                <Alert severity="info">Problem not found.</Alert>
            )}

            {error && (
                <Alert severity="error">{(error as Error).message}</Alert>
            )}
        </Container>
    );
};
