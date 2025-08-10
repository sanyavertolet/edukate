import { Box, Card, CardContent, Container, Stack, Typography } from "@mui/material";
import { FC } from "react";

const HowToJoinCard: FC = () => {
    return (
        <Card>
            <CardContent>
                <Typography color={"primary"} variant="h4" textAlign="center" sx={{ mb: 2 }}>
                    Hit the road!
                </Typography>
                <Typography textAlign="left">
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
                <Typography color={"primary"} variant="h4" textAlign="center" sx={{ mb: 2 }}>
                    Joined
                </Typography>
                <Typography textAlign="left">
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
                <Typography color={"primary"} variant="h4" textAlign="center" sx={{ mb: 2 }}>
                    Owned
                </Typography>
                <Typography textAlign="left">
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
                <Typography color={"primary"} variant="h4" textAlign="center" sx={{ mb: 2 }}>
                    Public
                </Typography>
                <Typography textAlign="left">
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
                <Typography variant={"h4"} color={"primary"} textAlign="center" sx={{ mb: 2 }}>
                    Bundle: Your Personalized Problem Collection
                </Typography>
                <Typography textAlign="left">
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
                <Typography variant={"h4"} color={"primary"} textAlign="center" sx={{ mb: 2 }}>
                    Why use Bundles?
                </Typography>
                <Typography textAlign="left">
                    - <span style={{color: 'purple', fontWeight: 'bold'}}>For Teachers</span>: Create homework assignments or practice sets for your students. Track which problems are included and control who has access.
                </Typography>
                <Typography textAlign="left">
                    - <span style={{color: 'purple', fontWeight: 'bold'}}>For Students</span>: Join your teacher's bundles to access assigned problems or create your own study collections.
                </Typography>
                <Typography textAlign="left">
                    - <span style={{color: 'purple', fontWeight: 'bold'}}>For Self-learners</span>: Organize problems by topic, difficulty, or learning path to structure your practice.
                </Typography>
            </CardContent>
        </Card>
    );
};

export const BundleInfoCards = () => {
    return (
        <Container maxWidth={"lg"} sx={{justifyContent: "flex-start"}}>
            <Box >
                <WhatIsBundleCard/>
            </Box>

            <Box paddingTop={3}>
                <WhyUseBundlesCard/>
            </Box>

            <Stack spacing={{ xs: 3, md: 2 }} direction={{ xs: "column", md: "row" }} paddingTop={3}>
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
