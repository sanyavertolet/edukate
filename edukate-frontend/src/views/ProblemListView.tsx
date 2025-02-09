import { useEffect, useState } from 'react';
import {Box, Typography} from '@mui/material';
import Grid from "@mui/material/Grid2"
import { ProblemMetadata } from '../types/ProblemMetadata';
import ProblemListComponent from '../components/ProblemListComponent.tsx';

export default function ProblemListView() {
    const [problems, setProblems] = useState<ProblemMetadata[]>();

    useEffect(() => {
        fetch('/api/v1/problems')
            .then((response) => response.json())
            .catch((error) => {
                console.error('Error fetching problem metadata:', error);
            })
            .then((data: ProblemMetadata[]) => {
                setProblems(data);
            });
    }, []);

    return (
            <Box>
                <Typography variant={"h3"}>Problems</Typography>
                <Grid container spacing={2}>
                    <Grid size={"grow"}/>
                    <Grid size={7}  display={"flex"} flexDirection={"column"} height={"75vh"}>
                        <ProblemListComponent problemList={problems} />
                    </Grid>
                    <Grid size={"grow"}/>
                </Grid>
            </Box>
    );
}
