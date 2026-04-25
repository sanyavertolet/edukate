import { useProblemSetRequest } from "@/features/problem-sets/api";
import { useCallback, useMemo, useState } from "react";
import { Box, Card, Paper, Typography } from "@mui/material";
import { ProblemSetProblemSelector, ProblemSetSelection } from "./ProblemSetProblemSelector";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { ProblemSetDescriptionTab } from "./ProblemSetDescriptionTab";
import { ProblemSetSettingsTab } from "./ProblemSetSettingsTab";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { useAuthContext } from "@/features/auth/context";

interface ProblemSetComponentProps {
    problemSetCode?: string;
}

export function ProblemSetComponent({ problemSetCode }: ProblemSetComponentProps) {
    const { isMobile } = useDeviceContext();
    const { user } = useAuthContext();
    const { data: problemSet } = useProblemSetRequest(problemSetCode);
    const [selection, setSelection] = useState<ProblemSetSelection>({ type: "description" });

    const isAdmin = useMemo(
        () => (user && problemSet && problemSet.admins.some((value) => value === user.name)) || false,
        [user, problemSet],
    );

    const onSelectionChange = useCallback((newSelection: ProblemSetSelection) => {
        setSelection(newSelection);
    }, []);

    return (
        <Box>
            {problemSet?.name && (
                <Typography component="h1" variant="h5" color="primary">
                    {problemSet.name}
                </Typography>
            )}
            <Grid container spacing={1} paddingTop={"1rem"}>
                <Grid sx={{ sm: "none", md: "block" }} key={"left-grid"} size={"grow"}>
                    <Card>
                        <ProblemSetProblemSelector
                            problems={problemSet ? problemSet.problems : []}
                            selection={selection}
                            onSelectionChange={onSelectionChange}
                            isAdmin={isAdmin}
                        />
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={isMobile ? 12 : 10}>
                    {selection.type === "problem" ? (
                        <ProblemComponent bookSlug={selection.problem.bookSlug} code={selection.problem.code} />
                    ) : selection.type === "settings" && problemSet ? (
                        <Paper variant="outlined">
                            <ProblemSetSettingsTab problemSet={problemSet} />
                        </Paper>
                    ) : problemSet ? (
                        <Paper variant="outlined">
                            <ProblemSetDescriptionTab problemSet={problemSet} />
                        </Paper>
                    ) : null}
                </Grid>
            </Grid>
        </Box>
    );
}
