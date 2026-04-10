import { useParams } from "react-router-dom";
import { Box, Stack, Typography } from "@mui/material";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { ProblemComponent } from "@/features/problems/components/ProblemComponent";
import { useProblemRequest } from "@/features/problems/api";

export default function ProblemPage() {
    const { id } = useParams();
    const { data: problem } = useProblemRequest(id);

    return (
        <Box>
            <Stack direction="row" justifyContent="center" spacing={2} alignItems="center" paddingBottom={"2rem"}>
                <Typography component="h1" variant="h5" color="primary">
                    Problem {id}
                </Typography>
                <ProblemStatusIcon status={problem?.status} />
            </Stack>

            {id && <ProblemComponent problemId={id} />}
        </Box>
    );
}
