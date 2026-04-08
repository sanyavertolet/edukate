import { Problem } from "@/features/problems/types";
import { Card, CardContent, Stack, Typography } from "@mui/material";
import { FileUpload } from "@/features/files/components/FileUpload";
import { ResultAccordionComponent } from "@/features/problems/components/ResultAccordion";
import { useSubmitProblemMutation } from "@/features/submissions/api";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { MobileFileUpload } from "@/features/files/components/MobileFileUpload";
import { toast } from "react-toastify";

interface SolutionCardProps {
    problem: Problem;
}

export default function SolutionCard({ problem }: SolutionCardProps) {
    const { isMobile } = useDeviceContext();
    const submitFilesMutation = useSubmitProblemMutation();

    const handleSubmit = (fileNames: string[]) => {
        if (fileNames.length === 0) {
            toast.error("Please select at least one file to submit");
            return;
        }

        submitFilesMutation.mutate(
            { problemId: problem.id, fileNames },
            {
                onSuccess: () => toast.success("Your solution has been submitted successfully!"),
            },
        );
    };

    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Solution
                </Typography>
                <Stack direction="column" spacing={2} alignItems="center">
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2, textAlign: "center" }}>
                        Take photos of your handwritten solution and upload them here. You can upload up to 5 images.
                    </Typography>

                    {isMobile ? (
                        <MobileFileUpload accept="image/*" maxFiles={5} onSubmit={handleSubmit} />
                    ) : (
                        <FileUpload accept="image/*" maxFiles={5} onSubmit={handleSubmit} />
                    )}
                    <ResultAccordionComponent problem={problem} />
                </Stack>
            </CardContent>
        </Card>
    );
}
