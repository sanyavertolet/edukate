import { useProblemSetRequest } from "@/features/problem-sets/api";
import { useCallback, useState } from "react";
import { Box, Card, Typography } from "@mui/material";
import { ProblemSetProblemSelector } from "./ProblemSetProblemSelector";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { ProblemSetIndexCard } from "./ProblemSetIndexCard";
import Grid from "@mui/material/Grid2";
import { useDeviceContext } from "@/shared/context/DeviceContext";

interface ProblemSetComponentProps {
    problemSetCode?: string;
}

export function ProblemSetComponent({ problemSetCode }: ProblemSetComponentProps) {
    const { isMobile } = useDeviceContext();
    const { data: problemSet } = useProblemSetRequest(problemSetCode);
    const [selectedProblemMetadata, setSelectedProblemMetadata] = useState<ProblemMetadata>();
    const onProblemSelect = useCallback((problemOrUndefined?: ProblemMetadata) => {
        setSelectedProblemMetadata(problemOrUndefined);
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
                            problemSetName={problemSet ? problemSet.name : "Index"}
                            problems={problemSet ? problemSet.problems : []}
                            onProblemSelect={onProblemSelect}
                            selectedProblem={selectedProblemMetadata}
                        />
                    </Card>
                </Grid>
                <Grid key={"central-grid"} size={isMobile ? 12 : 10}>
                    {selectedProblemMetadata ? (
                        <ProblemComponent bookSlug={selectedProblemMetadata.bookSlug} code={selectedProblemMetadata.code} />
                    ) : (
                        <ProblemSetIndexCard problemSet={problemSet} />
                    )}
                </Grid>
            </Grid>
        </Box>
    );
}
