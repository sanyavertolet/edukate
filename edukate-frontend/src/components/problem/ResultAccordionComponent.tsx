import {useMySubmissionsRequest, useResultRequest, useSubmitMutation} from "../../http/requests";
import { Accordion, AccordionActions, AccordionDetails, AccordionSummary, Button, Container, Typography } from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useEffect, useState } from "react";
import { Result } from "../../types/Result";
import { LatexComponent } from "../LatexComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { Submission } from "../../types/Submission";

interface ResultComponentProps {
    problemId: string;
    refreshProblem: () => void;
}

export function ResultAccordionComponent({ problemId, refreshProblem }: ResultComponentProps) {
    const [result, setResult] = useState<Result>();
    const resultRequest = useResultRequest(problemId);
    useEffect(
        () => {
            resultRequest.data && !resultRequest.isLoading && !resultRequest.error && setResult(resultRequest.data);
        },
        [resultRequest.data, resultRequest.isLoading, resultRequest.error]
    );

    const [submissions, setSubmissions] = useState<Submission[]>([]);

    const mySubmissionsRequest = useMySubmissionsRequest(problemId);
    useEffect(() => {
        if (mySubmissionsRequest.data && !mySubmissionsRequest.isLoading && !mySubmissionsRequest.error) {
            setSubmissions(mySubmissionsRequest.data);
        }
    }, [mySubmissionsRequest.data, mySubmissionsRequest.isLoading, mySubmissionsRequest.error]);

    const submitMutation = useSubmitMutation(problemId);
    useEffect(() => {
        if (submitMutation.isSuccess && submitMutation.data) {
            setSubmissions(old => [...(old || []), submitMutation.data]);
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
                </AccordionDetails>
                { submissions.length == 0 && (
                    <AccordionActions>
                        <Button onClick={handleClick}>Mark as done</Button>
                    </AccordionActions>
                )}
            </Accordion>
        </Container>
    );
}
