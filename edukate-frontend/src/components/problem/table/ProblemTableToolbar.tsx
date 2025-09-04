import { FC, ReactNode } from 'react';
import { Toolbar, Box, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { ProblemStatus } from '../../../types/problem/ProblemMetadata';

export type StatusFilter = ProblemStatus | 'ALL' | undefined;

type Props = {
    status: StatusFilter;
    onStatusChange: (status: StatusFilter) => void;
    prefix: string;
    onPrefixChange: (prefix: string) => void;
    rightSlot?: ReactNode;
};

export const ProblemTableToolbar: FC<Props> = ({ status, onStatusChange, prefix, onPrefixChange, rightSlot }) => {
    return (
        <Toolbar sx={{ display: 'flex', gap: 2, justifyContent: 'space-between', flexWrap: 'wrap' }}>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
                <FormControl disabled size="small" sx={{ minWidth: 160 }}>
                    <InputLabel size={"small"} id="status-filter-label">Status</InputLabel>
                    <Select
                        labelId="status-filter-label"
                        size={"small"}
                        label="Status"
                        value={status ?? 'ALL'}
                        onChange={(e) => onStatusChange((e.target.value || 'ALL') as StatusFilter)}
                    >
                        <MenuItem value="ALL">All</MenuItem>
                        <MenuItem value="SOLVED">Solved</MenuItem>
                        <MenuItem value="SOLVING">Solving</MenuItem>
                        <MenuItem value="FAILED">Failed</MenuItem>
                        <MenuItem value="NOT_SOLVED">Not solved</MenuItem>
                    </Select>
                </FormControl>

                <TextField
                    label="Search by prefix"
                    size="small"
                    value={prefix}
                    disabled
                    onChange={(e) => onPrefixChange(e.target.value)}
                />
            </Box>

            <Box sx={{ marginLeft: 'auto' }}>
                {rightSlot}
            </Box>
        </Toolbar>
    );
};
