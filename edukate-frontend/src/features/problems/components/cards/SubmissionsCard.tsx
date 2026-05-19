import { useState } from "react";
import { CardContent, Typography } from "@mui/material";
import { Card, Paper } from "@/shared/components/Styled";
import { SubmissionList } from "@/features/submissions/components/SubmissionList";
import { SubmissionDrawer } from "@/features/submissions/components/SubmissionDrawer";
import { Submission } from "@/features/submissions/types";
import { useTranslation } from "react-i18next";

interface SubmissionsCardProps {
    problemKey: string;
}

export default function SubmissionsCard({ problemKey }: SubmissionsCardProps) {
    const { t } = useTranslation("problems");
    const [selectedSubmission, setSelectedSubmission] = useState<Submission | null>(null);

    return (
        <>
            <Card>
                <CardContent>
                    <Typography color="secondary" variant="h6" paddingBottom={1}>
                        {t("submissions_heading")}
                    </Typography>
                    <Paper sx={{ width: { xs: "100%", sm: "80%" }, justifyContent: "center", margin: "auto" }}>
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
