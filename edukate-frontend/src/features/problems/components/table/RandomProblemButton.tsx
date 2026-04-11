import { FC } from "react";
import { useRandomProblemIdQuery } from "@/features/problems/api";
import { useNavigate } from "react-router-dom";
import { IconButton, Tooltip } from "@mui/material";
import ShuffleIcon from "@mui/icons-material/Shuffle";
import { toast } from "react-toastify";

export const RandomProblemButton: FC = () => {
    const randomProblemQuery = useRandomProblemIdQuery();
    const navigate = useNavigate();
    const onClick = () => {
        void randomProblemQuery.refetch().then(
            (result) => {
                const problemId = result.data;
                void navigate(`/problems/${problemId ?? ""}`);
            },
            () => {
                toast.error("Could not randomize the problem");
            },
        );
    };

    return (
        <Tooltip title={"Randomize problem"}>
            <IconButton aria-label="random-problem" color="primary" onClick={onClick}>
                <ShuffleIcon sx={{ fontSize: 30 }} />
            </IconButton>
        </Tooltip>
    );
};
