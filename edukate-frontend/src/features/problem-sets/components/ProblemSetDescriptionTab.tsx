import { ProblemSet } from "@/features/problem-sets/types";
import { FC } from "react";
import { Box, Divider, Paper, Stack, Typography } from "@mui/material";

interface ProblemSetDescriptionTabProps {
    problemSet: ProblemSet;
}

export const ProblemSetDescriptionTab: FC<ProblemSetDescriptionTabProps> = ({ problemSet }) => {
    return (
        <Paper variant={"outlined"} sx={{ p: 2, m: 2, mx: "auto", width: { xs: "100%", md: "50%" } }}>
            <Stack spacing={3} useFlexGap>
                <Typography color={"primary"} variant={"h4"} align="center">
                    {problemSet.name}
                </Typography>
                <Box>
                    <Stack direction={"row"} alignItems={"end"} justifyContent={"space-between"}>
                        <Typography textAlign={"start"}>Description: {problemSet.description}</Typography>
                    </Stack>
                    <Divider />
                </Box>
            </Stack>
        </Paper>
    );
};
