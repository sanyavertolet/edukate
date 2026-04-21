import { ProblemSet } from "@/features/problem-sets/types";
import { Box, Card, Divider, Tab, Tabs } from "@mui/material";
import { useAuthContext } from "@/features/auth/context";
import React, { FC, useMemo, useState } from "react";
import { ProblemSetSettingsTab } from "./ProblemSetSettingsTab";
import { ProblemSetDescriptionTab } from "./ProblemSetDescriptionTab";

interface ProblemSetIndexCardProps {
    problemSet?: ProblemSet;
}

type ProblemSetIndexTabs = "description" | "settings";

export const ProblemSetIndexCard: FC<ProblemSetIndexCardProps> = ({ problemSet }: ProblemSetIndexCardProps) => {
    const [tab, setTab] = useState<ProblemSetIndexTabs>("description");
    const { user } = useAuthContext();
    const isAdmin = useMemo(
        () => (user && problemSet && problemSet.admins.find((value) => value == user.name)) || false,
        [user, problemSet],
    );
    const onTabChange = (_: React.SyntheticEvent, newValue: ProblemSetIndexTabs) => {
        setTab(newValue);
    };
    return (
        <Card>
            <Box display="flex" flexDirection="column" alignItems="center">
                <Tabs value={tab} onChange={onTabChange} color={"primary"}>
                    <Tab key={"description"} value={"description"} label={"Description"} />
                    <Tab key={"settings"} value={"settings"} label={"Settings"} disabled={!isAdmin} />
                </Tabs>
                <Divider />
                {problemSet && tab == "description" && <ProblemSetDescriptionTab problemSet={problemSet} />}
                {problemSet && tab == "settings" && <ProblemSetSettingsTab problemSet={problemSet} />}
            </Box>
        </Card>
    );
};
