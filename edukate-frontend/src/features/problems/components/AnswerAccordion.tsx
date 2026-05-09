import { useAnswerRequest } from "@/features/problems/api";
import { Accordion, AccordionDetails, AccordionSummary, Box, Skeleton, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { useAuthContext } from "@/features/auth/context";
import { Problem } from "@/features/problems/types";
import { ImageListComponent } from "@/shared/components/images/ImageList";
import { useTranslation } from "react-i18next";

type AnswerComponentProps = { problem: Problem };

export function AnswerAccordionComponent({ problem }: AnswerComponentProps) {
    const { data: result, isLoading } = useAnswerRequest(problem.bookSlug, problem.code);
    const { isAuthorized } = useAuthContext();
    const { t } = useTranslation("problems");

    if (isLoading) {
        return (
            <Box sx={{ width: { xs: "100%", sm: "80%" } }}>
                <Skeleton variant="rectangular" height={48} sx={{ borderRadius: 1 }} />
            </Box>
        );
    }

    if (!result) return null;

    return (
        <Box sx={{ width: { xs: "100%", sm: "80%" } }}>
            <Accordion disabled={!isAuthorized}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="answer-content" id="answer-header">
                    <Typography component="span">{t("show_answer")}</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    {result.text && <LazyLatexComponent text={result.text} />}
                    {result.images.length > 0 && <ImageListComponent images={result.images} />}
                </AccordionDetails>
            </Accordion>
        </Box>
    );
}
