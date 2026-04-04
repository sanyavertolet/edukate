import { useParams } from "react-router-dom";
import { useState } from "react";
import { Box, Stack, Typography } from "@mui/material";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { ProblemStatus, Problem } from "@/features/problems/types";

export default function ProblemPage() {
    const { id } = useParams();

    const [problemStatus, setProblemStatus] = useState<ProblemStatus>();
    const onLoaded = (loadedProblem: Problem) => {
        setProblemStatus(loadedProblem.status);
    };
    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={2} alignItems="center" paddingBottom={"2rem"}>
                <Typography variant="h5" color="primary">
                    Problem {id}
                </Typography>
                <ProblemStatusIcon status={problemStatus} />
            </Stack>

            {id ? <ProblemComponent problemId={id} onLoaded={onLoaded} /> : undefined}
        </Box>
    );
}
