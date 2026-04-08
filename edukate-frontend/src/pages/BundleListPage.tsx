import { Box, Container, Tab, Tabs, Typography } from "@mui/material";
import { BundleCategoryList } from "@/features/bundles/components/BundleCategoryList";
import { BundleJoinForm } from "@/features/bundles/components/BundleJoinForm";
import { BundleInfoCards } from "@/features/bundles/components/BundleInfoCards";
import { SyntheticEvent, useState } from "react";
import { BundleCategory } from "@/features/bundles/types";
import { useAuthContext } from "@/features/auth/context";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

type ExtendedBundleCategory = BundleCategory | "info";

export default function BundleListPage() {
    const [tab, setTab] = useState<ExtendedBundleCategory>("info");
    const onTabChange = (_: SyntheticEvent, newValue: ExtendedBundleCategory) => {
        setTab(newValue);
    };
    const { isAuthorized } = useAuthContext();
    return (
        <Box>
            <Container>
                <Typography component="h1" color="primary" variant="h5">
                    Problem bundles
                </Typography>
            </Container>

            <Box>
                <Container>
                    <Box pt={2} width={"100%"} justifySelf={"center"}>
                        <BundleJoinForm disabled={!isAuthorized} />
                    </Box>
                    <Tabs value={tab} onChange={onTabChange} centered>
                        <Tab key={"info"} value={"info"} label="Info" />
                        <Tab key={"joined"} value={"joined"} label="Joined" />
                        <Tab key={"owned"} value={"owned"} label="Owned" />
                        <Tab key={"public"} value={"public"} label="Public" />
                    </Tabs>
                </Container>

                {tab == "info" && <BundleInfoCards />}
                {tab != "info" && (
                    <AuthRequired>
                        <BundleCategoryList tab={tab} />
                    </AuthRequired>
                )}
            </Box>
        </Box>
    );
}
