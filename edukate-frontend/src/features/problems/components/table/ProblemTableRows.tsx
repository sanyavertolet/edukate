import { FC } from "react";
import { TableRow, TableCell, Skeleton, Stack, Chip } from "@mui/material";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { TagChip } from "@/shared/components/TagChip";

type ProblemTableRowsProps = {
    items: ProblemMetadata[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (key: string) => void;
    onBookSlugClick: (slug: string) => void;
};

export const ProblemTableRows: FC<ProblemTableRowsProps> = ({
    items,
    loading,
    error,
    onRowClick,
    onBookSlugClick,
}) => {
    if (loading || error) {
        return (
            <>
                {Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={`placeholder-${String(i)}`}>
                        <TableCell>
                            <Skeleton variant="rounded" />
                        </TableCell>
                        <TableCell>
                            <Skeleton variant="rounded" />
                        </TableCell>
                        <TableCell>
                            <Skeleton variant="rounded" />
                        </TableCell>
                        <TableCell>
                            <Skeleton variant="rounded" />
                        </TableCell>
                    </TableRow>
                ))}
            </>
        );
    }

    return (
        <>
            {items?.map((item) => (
                <TableRow
                    key={item.key}
                    hover
                    tabIndex={0}
                    sx={{ cursor: "pointer" }}
                    onClick={() => {
                        onRowClick(item.key);
                    }}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") onRowClick(item.key);
                    }}
                >
                    <TableCell>
                        <ProblemStatusIcon status={item.status} />
                    </TableCell>
                    <TableCell>
                        <Chip
                            label={item.bookSlug}
                            size="small"
                            variant="outlined"
                            onClick={(e) => {
                                e.stopPropagation();
                                onBookSlugClick(item.bookSlug);
                            }}
                        />
                    </TableCell>
                    <TableCell>
                        {item.code}
                        {item.isHard ? "*" : ""}
                    </TableCell>
                    <TableCell>
                        <Stack direction={{ xs: "column", md: "row" }} spacing={{ xs: 0.5, md: 1 }}>
                            {item.tags.map((tag) => (
                                <TagChip key={`${item.key}-${tag}`} label={tag} />
                            ))}
                        </Stack>
                    </TableCell>
                </TableRow>
            ))}
        </>
    );
};
