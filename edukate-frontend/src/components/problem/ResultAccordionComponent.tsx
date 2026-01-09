import { useResultRequest } from "../../http/requests/problems";
import { Accordion, AccordionDetails, AccordionSummary, Box, Typography } from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useEffect, useState } from "react";
import { Result } from "../../types/problem/Result";
import { LatexComponent } from "../LatexComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { Problem } from "../../types/problem/Problem";
import { ImageListComponent } from "../images/ImageListComponent";

type ResultComponentProps = { problem: Problem }

export function ResultAccordionComponent({ problem }: ResultComponentProps) {
    const [result, setResult] = useState<Result>();
    const resultRequest = useResultRequest(problem.id);
    useEffect(
        () => {
            if (resultRequest.data && !resultRequest.isLoading && !resultRequest.error) {
                setResult(resultRequest.data);
            }
        },
        [resultRequest.data, resultRequest.isLoading, resultRequest.error]
    );

    const { isAuthorized } = useAuthContext();
    return (
        <Box width={"80%"}>
            <Accordion disabled={!isAuthorized}>
                <AccordionSummary expandIcon={<ExpandMoreIcon/>} aria-controls="result-content" id="result-header">
                    <Typography component="span">Show the result</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    { result?.text && <LatexComponent text={result?.text}/>}
                    { !!result?.images && result.images.length != 0 && <ImageListComponent images={result.images}/> }
                </AccordionDetails>
            </Accordion>
        </Box>
    );
}
