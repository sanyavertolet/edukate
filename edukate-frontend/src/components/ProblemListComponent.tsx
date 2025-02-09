import { ProblemMetadata } from '../types/ProblemMetadata';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Skeleton, Box, Typography } from '@mui/material';

interface ProblemListComponentProps {
    problemList: ProblemMetadata[] | undefined;
}

export default function ProblemListComponent({ problemList }: ProblemListComponentProps) {
    const tableRows = problemList?.map((item) => (
        <TableRow key={item.name}>
            <TableCell>{item.name}</TableCell>
        </TableRow>
    ));

    return (
        <Box>
            <Typography fontSize={20}>Problem List</Typography>
            {problemList ? (
                <TableContainer component={Paper}>
                    <Table sx={{ minWidth: 650 }} aria-label="problem table">
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>{tableRows}</TableBody>
                    </Table>
                </TableContainer>
            ) : (
                <Skeleton variant="rectangular" height={"10vh"} sx={{padding: 10}}/>
            )}
        </Box>
    );
}
