import { Box, Button, Container, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleListComponent } from "../components/bundle/BundleListComponent";
import { useNavigate } from "react-router-dom";
import AddIcon from '@mui/icons-material/Add';

export default function BundleListView() {
    const navigate = useNavigate();
    return (
        <Box>
            <Container>
                <Box sx={{
                    display: "flex", justifyContent: "center", alignItems: "center", position: "relative"
                }}>
                    <Typography color="primary" variant="h5">
                        Problem bundles
                    </Typography>
                    <Button
                        variant={"outlined"}
                        size={"small"}
                        color={"secondary"}
                        onClick={() => navigate("/bundles/new")}
                        sx={{ position: "absolute", right: 0 }}
                    >
                        <AddIcon/>
                        <Typography variant={"button"} sx={{ display: { xs: "none", sm: "flex" } }}>Create bundle</Typography>
                    </Button>
                </Box>
            </Container>

            <Grid container spacing={ 2 } >
                <Grid key={"left-grid"} size="grow" sx={{ display: { xs: "none", md: "flex" }}}/>
                <Grid key={"central-grid"} size={ 12 } display="flex" flexDirection="column">
                    <BundleListComponent/>
                </Grid>
                <Grid key={"right-grid"} size="grow" sx={{ display: { xs: "none", md: "flex" }}}/>
            </Grid>
        </Box>
    );
}
