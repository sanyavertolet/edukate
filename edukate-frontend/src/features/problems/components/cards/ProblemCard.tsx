import { Card, CardContent, Typography } from "@mui/material";
import { Problem } from "@/features/problems/types";
import { SubproblemsComponent } from "@/features/problems/components/SubproblemsComponent";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { ImageListComponent } from "@/shared/components/images/ImageList";
import { useTranslation } from "react-i18next";

interface ProblemCardProps {
    problem: Problem;
}

export default function ProblemCard({ problem }: ProblemCardProps) {
    const { t } = useTranslation("problems");
    return (
        <Card>
            <CardContent>
                <Typography color="secondary" variant="h6" paddingBottom={1}>
                    {t("terms_heading")}
                </Typography>

                {problem.text && <LazyLatexComponent text={problem.text} />}

                <SubproblemsComponent subproblems={problem.subproblems} />

                {problem.images.length > 0 && <ImageListComponent images={problem.images} />}
            </CardContent>
        </Card>
    );
}
