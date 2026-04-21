import { Card, CardContent, Paper, Typography } from "@mui/material";
import { SubmissionList } from "@/features/submissions/components/SubmissionList";

interface SubmissionsCardProps {
    problemKey: string;
}

export default function SubmissionsCard({ problemKey }: SubmissionsCardProps) {
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Submissions
                </Typography>
                <Paper elevation={0} variant={"outlined"} sx={{ width: "80%", justifyContent: "center", margin: "auto" }}>
                    <SubmissionList problemKey={problemKey} />
                </Paper>
            </CardContent>
        </Card>
    );
}
