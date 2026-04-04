import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from "@mui/material";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { useDeviceContext, usePageSpecificNavigation } from "@/shared/context/DeviceContext";
import { AdditionalNavigationElement } from "@/shared/context/DeviceContext";
import { useMemo } from "react";

interface BundleProblemSelectorProps {
    bundleName: string;
    problems: ProblemMetadata[];
    onProblemSelect: (problem?: ProblemMetadata) => void;
    selectedProblem?: ProblemMetadata;
}

export function BundleProblemSelector({
    problems,
    bundleName,
    onProblemSelect,
    selectedProblem,
}: BundleProblemSelectorProps) {
    const { isMobile } = useDeviceContext();

    // noinspection com.intellij.reactbuddy.ExhaustiveDepsInspection
    const pageSpecificNavigation: AdditionalNavigationElement[] = useMemo(
        () => [
            { text: bundleName, onClick: () => onProblemSelect(undefined), isSelected: selectedProblem === undefined },
            ...problems.map((problem) => ({
                text: problem.name,
                onClick: () => onProblemSelect(problem),
                isSelected: problem.name == selectedProblem?.name,
            })),
        ],
        [problems, selectedProblem],
    );
    usePageSpecificNavigation(pageSpecificNavigation);

    if (!isMobile) {
        return (
            <Box>
                <List>
                    <ListItem key={bundleName} disablePadding>
                        <ListItemButton onClick={() => onProblemSelect(undefined)}>
                            <ListItemText primary={bundleName} />
                        </ListItemButton>
                    </ListItem>
                </List>
                <Divider />
                <List>
                    {problems.map((problem) => (
                        <ListItemButton
                            key={problem.name}
                            selected={problem.name == selectedProblem?.name}
                            onClick={() => onProblemSelect(problem)}
                        >
                            <ListItemIcon>
                                <ProblemStatusIcon status={problem.status} />
                            </ListItemIcon>
                            <ListItemText primary={problem.name} />
                        </ListItemButton>
                    ))}
                </List>
            </Box>
        );
    }
}
