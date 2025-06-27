import { Box, Container, Tab, Tabs, Typography } from "@mui/material";
import { BundleCategoryListComponent } from "../components/bundle/BundleCategoryListComponent";
import { BundleJoinForm } from "../components/bundle/BundleJoinForm";
import { BundleInfoCards } from "../components/bundle/BundleInfoCards";
import { SyntheticEvent, useState } from "react";
import { BundleCategory } from "../types/Bundle";
import { useAuthContext } from "../components/auth/AuthContextProvider";
import { AuthRequired } from "../components/auth/AuthRequired";

type ExtendedBundleCategory = BundleCategory | "info";

export default function BundleListView() {
    const [tab, setTab] = useState<ExtendedBundleCategory>("info");
    const onTabChange = (_: SyntheticEvent, newValue: ExtendedBundleCategory) => { setTab(newValue); }
    const { isAuthorized } = useAuthContext();
    return (
        <Box>
            <Container>
                <Typography color="primary" variant="h5">
                    Problem bundles
                </Typography>
            </Container>

            <Box>
                <Container>
                    <Box pt={2} width={"100%"} justifySelf={"center"}>
                        <BundleJoinForm disabled={!isAuthorized}/>
                    </Box>
                    <Tabs value={tab} onChange={onTabChange} centered>
                        <Tab key={"info"} value={"info"} label="Info" />
                        <Tab key={"joined"} value={"joined"} label="Joined" />
                        <Tab key={"owned"} value={"owned"} label="Owned" />
                        <Tab key={"public"} value={"public"} label="Public" />
                    </Tabs>
                </Container>

                { tab == "info" && <BundleInfoCards/>}
                { tab != "info" && (
                    <AuthRequired>
                        <BundleCategoryListComponent tab={tab}/>
                    </AuthRequired>
                )}
            </Box>
        </Box>
    );
}
