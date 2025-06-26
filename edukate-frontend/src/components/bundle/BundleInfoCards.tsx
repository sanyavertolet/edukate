import { Box, Card, CardContent, Container, Stack, Typography } from "@mui/material";
import { FC } from "react";

const HowToJoinCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography color={"primary"} variant="h4">
                    Hit the road!
                </Typography>
                <Typography>
                    To join a bundle, simply enter its share code in the join field at the top right of the page.
                    Ready to create your own?
                    Click the "+" button to get started!
                </Typography>
            </CardContent>
        </Card>
    );
};

const JoinedCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography color={"primary"} variant="h4">
                    Joined
                </Typography>
                <Typography>
                    This tab shows all bundles you've joined using a share code.
                    These are collections created by others that you now have access to.
                    Perfect for students accessing teacher-assigned problem sets or collaborative study groups.
                </Typography>
            </CardContent>
        </Card>
    );
};

const OwnedCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography color={"primary"} variant="h4">
                    Owned
                </Typography>
                <Typography>
                    Here you'll find all bundles you've created.
                    As the owner, you have administrative privileges to manage these bundles, including adding/removing problems and controlling access permissions.
                </Typography>
            </CardContent>
        </Card>
    );
};

const PublicCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography color={"primary"} variant="h4">
                    Public
                </Typography>
                <Typography>
                    Browse publicly available bundles created by the community.
                    These are open collections that anyone can access without needing a share code.
                    Discover new problem sets and learning resources shared by others.
                </Typography>
            </CardContent>
        </Card>
    );
};

const WhatIsBundleCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography variant={"h4"} color={"primary"}>
                    Bundle: Your Personalized Problem Collection
                </Typography>
                <Typography>
                    Bundles are curated collections of problems that allow you to organize, share, and collaborate on educational content.
                    Think of them as customized problem sets that can be used for homework assignments, study groups, or personal practice.
                    Each bundle has a unique share code, making it easy to distribute to students or colleagues.
                </Typography>
            </CardContent>
        </Card>
    );
}

const WhyUseBundlesCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography variant={"h4"} color={"primary"}>
                    Why use Bundles?
                </Typography>
                <Typography textAlign={"start"}>
                    - <b>For Teachers</b>: Create homework assignments or practice sets for your students. Track which problems are included and control who has access.
                </Typography>
                <Typography textAlign={"start"}>
                    - <b>For Students</b>: Join your teacher's bundles to access assigned problems or create your own study collections.
                </Typography>
                <Typography textAlign={"start"}>
                    - <b>For Self-learners</b>: Organize problems by topic, difficulty, or learning path to structure your practice.
                </Typography>
            </CardContent>
        </Card>
    );
};

export const BundleInfoCards = () => {
    return (
        <Container maxWidth={"lg"} sx={{justifyContent: "center"}}>
            <Box >
                <WhatIsBundleCard/>
            </Box>

            <Box paddingTop={3}>
                <WhyUseBundlesCard/>
            </Box>

            <Stack spacing={2} direction={"row"} paddingTop={3}>
                    <JoinedCard/>
                    <OwnedCard/>
                    <PublicCard/>
            </Stack>

            <Box paddingTop={3}>
                <HowToJoinCard/>
            </Box>
        </Container>
    )
};
