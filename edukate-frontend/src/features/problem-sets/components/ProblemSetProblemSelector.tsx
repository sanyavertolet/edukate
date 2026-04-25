import { Box, Divider, List, ListItemButton, ListItemIcon, ListItemText } from "@mui/material";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { useDeviceContext, usePageSpecificNavigation } from "@/shared/context/DeviceContext";
import { AdditionalNavigationElement } from "@/shared/context/DeviceContext";
import { useMemo } from "react";

export type ProblemSetSelection =
    | { type: "description" }
    | { type: "settings" }
    | { type: "problem"; problem: ProblemMetadata };

interface ProblemSetProblemSelectorProps {
    problems: ProblemMetadata[];
    selection: ProblemSetSelection;
    onSelectionChange: (selection: ProblemSetSelection) => void;
    isAdmin: boolean;
}

export function ProblemSetProblemSelector({
    problems,
    selection,
    onSelectionChange,
    isAdmin,
}: ProblemSetProblemSelectorProps) {
    const { isMobile } = useDeviceContext();

    const pageSpecificNavigation: AdditionalNavigationElement[] = useMemo(
        () => [
            {
                text: "Description",
                onClick: () => {
                    onSelectionChange({ type: "description" });
                },
                isSelected: selection.type === "description",
            },
            ...(isAdmin
                ? [
                      {
                          text: "Settings",
                          onClick: () => {
                              onSelectionChange({ type: "settings" });
                          },
                          isSelected: selection.type === "settings",
                      },
                  ]
                : []),
            ...problems.map((problem) => ({
                text: problem.code,
                onClick: () => {
                    onSelectionChange({ type: "problem", problem });
                },
                isSelected: selection.type === "problem" && problem.code === selection.problem.code,
            })),
        ],
        [problems, selection, onSelectionChange, isAdmin],
    );
    usePageSpecificNavigation(pageSpecificNavigation);

    if (!isMobile) {
        return (
            <Box>
                <List disablePadding>
                    <ListItemButton
                        selected={selection.type === "description"}
                        onClick={() => {
                            onSelectionChange({ type: "description" });
                        }}
                    >
                        <ListItemIcon>
                            <InfoOutlinedIcon />
                        </ListItemIcon>
                        <ListItemText primary="Description" />
                    </ListItemButton>
                    {isAdmin && (
                        <ListItemButton
                            selected={selection.type === "settings"}
                            onClick={() => {
                                onSelectionChange({ type: "settings" });
                            }}
                        >
                            <ListItemIcon>
                                <SettingsOutlinedIcon />
                            </ListItemIcon>
                            <ListItemText primary="Settings" />
                        </ListItemButton>
                    )}
                </List>
                <Divider />
                <List disablePadding>
                    {problems.map((problem) => (
                        <ListItemButton
                            key={problem.code}
                            selected={selection.type === "problem" && problem.code === selection.problem.code}
                            onClick={() => {
                                onSelectionChange({ type: "problem", problem });
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
