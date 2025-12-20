import { FC } from "react";
import { useRandomProblemIdQuery } from "../../../http/requests/problems";
import { useNavigate } from "react-router-dom";
import { IconButton, Tooltip } from "@mui/material";
import ShuffleIcon from '@mui/icons-material/Shuffle';
import { toast } from "react-toastify";

export const RandomProblemButton: FC = () => {
    const randomProblemQuery = useRandomProblemIdQuery();
    const navigate = useNavigate();
    const onClick = () => {
        randomProblemQuery.refetch()
            .then(
                (result) => {
                    const problemId = result.data;
                    navigate(`/problems/${problemId}`)
                },
                () => { toast.error("Could not randomize the problem") }
            )
    };

    return (
        <Tooltip title={"Randomize problem"}>
            <IconButton aria-label="random-problem" color="primary" size={"small"} onClick={onClick}>
                <ShuffleIcon />
            </IconButton>
        </Tooltip>
    );
}
