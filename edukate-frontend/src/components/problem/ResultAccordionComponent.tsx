import { useResultRequest, useSubmitMutation } from "../../http/requests";
import { Accordion, AccordionActions, AccordionDetails, AccordionSummary, Button, Container, Typography } from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useEffect, useState } from "react";
import { Result } from "../../types/Result";
import { LatexComponent } from "../LatexComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { Problem } from "../../types/Problem";
import { ImageListComponent } from "../images/ImageListComponent";

interface ResultComponentProps {
    problem: Problem;
    refreshProblem: () => void;
}

export function ResultAccordionComponent({ problem, refreshProblem }: ResultComponentProps) {
    const [result, setResult] = useState<Result>();
    const resultRequest = useResultRequest(problem.id);
    useEffect(
        () => {
            resultRequest.data && !resultRequest.isLoading && !resultRequest.error && setResult(resultRequest.data);
        },
        [resultRequest.data, resultRequest.isLoading, resultRequest.error]
    );

    const submitMutation = useSubmitMutation(problem.id);
    useEffect(() => {
        if (submitMutation.isSuccess && submitMutation.data) {
            refreshProblem();
        }
    }, [submitMutation.isSuccess, submitMutation.data]);

    const handleClick = () => { submitMutation.mutate() };

    const { user } = useAuthContext();
    return (
        <Container>
            <Accordion disabled={!user}>
                <AccordionSummary expandIcon={<ExpandMoreIcon/>} aria-controls="result-content" id="result-header">
                    <Typography component="span">Show the result</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    { result?.text && <LatexComponent text={result?.text}/>}
                    { !!result?.images && <ImageListComponent images={result?.images}/> }
                </AccordionDetails>
                { problem.status != "SOLVED" && (
                    <AccordionActions>
                        <Button onClick={handleClick}>Mark as done</Button>
                    </AccordionActions>
                )}
            </Accordion>
        </Container>
    );
}
