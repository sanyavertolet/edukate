import { Card, CardContent, Paper, Typography } from "@mui/material";
import { SubmissionListComponent } from "../../submission/SubmissionListComponent";

interface SubmissionsCardComponentProps {
    problemId: string;
}

export default function SubmissionsCardComponent({ problemId }: SubmissionsCardComponentProps) {
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Submissions
                </Typography>
                <Paper elevation={0} variant={"outlined"} sx={{ width: "60%", justifyContent: "center", margin: "auto"}}>
                    <SubmissionListComponent problemId={problemId}/>
                </Paper>
            </CardContent>
        </Card>
    );
}
