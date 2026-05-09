import { Alert, Box, CircularProgress, Stack } from "@mui/material";
import ProblemCard from "./cards/ProblemCard";
import SolutionCard from "./cards/SolutionCard";
import { useProblemRequest } from "@/features/problems/api";
import SubmissionsCard from "./cards/SubmissionsCard";
import { AuthRequired } from "@/features/auth/components/AuthRequired";
import { getApiErrorMessage } from "@/lib/api-error";
import { useTranslation } from "react-i18next";

export function ProblemComponent({ bookSlug, code }: { bookSlug: string; code: string }) {
    const { t } = useTranslation("problems");
    const { data: problem, isLoading, error } = useProblemRequest(bookSlug, code);

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
                        <SubmissionsCard problemKey={problem.key} />
                    </AuthRequired>
                </Stack>
            )}

            {!isLoading && !error && !problem && <Alert severity="info">{t("problem_not_found")}</Alert>}

            {error && <Alert severity="error">{getApiErrorMessage(error)}</Alert>}
        </Box>
    );
}
