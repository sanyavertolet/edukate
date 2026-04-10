import { Alert, Box, CircularProgress, Stack } from "@mui/material";
import ProblemCard from "./cards/ProblemCard";
import SolutionCard from "./cards/SolutionCard";
import { useProblemRequest } from "@/features/problems/api";
import SubmissionsCard from "./cards/SubmissionsCard";
import { AuthRequired } from "@/features/auth/components/AuthRequired";
import { getApiErrorMessage } from "@/lib/api-error";

export function ProblemComponent({ problemId }: { problemId: string }) {
    const { data: problem, isLoading, error } = useProblemRequest(problemId);

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

            {error && <Alert severity="error">{getApiErrorMessage(error)}</Alert>}
        </Box>
    );
}
