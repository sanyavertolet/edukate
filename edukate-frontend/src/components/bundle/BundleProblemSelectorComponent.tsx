import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from "@mui/material";
import { ProblemMetadata } from "../../types/ProblemMetadata";
import { ProblemStatusIcon } from "../problem/ProblemStatusIcon";

interface BundleProblemSelectorComponentProps {
    bundleName: string;
    problems: ProblemMetadata[];
    onProblemSelect: (problem?: ProblemMetadata) => void;
    selectedProblem?: ProblemMetadata;
}

export function BundleProblemSelectorComponent(
    {problems, bundleName, onProblemSelect, selectedProblem} : BundleProblemSelectorComponentProps
) {
    return (
        <Box>
            <List>
                <ListItem key={bundleName} disablePadding>
                    <ListItemButton onClick={() => onProblemSelect(undefined)}>
                        <ListItemText primary={bundleName}/>
                    </ListItemButton>
                </ListItem>
            </List>
            <Divider/>
            <List>
                { problems.map((problem) => (
                    <ListItemButton key={problem.name} selected={problem.name == selectedProblem?.name} onClick={() => onProblemSelect(problem)}>
                        <ListItemIcon>
                            <ProblemStatusIcon status={problem.status}/>
                        </ListItemIcon>
                        <ListItemText primary={problem.name} />
                    </ListItemButton>
                ))}
            </List>
        </Box>
    );
}
