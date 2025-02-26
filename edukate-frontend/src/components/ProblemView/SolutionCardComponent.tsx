import { Problem } from "../../types/Problem";
import { Stack } from "@mui/material";
import { ProblemInputFormComponent } from "./ProblemInputFormComponent.tsx";
import DragAndDropComponent from "./DragAndDropComponent.tsx";

interface SolutionCardComponentProps {
    problem: Problem;
}

export default function SolutionCardComponent({problem}: SolutionCardComponentProps) {
    return (
        <Stack direction={'column'} spacing={2} alignItems={'center'} >
            <ProblemInputFormComponent problem={problem}/>

            <DragAndDropComponent hidden={true}/>
        </Stack>
    )
}
