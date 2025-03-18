import { Problem } from "../../types/Problem";
import { Card, CardContent, Link, Stack, Typography } from "@mui/material";
import DragAndDropComponent from "./DragAndDropComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { ResultAccordionComponent } from "./ResultAccordionComponent";

interface SolutionCardComponentProps {
    problem: Problem;
    refreshProblem: () => void;
}

export default function SolutionCardComponent({ problem, refreshProblem }: SolutionCardComponentProps) {
    const { user } = useAuthContext();
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6">
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
                    <Stack direction="column" spacing={2} alignItems="center" paddingTop={2}>
                        {/* todo: will be removed later */}
                        {/*<ProblemInputFormComponent problem={ problem }/>*/}
                        <DragAndDropComponent hidden={true}/>
                        <ResultAccordionComponent problemId={problem.id} refreshProblem={refreshProblem}/>
                    </Stack>
                )}
            </CardContent>
        </Card>
    );
}
