import { Alert, Box, CircularProgress, Stack } from "@mui/material";
import ProblemCard from "./cards/ProblemCard";
import SolutionCard from "./cards/SolutionCard";
import { useEffect } from "react";
import { useProblemRequest } from "@/features/problems/api";
import { Problem } from "@/features/problems/types";
import SubmissionsCard from "./cards/SubmissionsCard";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

interface ProblemComponentProps {
    problemId: string;
    onLoaded?: (problem: Problem) => void;
}

export function ProblemComponent({ problemId, onLoaded }: ProblemComponentProps) {
    const { data: problem, isLoading, error } = useProblemRequest(problemId);

    useEffect(() => {
        if (problem && onLoaded) {
            onLoaded(problem);
        }
    }, [problem]);

    return (
        <Box>
            {isLoading && (
                <Box display="flex" justifyContent="center">
                    <CircularProgress />
                </Box>
            )}

            {!isLoading && !error && problem && (
                <Stack display="flex" justifyContent="center" spacing={2}>
                    <ProblemCard problem={problem} />
                    <AuthRequired>
                        <SolutionCard problem={problem} />
                        <SubmissionsCard problemId={problem.id} />
                    </AuthRequired>
                </Stack>
            )}

            {!isLoading && !error && !problem && <Alert severity="info">Problem not found.</Alert>}

            {error && <Alert severity="error">{(error as Error).message}</Alert>}
        </Box>
    );
}
