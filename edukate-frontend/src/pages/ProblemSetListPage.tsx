import { Box, Container, Tab, Tabs, Typography } from "@mui/material";
import { ProblemSetCategoryList } from "@/features/problem-sets/components/ProblemSetCategoryList";
import { ProblemSetToolbar } from "@/features/problem-sets/components/ProblemSetToolbar";
import { ProblemSetWelcomeBanner } from "@/features/problem-sets/components/ProblemSetWelcomeBanner";
import { SyntheticEvent, useState } from "react";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { useAuthContext } from "@/features/auth/context";
import { AuthRequired } from "@/features/auth/components/AuthRequired";
import { useTranslation } from "react-i18next";

export default function ProblemSetListPage() {
    const { isAuthorized } = useAuthContext();
    const { t: tNav } = useTranslation("navigation");
    const { t: tPs } = useTranslation("problem-sets");
    const [tab, setTab] = useState<ProblemSetCategory>("public");

    const onTabChange = (_: SyntheticEvent, newValue: ProblemSetCategory) => {
        setTab(newValue);
    };

    return (
        <Box>
            <Container>
                <Typography component="h1" color="primary" variant="h5" align="center">
                    {tNav("problem_sets")}
                </Typography>

                <Box pt={2}>
                    {!isAuthorized && <ProblemSetWelcomeBanner />}

                    <ProblemSetToolbar disabled={!isAuthorized} />

                    <Tabs value={tab} onChange={onTabChange} centered sx={{ mt: 2 }}>
                        <Tab value={"public"} label={tPs("tab_public")} />
                        <Tab value={"joined"} label={tPs("tab_joined")} />
                        <Tab value={"owned"} label={tPs("tab_owned")} />
                    </Tabs>
                </Box>
            </Container>

            <Container sx={{ pt: 2 }}>
                {tab === "public" ? (
                    <ProblemSetCategoryList tab={tab} onTabSwitch={setTab} />
                ) : (
                    <AuthRequired>
                        <ProblemSetCategoryList tab={tab} onTabSwitch={setTab} />
                    </AuthRequired>
                )}
            </Container>
        </Box>
    );
}
