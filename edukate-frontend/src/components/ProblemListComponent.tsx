import { ProblemMetadata } from '../types/ProblemMetadata';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Skeleton, Box, Typography } from '@mui/material';
import { useQuery } from "@tanstack/react-query";
import {useEffect, useState} from "react";

// interface ProblemListComponentProps {
//     problemList: ProblemMetadata[] | undefined;
// }

function useProblemListRequest() {
    const problemsUrl = `${window.location.origin}/api/v1/problems`;
    console.log(problemsUrl);
    return useQuery({
        queryKey: ['problemList'],
        queryFn: async () => {
            const response = await fetch(problemsUrl);
            if (!response.ok) {
                throw new Error(`Error fetching data: ${response.status}`)
            }
            return await response.json() as ProblemMetadata[];
        },
    });
}

export default function ProblemListComponent() {
    const [ problemList, setProblemList ] = useState<ProblemMetadata[]>()
    const { data, isLoading, error } = useProblemListRequest()

    useEffect(() => {
        if (data && !isLoading && !error) {
            setProblemList(data)
        }
    }, [data, isLoading, error]);
    
    const tableRows = problemList?.map((item) => (
        <TableRow key={item.name}>
            <TableCell>{item.name}</TableCell>
        </TableRow>
    ));

    return (
        <Box>
            <Typography fontSize={20}>Problem List</Typography>
            {!isLoading && !error ? (
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
