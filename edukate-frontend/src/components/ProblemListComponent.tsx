import { ProblemMetadata } from '../types/ProblemMetadata';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Skeleton, Box } from '@mui/material';
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useProblemListRequest } from "../http/requests";

export default function ProblemListComponent() {
    const [ problemList, setProblemList ] = useState<ProblemMetadata[]>();
    const { data, isLoading, error } = useProblemListRequest();
    const navigate = useNavigate();

    const navigateToProblem = (problemName: string) => {navigate(`/problems/${problemName}`)};

    useEffect(() => { if (data && !isLoading && !error) { setProblemList(data) }}, [data, isLoading, error]);
    
    const tableRows = problemList?.map((item) => (
        <TableRow
            key={ item.name }
            sx={{ cursor: 'pointer', textcolor: '#ffffff' }}
            onClick={navigateToProblem.bind(null, item.name)}
            hover
        >
            <TableCell>{ item.name }</TableCell>
        </TableRow>
    ));

    const tableRowsPlaceholder = Array(5).fill(0).map((_, i) => (
       <TableRow key={ i }>
            <TableCell>
                <Skeleton variant="text" />
            </TableCell>
       </TableRow>
    ));

    return (
        <Box>
            <TableContainer component={ Paper }>
                <Table sx={{ minWidth: 650 }} size="small" aria-label="problem table">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>{ !isLoading && !error ? tableRows : tableRowsPlaceholder }</TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
}
