import { FC, FormEvent, useState } from "react";
import { Divider, IconButton, InputBase, Paper } from "@mui/material";
import { Send } from "@mui/icons-material";
import { Problem } from "../../types/Problem";

interface ProblemInputFormComponentProps {
    problem: Problem;
}

export const ProblemInputFormComponent: FC<ProblemInputFormComponentProps> = ({problem}) => {
    const [result, setResult] = useState("")

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        console.log(`${problem.id}: ${result}`);
    }

    return (
        <Paper component="form" sx={{ p: '2px 4px', display: 'flex', width: "-webkit-fill-available" }}>
            <InputBase
                sx={{ ml: 1, flex: 1 }}
                placeholder="Input your result"
                inputProps={{ 'aria-label': 'input result here' }}
                value={ result }
                onChange={event => setResult(event.target.value)}
            />
            <Divider sx={{ height: 28, m: 0.5 }} orientation="vertical" />
            <IconButton
                type="button"
                sx={{ p: "10px" }}
                aria-label="submit"
                color="primary"
                size="small"
                onClick={ handleSubmit }
                disabled
            >
                <Send fontSize="inherit"/>
            </IconButton>
        </Paper>
    );
}
