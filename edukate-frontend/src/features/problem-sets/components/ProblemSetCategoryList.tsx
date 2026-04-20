import { Box, Card, CardContent, Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { ProblemSetCard } from "./ProblemSetCard";
import { useProblemSetsRequest } from "@/features/problem-sets/api";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { toast } from "react-toastify";

interface ProblemSetCategoryListProps {
    tab: ProblemSetCategory;
}

export function ProblemSetCategoryList({ tab }: ProblemSetCategoryListProps) {
    const { data: problemSets } = useProblemSetsRequest(tab);

    return (
        <Box>
            <Grid container rowSpacing={2} spacing={2}>
                {(!problemSets || problemSets.length == 0) && <ProblemSetEmptyList />}

                {problemSets?.map((problemSet, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3 }} key={`grid-${String(index)}`}>
                        <ProblemSetCard problemSetMetadata={problemSet} onCopy={() => toast.info("Copied to clipboard.")} />
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
}

export function ProblemSetEmptyList() {
    return (
        <Container>
            <Card>
                <CardContent>
                    <Typography variant={"h4"} color={"primary"} align={"center"}>
                        No problem sets found.
                    </Typography>
                    <Typography variant={"body1"}>
                        Create your own problem set or join one of the public problem sets.
                    </Typography>
                </CardContent>
            </Card>
        </Container>
    );
}
