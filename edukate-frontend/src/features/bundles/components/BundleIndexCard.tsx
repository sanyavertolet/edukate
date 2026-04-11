import { Bundle } from "@/features/bundles/types";
import { Box, Card, Divider, Tab, Tabs } from "@mui/material";
import { useAuthContext } from "@/features/auth/context";
import React, { FC, useMemo, useState } from "react";
import { BundleSettingsTab } from "./BundleSettingsTab";
import { BundleDescriptionTab } from "./BundleDescriptionTab";

interface BundleIndexCardProps {
    bundle?: Bundle;
}

type BundleIndexTabs = "description" | "settings";

export const BundleIndexCard: FC<BundleIndexCardProps> = ({ bundle }: BundleIndexCardProps) => {
    const [tab, setTab] = useState<BundleIndexTabs>("description");
    const { user } = useAuthContext();
    const isAdmin = useMemo(
        () => (user && bundle && bundle.admins.find((value) => value == user.name)) || false,
        [user, bundle],
    );
    const onTabChange = (_: React.SyntheticEvent, newValue: BundleIndexTabs) => {
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
                {bundle && tab == "description" && <BundleDescriptionTab bundle={bundle} />}
                {bundle && tab == "settings" && <BundleSettingsTab bundle={bundle} />}
            </Box>
        </Card>
    );
};
