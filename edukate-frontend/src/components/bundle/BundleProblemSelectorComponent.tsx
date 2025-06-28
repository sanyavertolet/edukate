import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from "@mui/material";
import { ProblemMetadata } from "../../types/problem/ProblemMetadata";
import { ProblemStatusIcon } from "../problem/ProblemStatusIcon";
import { useDeviceContext, usePageSpecificNavigation } from "../topbar/DeviceContextProvider";
import { AdditionalNavigationElement } from "../topbar/AdditionalNavigationElement";
import { useMemo } from "react";

interface BundleProblemSelectorComponentProps {
    bundleName: string;
    problems: ProblemMetadata[];
    onProblemSelect: (problem?: ProblemMetadata) => void;
    selectedProblem?: ProblemMetadata;
}

export function BundleProblemSelectorComponent(
    {problems, bundleName, onProblemSelect, selectedProblem} : BundleProblemSelectorComponentProps
) {
    const { isMobile } = useDeviceContext();

    // noinspection com.intellij.reactbuddy.ExhaustiveDepsInspection
    const pageSpecificNavigation: AdditionalNavigationElement[] = useMemo(() => [
        { text: bundleName, onClick: () => onProblemSelect(undefined), isSelected: selectedProblem === undefined },
        ...(problems.map(problem => (
            { text: problem.name, onClick: () => onProblemSelect(problem), isSelected: problem.name == selectedProblem?.name }
        )))
    ], [problems, selectedProblem]);
    usePageSpecificNavigation(pageSpecificNavigation);

    if (!isMobile) {
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
                    {problems.map((problem) => (
                        <ListItemButton key={problem.name} selected={problem.name == selectedProblem?.name}
                                        onClick={() => onProblemSelect(problem)}>
                            <ListItemIcon>
                                <ProblemStatusIcon status={problem.status}/>
                            </ListItemIcon>
                            <ListItemText primary={problem.name}/>
                        </ListItemButton>
                    ))}
                </List>
            </Box>
        );
    }
}
