import { useAnswerRequest } from "@/features/problems/api";
import { Accordion, AccordionDetails, AccordionSummary, Box, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { useAuthContext } from "@/features/auth/context";
import { Problem } from "@/features/problems/types";
import { ImageListComponent } from "@/shared/components/images/ImageList";

type AnswerComponentProps = { problem: Problem };

export function AnswerAccordionComponent({ problem }: AnswerComponentProps) {
    const { data: result } = useAnswerRequest(problem.bookSlug, problem.code);
    const { isAuthorized } = useAuthContext();

    if (!result) return null;

    return (
        <Box width={"80%"}>
            <Accordion disabled={!isAuthorized}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="answer-content" id="answer-header">
                    <Typography component="span">Show the answer</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    {result.text && <LazyLatexComponent text={result.text} />}
                    {result.images.length > 0 && <ImageListComponent images={result.images} />}
                </AccordionDetails>
            </Accordion>
        </Box>
    );
}
