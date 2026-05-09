import { Box, Tab, Tabs } from "@mui/material";
import React, { useState } from "react";
import { LazyLatexComponent } from "@/shared/components/LazyLatexComponent";
import { Subproblem } from "@/features/problems/types";

interface SubproblemsComponentProps {
    subproblems?: Subproblem[];
}

export function SubproblemsComponent({ subproblems }: SubproblemsComponentProps) {
    const subproblemIds = subproblems?.map((subproblem) => subproblem.code);
    const subproblemMap = subproblems && Object.fromEntries(subproblems.map(({ code, text }) => [code, text]));

    const [currentTabIndex, setCurrentTabIndex] = useState(0);

    const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
        setCurrentTabIndex(newValue);
    };

    if (subproblemIds == null || subproblemIds.length === 0) {
        return null;
    }

    return (
        <Box>
            <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
                <Tabs
                    value={currentTabIndex}
                    onChange={handleTabChange}
                    aria-label="subproblem tabs"
                    textColor="secondary"
                    indicatorColor="secondary"
                    centered
                >
                    {subproblemIds.map((subproblemId, index) => (
                        <Tab key={subproblemId} value={index} label={subproblemId} />
                    ))}
                </Tabs>
            </Box>

            {subproblemMap && (
                <Box sx={{ padding: "2rem" }}>
                    <LazyLatexComponent text={subproblemMap[subproblemIds[currentTabIndex]]} />
                </Box>
            )}
        </Box>
    );
}
