import { Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import ProblemListComponent from "@/features/problems/components/ProblemListComponent";

export default function ProblemListPage() {
    return (
        <Container>
            <Typography component="h1" color="primary" variant="h5" align="center">
                Problems
            </Typography>

            <Grid container spacing={2} paddingTop={"1rem"}>
                <Grid key={"left-grid"} size="grow" sx={{ display: { xs: "none", md: "flex" } }} />
                <Grid key={"central-grid"} size={12} display="flex" flexDirection="column">
                    <ProblemListComponent />
                </Grid>
                <Grid key={"right-grid"} size="grow" sx={{ display: { xs: "none", md: "flex" } }} />
            </Grid>
        </Container>
    );
}
