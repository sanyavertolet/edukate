import { Box, Container, Snackbar, Tab, Tabs } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleCard } from "./BundleCard";
import { useBundlesRequest } from "../../http/requests";
import { SyntheticEvent, useEffect, useState } from "react";
import { BundleMetadata } from "../../types/BundleMetadata";
import { BundleJoinForm } from "./BundleJoinForm";

type TabType = "owned" | "public" | "joined";

export function BundleListComponent() {
    const [tab, setTab] = useState<TabType>("joined");

    const { data, isLoading, error } = useBundlesRequest(tab);
    const [bundles, setBundles] = useState<BundleMetadata[]>([]);
    useEffect(() => { if (data && !isLoading && !error) { setBundles(data); } }, [data, isLoading, error, tab]);

    const onTabChange = (_: SyntheticEvent, newValue: TabType) => { setTab(newValue); }

    const [open, setOpen] = useState(false);
    return (
        <Box>
            <Box sx={{ position: "absolute", right: "1rem" }}>
                <BundleJoinForm/>
            </Box>
            <Container>
                <Tabs value={tab} onChange={onTabChange} centered>
                    <Tab value={"joined"} label="Joined" />
                    <Tab value={"owned"} label="Owned" />
                    <Tab value={"public"} label="Public"/>
                </Tabs>
            </Container>

            <Grid sx={{ paddingTop: "1rem" }} container rowSpacing={ 1 } spacing={ 2 }>
                {bundles.map((bundle) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3}}>
                        <BundleCard bundleMetadata={bundle} onCopy={() => setOpen(true)}/>
                    </Grid>
                ))}
            </Grid>

            <Snackbar
                open={open}
                autoHideDuration={1000}
                onClose={() => setOpen(false)}
                message={"Copied to clipboard."}
            />
        </Box>
    );
}
