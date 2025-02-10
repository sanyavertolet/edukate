import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Problem } from "../types/Problem";
import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress, Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import ProblemCardComponent from "../components/ProblemCardComponent";
import SolutionCardComponent from "../components/SolutionCardComponent";

function useProblemRequest(name: string) {
    const problemUrl = `${window.location.origin}/api/v1/problems/${name}`;
    return useQuery({
        queryKey: ['problem'],
        queryFn: async () => {
            const response = await fetch(problemUrl);
            if (!response.ok) {
                throw new Error(`Error fetching data: ${response.status}`)
            }
            return await response.json() as Problem;
        },
    });
}

export default function ProblemView() {
    const { id } = useParams();

    const { data, isLoading, error } = useProblemRequest(id!);
    const [ problem, setProblem ] = useState<Problem>()

    useEffect(() => {
        if (data && !isLoading && !error) {
            setProblem(data);
        }
    }, [data, isLoading, error]);

    return (
        <Container sx={{ my: 4 }}>
            {isLoading && (
                <Box display="flex" justifyContent="center">
                    <CircularProgress />
                </Box>
            )}

            <Typography variant="h5" gutterBottom>
                Problem {id}
            </Typography>

            {!isLoading && !error && problem && (
                <Grid container spacing={2} alignItems={"stretch"}>
                    <Grid size={6}>
                        <ProblemCardComponent problem={problem}/>
                    </Grid>
                    <Grid size={6}>
                        <SolutionCardComponent problem={problem}/>
                    </Grid>
                </Grid>
            )}

            {!isLoading && !error && !problem && (
                <Alert severity="info">Problem not found.</Alert>
            )}

            {error && (
                <Alert severity="error">{(error as Error).message}</Alert>
            )}
        </Container>
    );
}
