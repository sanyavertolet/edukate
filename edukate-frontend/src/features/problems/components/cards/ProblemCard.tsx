import { Card, CardContent, Typography } from "@mui/material";
import { Problem } from "@/features/problems/types";
import { SubtasksComponent } from "@/features/problems/components/SubtasksComponent";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { ImageListComponent } from "@/shared/components/images/ImageList";

interface ProblemCardProps {
    problem: Problem;
}

export default function ProblemCard({ problem }: ProblemCardProps) {
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    Terms
                </Typography>

                {problem.text && <LazyLatexComponent text={problem.text} />}

                <SubtasksComponent subtasks={problem.subtasks} />

                {problem.images?.length > 0 && <ImageListComponent images={problem.images} />}
            </CardContent>
        </Card>
    );
}
