import { Card, CardContent, Typography } from "@mui/material";
import { Problem } from "../../types/problem/Problem";
import { SubtasksComponent } from "./SubtasksComponent";
import { LatexComponent } from "../LatexComponent";
import { ImageListComponent } from "../images/ImageListComponent";

interface ProblemCardComponentProps {
    problem: Problem;
}

export default function ProblemCardComponent({problem}: ProblemCardComponentProps) {
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Terms
                </Typography>

                { problem.text && (<LatexComponent text={problem.text}/>)}

                <SubtasksComponent subtasks={ problem.subtasks }/>

                { problem.images?.length > 0 && (
                    <ImageListComponent images={problem.images}/>
                )}
            </CardContent>
        </Card>
    );
}
