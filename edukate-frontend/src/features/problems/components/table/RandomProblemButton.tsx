import { FC } from "react";
import { useRandomProblemKeyQuery } from "@/features/problems/api";
import { useNavigate } from "react-router-dom";
import { Fab, IconButton, Tooltip } from "@mui/material";
import ShuffleIcon from "@mui/icons-material/Shuffle";
import { toast } from "react-toastify";

function useRandomProblemNavigation() {
    const randomProblemQuery = useRandomProblemKeyQuery();
    const navigate = useNavigate();
    const onClick = () => {
        void randomProblemQuery.refetch().then(
            (result) => {
                const problemKey = result.data;
                void navigate(`/problems/${problemKey ?? ""}`);
            },
            () => {
                toast.error("Could not randomize the problem");
            },
        );
    };
    return onClick;
}

export const RandomProblemButton: FC = () => {
    const onClick = useRandomProblemNavigation();

    return (
        <Tooltip title={"Randomize problem"}>
            <IconButton aria-label="random-problem" color="primary" onClick={onClick}>
                <ShuffleIcon sx={{ fontSize: 30 }} />
            </IconButton>
        </Tooltip>
    );
};

export const RandomProblemFab: FC = () => {
    const onClick = useRandomProblemNavigation();

    return (
        <Fab color="primary" aria-label="random-problem" onClick={onClick} sx={{ position: "fixed", bottom: 24, right: 24 }}>
            <ShuffleIcon />
        </Fab>
    );
};
