import { useResultRequest } from "@/features/problems/api";
import { Accordion, AccordionDetails, AccordionSummary, Box, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { useAuthContext } from "@/features/auth/context";
import { Problem } from "@/features/problems/types";
import { ImageListComponent } from "@/shared/components/images/ImageList";

type ResultComponentProps = { problem: Problem };

export function ResultAccordionComponent({ problem }: ResultComponentProps) {
    const { data: result } = useResultRequest(problem.id);
    const { isAuthorized } = useAuthContext();

    if (!result) return null;

    return (
        <Box width={"80%"}>
            <Accordion disabled={!isAuthorized}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="result-content" id="result-header">
                    <Typography component="span">Show the result</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    {result.text && <LazyLatexComponent text={result.text} />}
                    {result.images.length > 0 && <ImageListComponent images={result.images} />}
                </AccordionDetails>
            </Accordion>
        </Box>
    );
}
