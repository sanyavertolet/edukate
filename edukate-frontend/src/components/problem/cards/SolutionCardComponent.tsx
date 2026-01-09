import { Problem } from "../../../types/problem/Problem";
import { Card, CardContent, Stack, Typography } from "@mui/material";
import { FileUploadComponent } from "../../files/FileUploadComponent";
import { ResultAccordionComponent } from "../ResultAccordionComponent";
import { useSubmitProblemMutation } from "../../../http/requests/submissions";
import { useDeviceContext } from "../../topbar/DeviceContextProvider";
import { MobileFileUploadComponent } from "../../files/MobileFileUploadComponent";
import { toast } from "react-toastify";

interface SolutionCardComponentProps {
    problem: Problem;
    refreshProblem: () => void;
}

export default function SolutionCardComponent({ problem, refreshProblem }: SolutionCardComponentProps) {
    const { isMobile } = useDeviceContext();

    const submitFilesMutation = useSubmitProblemMutation();

    const handleSubmit = (fileNames: string[]) => {
        if (fileNames.length === 0) {
            toast.error("Please select at least one file to submit");
            return;
        }

        submitFilesMutation.mutate(
            { problemId: problem.id, fileNames },
            { onSuccess: () => {
                toast.success("Your solution has been submitted successfully!");
                refreshProblem();
            }, onError: (error) => {
                toast.error(error.message);
            }}
        );
    };

    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Solution
                </Typography>
                <Stack direction="column" spacing={2} alignItems="center">
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2, textAlign: 'center' }}>
                        Take photos of your handwritten solution and upload them here. You can upload up to 5 images.
                    </Typography>

                    { isMobile
                        ? <MobileFileUploadComponent accept="image/*" maxFiles={5} onSubmit={handleSubmit}/>
                        : <FileUploadComponent accept="image/*" maxFiles={5} onSubmit={handleSubmit}/>
                    }
                    <ResultAccordionComponent problem={problem}/>
                </Stack>
            </CardContent>
        </Card>
    );
}
