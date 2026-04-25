import { Box, Container, Tab, Tabs, Typography } from "@mui/material";
import { ProblemSetCategoryList } from "@/features/problem-sets/components/ProblemSetCategoryList";
import { ProblemSetToolbar } from "@/features/problem-sets/components/ProblemSetToolbar";
import { ProblemSetWelcomeBanner } from "@/features/problem-sets/components/ProblemSetWelcomeBanner";
import { SyntheticEvent, useState } from "react";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { useAuthContext } from "@/features/auth/context";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

export default function ProblemSetListPage() {
    const { isAuthorized } = useAuthContext();
    const [tab, setTab] = useState<ProblemSetCategory>(isAuthorized ? "owned" : "public");

    const onTabChange = (_: SyntheticEvent, newValue: ProblemSetCategory) => {
        setTab(newValue);
    };

    return (
        <Box>
            <Container>
                <Typography component="h1" color="primary" variant="h5" align="center">
                    Problem Sets
                </Typography>

                <Box pt={2}>
                    {!isAuthorized && <ProblemSetWelcomeBanner />}

                    <ProblemSetToolbar disabled={!isAuthorized} />

                    <Tabs value={tab} onChange={onTabChange} centered sx={{ mt: 2 }}>
                        <Tab value={"joined"} label="Joined" />
                        <Tab value={"owned"} label="Owned" />
                        <Tab value={"public"} label="Public" />
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
