import { Problem } from "../../types/Problem";
import { Alert, Card, CardContent, Link, Snackbar, Stack, Typography } from "@mui/material";
import { DragAndDropComponent } from "../files/DragAndDropComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { ResultAccordionComponent } from "./ResultAccordionComponent";
import { useEffect, useState } from "react";
import { useSubmitProblemMutation } from "../../http/requests";

interface SolutionCardComponentProps {
    problem: Problem;
    refreshProblem: () => void;
}

export default function SolutionCardComponent({ problem, refreshProblem }: SolutionCardComponentProps) {
    const { user } = useAuthContext();

    const [error, setError] = useState<string | null>(null);
    const [isSuccess, setIsSuccess] = useState<boolean>(false);

    const submitFilesMutation = useSubmitProblemMutation();

    useEffect(() => {
        if (submitFilesMutation.error) {
            setError(submitFilesMutation.error.message);
        }
    }, [submitFilesMutation.error]);

    const handleSubmit = (fileKeys: string[]) => {
        if (fileKeys.length === 0) {
            setError('Please select at least one file to submit');
            return;
        }

        submitFilesMutation.mutate(
            { problemId: problem.id, fileKeys },
            { onSuccess: () => {
                setIsSuccess(true);
                refreshProblem();
            }}
        );
    };

    const handleCloseSnackbar = () => {
        setError(null);
        setIsSuccess(false);
    };

    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Solution
                </Typography>
                { user?.status == "PENDING" && (
                    <Typography variant="body1" color="primary">Account pending approval.</Typography>
                )}
                { user == null && (
                    <Typography variant="body1" color="primary">
                        <Link href={"/sign-in"}>Sign in</Link>/<Link href={"/sign-up"}>sign up</Link> to solve.
                    </Typography>
                )}
                { user?.status == "ACTIVE" && (
                    <Stack direction="column" spacing={2} alignItems="center">
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2, textAlign: 'center', maxWidth: '600px' }}>
                            Take photos of your handwritten solution and upload them here. You can upload up to 5 images.
                        </Typography>

                        <DragAndDropComponent
                            accept="image/*"
                            maxFiles={5}
                            onSubmit={handleSubmit}
                        />

                        <ResultAccordionComponent problem={problem} refreshProblem={refreshProblem}/>

                        <Snackbar open={!!error} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                            <Alert onClose={handleCloseSnackbar} severity="error" sx={{ width: '100%' }}>
                                {error}
                            </Alert>
                        </Snackbar>

                        <Snackbar open={isSuccess} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                            <Alert onClose={handleCloseSnackbar} severity="success" sx={{ width: '100%' }}>
                                Your solution has been submitted successfully!
                            </Alert>
                        </Snackbar>
                    </Stack>
                )}
            </CardContent>
        </Card>
    );
}
