import { useParams } from "react-router-dom";
import { useState } from "react";
import { Box, Stack, Typography } from "@mui/material";
import { ProblemStatusIcon } from "../components/problem/ProblemStatusIcon";
import { ProblemComponent } from "../components/problem/ProblemComponent";
import { ProblemStatus } from "../types/problem/ProblemMetadata";
import { Problem } from "../types/problem/Problem";

export default function ProblemView() {
    const { id } = useParams();

    const [ problemStatus, setProblemStatus ] = useState<ProblemStatus>();
    const onLoaded = (loadedProblem: Problem) => { setProblemStatus(loadedProblem.status) };
    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={ 2 } alignItems="center" paddingBottom={"2rem"}>
                <Typography variant="h5" color="primary">
                    Problem { id }
                </Typography>
                <ProblemStatusIcon status={ problemStatus }/>
            </Stack>

            { id ? <ProblemComponent problemId={id} onLoaded={onLoaded}/> : undefined }
        </Box>
    );
};
