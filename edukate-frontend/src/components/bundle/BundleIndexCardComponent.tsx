import { Bundle } from "../../types/bundle/Bundle";
import { Box, Card, Divider, Tab, Tabs } from "@mui/material";
import { useAuthContext } from "../auth/AuthContextProvider";
import React, { FC, useMemo, useState } from "react";
import { BundleSettingsTabComponent } from "./BundleSettingsTabComponent";
import { BundleDescriptionTabComponent } from "./BundleDescriptionTabComponent";

interface BundleIndexCardComponentProps {
    bundle?: Bundle;
}

type BundleIndexTabs = "description" | "settings";

export const BundleIndexCardComponent: FC<BundleIndexCardComponentProps> = ({bundle}: BundleIndexCardComponentProps) => {
    const [tab, setTab] = useState<BundleIndexTabs>("description");
    const { user } = useAuthContext();
    const isAdmin = useMemo(() => user && bundle && bundle.admins.find((value) => value == user?.name) || false, [user, bundle]);
    const onTabChange = (_: React.SyntheticEvent, newValue: BundleIndexTabs) => {
        setTab(newValue);
    };
    return (
        <Card>
            <Box justifyContent={"center"}>
                <Tabs value={tab} onChange={onTabChange} color={"primary"}>
                    <Tab key={"description"} value={"description"} label={"Description"}/>
                    <Tab key={"settings"} value={"settings"} label={"Settings"} disabled={!isAdmin}/>
                </Tabs>
                <Divider/>
                { bundle && tab == "description" && <BundleDescriptionTabComponent bundle={bundle}/>}
                { bundle && tab == "settings" && <BundleSettingsTabComponent bundle={bundle}/>}
            </Box>
        </Card>
    )
}
