import { useProblemSetRequest } from "@/features/problem-sets/api";
import { useCallback, useMemo, useState } from "react";
import { Alert, Box, CircularProgress, Typography } from "@mui/material";
import { Card, Paper } from "@/shared/components/Styled";
import { getApiErrorMessage } from "@/lib/api-error";
import { ProblemSetProblemSelector, ProblemSetSelection } from "./ProblemSetProblemSelector";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { ProblemSetDescriptionTab } from "./ProblemSetDescriptionTab";
import { ProblemSetSettingsTab } from "./ProblemSetSettingsTab";
import { SupervisorTicketList } from "@/features/supervisor-tickets/components/SupervisorTicketList";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { useAuthContext } from "@/features/auth/context";

interface ProblemSetComponentProps {
    problemSetCode?: string;
}

export function ProblemSetComponent({ problemSetCode }: ProblemSetComponentProps) {
    const { isMobile } = useDeviceContext();
    const { user } = useAuthContext();
    const { data: problemSet, isLoading, error } = useProblemSetRequest(problemSetCode);
    const [selection, setSelection] = useState<ProblemSetSelection>({ type: "description" });

    const isAdmin = useMemo(
        () => (user && problemSet && problemSet.admins.some((value) => value === user.name)) || false,
        [user, problemSet],
    );

    const isModerator = useMemo(
        () =>
            (user && problemSet && (problemSet.admins.includes(user.name) || problemSet.moderators.includes(user.name))) ||
            false,
        [user, problemSet],
    );

    const onSelectionChange = useCallback((newSelection: ProblemSetSelection) => {
        setSelection(newSelection);
    }, []);

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" pt={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return <Alert severity="error">{getApiErrorMessage(error)}</Alert>;
    }

    return (
        <Box>
            {problemSet?.name && (
                <Typography component="h1" variant="h5" color="primary">
                    {problemSet.name}
                </Typography>
            )}
            <Grid container spacing={1} paddingTop={"1rem"}>
                <Grid sx={{ display: { xs: "none", md: "block" } }} key={"left-grid"} size={"grow"}>
                    <Card>
                        <ProblemSetProblemSelector
                            problems={problemSet ? problemSet.problems : []}
                            selection={selection}
                            onSelectionChange={onSelectionChange}
                            isAdmin={isAdmin}
                            isModerator={isModerator}
                        />
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={isMobile ? 12 : 10}>
                    {selection.type === "problem" ? (
                        <ProblemComponent
                            bookSlug={selection.problem.bookSlug}
                            code={selection.problem.code}
                            problemSetCode={problemSet?.shareCode}
                        />
                    ) : selection.type === "settings" && problemSet ? (
                        <Paper>
                            <ProblemSetSettingsTab problemSet={problemSet} />
                        </Paper>
                    ) : selection.type === "supervisor" && problemSet ? (
                        <SupervisorTicketList problemSetShareCode={problemSet.shareCode} />
                    ) : problemSet ? (
                        <Paper>
                            <ProblemSetDescriptionTab problemSet={problemSet} />
                        </Paper>
                    ) : null}
                </Grid>
            </Grid>
        </Box>
    );
}
