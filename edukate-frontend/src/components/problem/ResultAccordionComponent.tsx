import { useResultRequest } from "../../http/requests";
import {
    Accordion,
    AccordionActions,
    AccordionDetails,
    AccordionSummary,
    Box,
    Button,
    Typography
} from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useEffect, useState } from "react";
import { Result } from "../../types/problem/Result";
import { LatexComponent } from "../LatexComponent";
import { useAuthContext } from "../auth/AuthContextProvider";
import { Problem } from "../../types/problem/Problem";
import { ImageListComponent } from "../images/ImageListComponent";

interface ResultComponentProps {
    problem: Problem;
    refreshProblem: () => void;
}

// TODO: fix Mark as done button when submissions are finished
export function ResultAccordionComponent({ problem /*, refreshProblem */ }: ResultComponentProps) {
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

    // FIXME: implement submission mutation
    // const submitMutation = useSubmitMutation(problem.id);
    // useEffect(() => {
    //     if (submitMutation.isSuccess && submitMutation.data) {
    //         refreshProblem();
    //     }
    // }, [submitMutation.isSuccess, submitMutation.data, refreshProblem]);

    const handleClick = () => { /* submitMutation.mutate() */ };

    const { isAuthorized } = useAuthContext();
    return (
        <Box width="60%">
            <Accordion disabled={!isAuthorized}>
                <AccordionSummary expandIcon={<ExpandMoreIcon/>} aria-controls="result-content" id="result-header">
                    <Typography component="span">Show the result</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    { result?.text && <LatexComponent text={result?.text}/>}
                    { !!result?.images && result.images.length != 0 && <ImageListComponent images={result.images}/> }
                </AccordionDetails>
                { problem.status != "SOLVED" && (
                    <AccordionActions>
                        <Button disabled onClick={handleClick}>Mark as done</Button>
                    </AccordionActions>
                )}
            </Accordion>
        </Box>
    );
}
