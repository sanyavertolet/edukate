import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from "@mui/material";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { useDeviceContext, usePageSpecificNavigation } from "@/shared/context/DeviceContext";
import { AdditionalNavigationElement } from "@/shared/context/DeviceContext";
import { useMemo } from "react";

interface ProblemSetProblemSelectorProps {
    problemSetName: string;
    problems: ProblemMetadata[];
    onProblemSelect: (problem?: ProblemMetadata) => void;
    selectedProblem?: ProblemMetadata;
}

export function ProblemSetProblemSelector({
    problems,
    problemSetName,
    onProblemSelect,
    selectedProblem,
}: ProblemSetProblemSelectorProps) {
    const { isMobile } = useDeviceContext();

    const pageSpecificNavigation: AdditionalNavigationElement[] = useMemo(
        () => [
            {
                text: problemSetName,
                onClick: () => {
                    onProblemSelect(undefined);
                },
                isSelected: selectedProblem === undefined,
            },
            ...problems.map((problem) => ({
                text: problem.code,
                onClick: () => {
                    onProblemSelect(problem);
                },
                isSelected: problem.code == selectedProblem?.code,
            })),
        ],
        [problems, selectedProblem],
    );
    usePageSpecificNavigation(pageSpecificNavigation);

    if (!isMobile) {
        return (
            <Box>
                <List>
                    <ListItem key={problemSetName} disablePadding>
                        <ListItemButton
                            onClick={() => {
                                onProblemSelect(undefined);
                            }}
                        >
                            <ListItemText primary={problemSetName} />
                        </ListItemButton>
                    </ListItem>
                </List>
                <Divider />
                <List>
                    {problems.map((problem) => (
                        <ListItemButton
                            key={problem.code}
                            selected={problem.code == selectedProblem?.code}
                            onClick={() => {
                                onProblemSelect(problem);
                            }}
                        >
                            <ListItemIcon>
                                <ProblemStatusIcon status={problem.status} />
                            </ListItemIcon>
                            <ListItemText primary={problem.code} />
                        </ListItemButton>
                    ))}
                </List>
            </Box>
        );
    }
}
