import { useState } from "react";
import { Card, CardContent, Paper, Typography } from "@mui/material";
import { SubmissionList } from "@/features/submissions/components/SubmissionList";
import { SubmissionDrawer } from "@/features/submissions/components/SubmissionDrawer";
import { Submission } from "@/features/submissions/types";

interface SubmissionsCardProps {
    problemKey: string;
}

export default function SubmissionsCard({ problemKey }: SubmissionsCardProps) {
    const [selectedSubmission, setSelectedSubmission] = useState<Submission | null>(null);

    return (
        <>
            <Card>
                <CardContent>
                    <Typography color="secondary" variant="h6" paddingBottom={1}>
                        Submissions
                    </Typography>
                    <Paper
                        elevation={0}
                        variant={"outlined"}
                        sx={{ width: "80%", justifyContent: "center", margin: "auto" }}
                    >
                        <SubmissionList problemKey={problemKey} onSubmissionClick={setSelectedSubmission} />
                    </Paper>
                </CardContent>
            </Card>

            <SubmissionDrawer
                submission={selectedSubmission}
                onClose={() => {
                    setSelectedSubmission(null);
                }}
            />
        </>
    );
}
