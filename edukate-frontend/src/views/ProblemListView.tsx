import { Box, Typography } from '@mui/material';
import Grid from "@mui/material/Grid2";
import ProblemListComponent from '../components/ProblemListComponent';

export default function ProblemListView() {
    return (
        <Box>
            <Typography color="primary" variant="h5">
                Problems
            </Typography>

            <Grid container spacing={ 2 }>
                <Grid key={"left-grid"} size="grow"/>
                <Grid key={"central-grid"} size={ 10 } display="flex" flexDirection="column">
                    <ProblemListComponent/>
                </Grid>
                <Grid key={"right-grid"} size="grow"/>
            </Grid>
        </Box>
    );
};
