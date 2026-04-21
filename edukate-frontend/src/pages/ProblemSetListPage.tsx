import { Box, Container, Tab, Tabs, Typography } from "@mui/material";
import { ProblemSetCategoryList } from "@/features/problem-sets/components/ProblemSetCategoryList";
import { ProblemSetJoinForm } from "@/features/problem-sets/components/ProblemSetJoinForm";
import { ProblemSetInfoCards } from "@/features/problem-sets/components/ProblemSetInfoCards";
import { SyntheticEvent, useState } from "react";
import { ProblemSetCategory } from "@/features/problem-sets/types";
import { useAuthContext } from "@/features/auth/context";
import { AuthRequired } from "@/features/auth/components/AuthRequired";

type ExtendedProblemSetCategory = ProblemSetCategory | "info";

export default function ProblemSetListPage() {
    const [tab, setTab] = useState<ExtendedProblemSetCategory>("info");
    const onTabChange = (_: SyntheticEvent, newValue: ExtendedProblemSetCategory) => {
        setTab(newValue);
    };
    const { isAuthorized } = useAuthContext();
    return (
        <Box>
            <Container>
                <Typography component="h1" color="primary" variant="h5" align="center">
                    Problem Sets
                </Typography>
            </Container>

            <Box>
                <Container>
                    <Box pt={2} width={"100%"}>
                        <ProblemSetJoinForm disabled={!isAuthorized} />
                    </Box>
                    <Tabs value={tab} onChange={onTabChange} centered>
                        <Tab key={"info"} value={"info"} label="Info" />
                        <Tab key={"joined"} value={"joined"} label="Joined" />
                        <Tab key={"owned"} value={"owned"} label="Owned" />
                        <Tab key={"public"} value={"public"} label="Public" />
                    </Tabs>
                </Container>

                {tab == "info" && <ProblemSetInfoCards />}
                {tab != "info" && (
                    <AuthRequired>
                        <ProblemSetCategoryList tab={tab} />
                    </AuthRequired>
                )}
            </Box>
        </Box>
    );
}
