import { FC } from 'react';
import { TableRow, TableCell, Skeleton, Stack } from '@mui/material';
import { ProblemMetadata } from '../../../types/problem/ProblemMetadata';
import { ProblemStatusIcon } from '../ProblemStatusIcon';
import { TagChip } from '../../basic/TagChip';

type ProblemTableRowsProps = {
    items: ProblemMetadata[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (name: string) => void;
};

export const ProblemTableRows: FC<ProblemTableRowsProps> = ({ items, loading, error, onRowClick }) => {
    if (loading || error) {
        return (
            <>
                {Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={`placeholder-${i}`}>
                        <TableCell><Skeleton variant="rounded" /></TableCell>
                        <TableCell><Skeleton variant="rounded" /></TableCell>
                        <TableCell><Skeleton variant="rounded" /></TableCell>
                    </TableRow>
                ))}
            </>
        );
    }

    return (
        <>
            {items?.map((item) => (
                <TableRow
                    key={item.name}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() => onRowClick(item.name)}
                >
                    <TableCell><ProblemStatusIcon status={item.status} /></TableCell>
                    <TableCell>{item.name}{item.isHard ? '*' : ''}</TableCell>
                    <TableCell>
                        <Stack direction={{ xs: 'column', md: 'row' }} spacing={{ xs: 0.5, md: 1 }}>
                            {item.tags.map((tag) => (
                                <TagChip key={`${item.name}-${tag}`} label={tag} />
                            ))}
                        </Stack>
                    </TableCell>
                </TableRow>
            ))}
        </>
    );
};
