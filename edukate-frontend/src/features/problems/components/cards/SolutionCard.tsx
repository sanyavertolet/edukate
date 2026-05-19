import { Problem } from "@/features/problems/types";
import { CardContent, Stack, Typography } from "@mui/material";
import { Card } from "@/shared/components/Styled";
import { FileUpload } from "@/features/files/components/FileUpload";
import { AnswerAccordionComponent } from "@/features/problems/components/AnswerAccordion";
import { useSubmitProblemMutation } from "@/features/submissions/api";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { MobileFileUpload } from "@/features/files/components/MobileFileUpload";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

interface SolutionCardProps {
    problem: Problem;
}

export default function SolutionCard({ problem }: SolutionCardProps) {
    const { isMobile } = useDeviceContext();
    const { t } = useTranslation("problems");
    const { t: tSubmissions } = useTranslation("submissions");
    const submitFilesMutation = useSubmitProblemMutation();

    const handleSubmit = (fileNames: string[]) => {
        if (fileNames.length === 0) {
            toast.error(tSubmissions("select_file_error"));
            return;
        }

        submitFilesMutation.mutate(
            { problemKey: problem.key, fileNames },
            {
                onSuccess: () => toast.success(tSubmissions("submit_success")),
            },
        );
    };

    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    {t("solution_heading")}
                </Typography>
                <Stack direction="column" spacing={2} alignItems="center">
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2, textAlign: "center" }}>
                        {t("upload_solution_description")}
                    </Typography>

                    {isMobile ? (
                        <MobileFileUpload
                            accept="image/*"
                            maxFiles={5}
                            onSubmit={handleSubmit}
                            isSubmitting={submitFilesMutation.isPending}
                        />
                    ) : (
                        <FileUpload
                            accept="image/*"
                            maxFiles={5}
                            onSubmit={handleSubmit}
                            isSubmitting={submitFilesMutation.isPending}
                        />
                    )}
                    <AnswerAccordionComponent problem={problem} />
                </Stack>
            </CardContent>
        </Card>
    );
}
