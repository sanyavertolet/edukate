import { FC } from "react";
import { Box, Paper, Typography } from "@mui/material";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemSetUserManagement } from "./ProblemSetUserManagement";

interface ProblemSetSettingsTabProps {
    problemSet: ProblemSet;
}

export const ProblemSetSettingsTab: FC<ProblemSetSettingsTabProps> = ({ problemSet }) => {
    return (
        <Box>
            <Paper variant={"outlined"} sx={{ p: 2, m: 2, mx: "auto", width: { xs: "100%", md: "50%" } }}>
                <Box>
                    <Typography color={"primary"} variant={"h4"} align="center">
                        Users
                    </Typography>
                    <ProblemSetUserManagement shareCode={problemSet.shareCode} />
                </Box>
            </Paper>
        </Box>
    );
};
