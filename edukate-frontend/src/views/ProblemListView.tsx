import { Box, Typography } from '@mui/material';
import Grid from "@mui/material/Grid2";
import ProblemListComponent from '../components/ProblemListComponent';

export default function ProblemListView() {
    return (
        <Box>
            <Typography marginTop="2rem" color="primary" variant="h5">Problems</Typography>
            <Grid container spacing={ 2 } marginTop="0.5rem">
                <Grid key={"left-grid"} size="grow"/>
                <Grid key={"central-grid"} size={ 10 }  display="flex" flexDirection="column" height="75vh">
                    <ProblemListComponent/>
                </Grid>
                <Grid key={"right-grid"} size="grow"/>
            </Grid>
        </Box>
    );
};
