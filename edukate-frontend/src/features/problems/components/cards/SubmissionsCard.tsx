import { Card, CardContent, Paper, Typography } from "@mui/material";
import { SubmissionList } from "@/features/submissions/components/SubmissionList";

interface SubmissionsCardProps {
    problemId: string;
}

export default function SubmissionsCard({ problemId }: SubmissionsCardProps) {
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Submissions
                </Typography>
                <Paper elevation={0} variant={"outlined"} sx={{ width: "80%", justifyContent: "center", margin: "auto" }}>
                    <SubmissionList problemId={problemId} />
                </Paper>
            </CardContent>
        </Card>
    );
}
