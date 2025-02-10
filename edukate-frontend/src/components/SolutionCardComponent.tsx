import { Problem } from "../types/Problem";
import { Card, CardContent, Divider, Typography } from "@mui/material";

interface SolutionCardComponentProps {
    problem: Problem;
}

export default function SolutionCardComponent({problem}: SolutionCardComponentProps) {
    console.log(problem)
    return (
        <Card>
            <CardContent>
                <Typography variant={"h6"}>
                    Solution
                </Typography>
                <Divider sx={{ my: 2 }} />

            </CardContent>
        </Card>
    )
}
