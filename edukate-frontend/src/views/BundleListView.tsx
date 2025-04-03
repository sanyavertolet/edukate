import { Box, Typography } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { BundleListComponent } from "../components/bundle/BundleListComponent";

export default function BundleListView() {
    return (
        <Box>
            <Typography color="primary" variant="h5">
                Problem bundles
            </Typography>

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
