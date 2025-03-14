import { Box, Container, Tab, Tabs } from "@mui/material";
import React, { useState } from "react";
import { LatexComponent } from "../LatexComponent";

interface SubtasksComponentProps {
    subtasks: { id: string, text: string }[] | null
}

export function SubtasksComponent({subtasks}: SubtasksComponentProps) {
    const subtaskIds = subtasks?.map(subtask => subtask.id);
    const subtaskMap = subtasks && Object.fromEntries(subtasks.map(({id, text}) => [id, text]));

    const [currentTabIndex, setCurrentTabIndex] = useState(0);

    const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
        setCurrentTabIndex(newValue);
    };

    return ( subtaskIds &&
        <Container>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={currentTabIndex}
                      onChange={handleTabChange}
                      aria-label="subtask tabs"
                      textColor="secondary"
                      indicatorColor="secondary"
                      centered
                >
                    { subtaskIds.map((subtaskId, index) =>
                        <Tab key={subtaskId} value={index} label={subtaskId}/>
                    )}
                </Tabs>
            </Box>

            { subtaskMap &&
                <Box sx={{ padding: "2rem" }}>
                    <LatexComponent text={subtaskMap[subtaskIds[currentTabIndex]]}/>
                </Box>
            }
        </Container>
    );
}
