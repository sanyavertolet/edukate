import { FC } from "react";
import { TableRow, TableCell, Skeleton, Stack, SxProps, Theme } from "@mui/material";
import { useTranslation } from "react-i18next";
import { ProblemMetadata } from "@/features/problems/types";
import { ProblemStatusIcon } from "@/features/problems/components/ProblemStatusIcon";
import { TagChip } from "@/shared/components/TagChip";
import { BookLabel } from "@/shared/components/BookLabel";
import { formatRelative } from "@/shared/utils/date";

type ProblemTableRowsProps = {
    items: ProblemMetadata[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (key: string) => void;
    onBookSlugClick: (slug: string) => void;
};

/** Per-column responsive visibility: Tags (3) hidden below md, Created (4) hidden on xs only */
const columnVisibility: Record<number, SxProps<Theme>> = {
    3: { display: { xs: "none", md: "table-cell" } },
    4: { display: { xs: "none", sm: "table-cell" } },
};

export const ProblemTableRows: FC<ProblemTableRowsProps> = ({ items, loading, error, onRowClick, onBookSlugClick }) => {
    const { i18n } = useTranslation();
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
                        <TableCell sx={columnVisibility[3]}>
                            <Skeleton variant="rounded" />
                        </TableCell>
                        <TableCell sx={columnVisibility[4]}>
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
                        <BookLabel bookSlug={item.bookSlug} onClick={onBookSlugClick} />
                    </TableCell>
                    <TableCell>
                        {item.code}
                        {item.isHard && " ★"}
                    </TableCell>
                    <TableCell sx={columnVisibility[3]}>
                        <Stack direction={{ xs: "column", md: "row" }} spacing={{ xs: 0.5, md: 1 }}>
                            {item.tags.map((tag) => (
                                <TagChip key={`${item.key}-${tag}`} label={tag} />
                            ))}
                        </Stack>
                    </TableCell>
                    <TableCell sx={columnVisibility[4]}>{formatRelative(item.createdAt, i18n.language)}</TableCell>
                </TableRow>
            ))}
        </>
    );
};
