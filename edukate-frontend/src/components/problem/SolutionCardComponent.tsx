import { Problem } from "../../types/Problem";
import { Stack, Typography } from "@mui/material";
import { ProblemInputFormComponent } from "./ProblemInputFormComponent";
import DragAndDropComponent from "./DragAndDropComponent";
import { useAuthContext } from "../auth/AuthContextProvider";

interface SolutionCardComponentProps {
    problem: Problem;
}

export default function SolutionCardComponent({ problem }: SolutionCardComponentProps) {
    const { user } = useAuthContext();
    return ( user?.status == "ACTIVE" &&
        <Stack direction="column" spacing={ 2 } alignItems="center">
            <Typography variant="h6" color="primary">The answer will be provided soon.</Typography>
            <ProblemInputFormComponent problem={ problem }/>
            <DragAndDropComponent hidden={ true }/>
        </Stack>
    );
}
