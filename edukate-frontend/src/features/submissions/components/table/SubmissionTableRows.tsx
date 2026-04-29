import { FC } from "react";
import { TableRow, TableCell, Skeleton, Box, Tooltip, SxProps, Theme } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { Submission, SubmissionStatus } from "@/features/submissions/types";
import { formatRelative } from "@/shared/utils/date";
import { BookChip } from "@/shared/components/BookChip";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";

type SubmissionTableRowsProps = {
    items: Submission[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (submission: Submission) => void;
    onBookSlugClick: (bookSlug: string) => void;
    onUserNameClick: (userName: string) => void;
};

function SubmissionStatusIcon({ status }: { status: SubmissionStatus }) {
    switch (status) {
        case "SUCCESS":
            return (
                <Tooltip title="Success" slotProps={defaultTooltipSlotProps}>
                    <CheckCircleOutlineIcon color="success" />
                </Tooltip>
            );
        case "PENDING":
            return (
                <Tooltip title="Pending" slotProps={defaultTooltipSlotProps}>
                    <HourglassEmptyIcon color="warning" />
                </Tooltip>
            );
        case "FAILED":
            return (
                <Tooltip title="Failed" slotProps={defaultTooltipSlotProps}>
                    <CancelOutlinedIcon color="error" />
                </Tooltip>
            );
    }
}

const COLUMN_COUNT = 5;
/** Per-column responsive visibility: Updated (4) hidden below md */
const columnVisibility: Record<number, SxProps<Theme>> = {
    4: { display: { xs: "none", md: "table-cell" } },
};

export const SubmissionTableRows: FC<SubmissionTableRowsProps> = ({
    items,
    loading,
    error,
    onRowClick,
    onBookSlugClick,
    onUserNameClick,
}) => {
    if (loading || error) {
        return (
            <>
                {Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={`placeholder-${String(i)}`}>
                        {Array.from({ length: COLUMN_COUNT }).map((_, j) => (
                            <TableCell key={`cell-${String(j)}`} sx={columnVisibility[j]}>
                                <Skeleton variant="rounded" />
                            </TableCell>
                        ))}
                    </TableRow>
                ))}
            </>
        );
    }

    return (
        <>
            {items?.map((item) => (
                <TableRow
                    key={item.id}
                    hover
                    tabIndex={0}
                    sx={{ cursor: "pointer" }}
                    onClick={() => {
                        onRowClick(item);
                    }}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") onRowClick(item);
                    }}
                >
                    <TableCell align="center">
                        <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                            <SubmissionStatusIcon status={item.status} />
                        </Box>
                    </TableCell>
                    <TableCell>
                        <BookChip bookSlug={item.problemKey.split("/")[0]} onClick={onBookSlugClick} />
                    </TableCell>
                    <TableCell>{item.problemKey.split("/").slice(1).join("/")}</TableCell>
                    <TableCell>
                        <Box
                            component="span"
                            sx={{
                                color: "primary.main",
                                cursor: "pointer",
                                "&:hover": { textDecoration: "underline" },
                            }}
                            onClick={(e) => {
                                e.stopPropagation();
                                onUserNameClick(item.userName);
                            }}
                        >
                            {item.userName}
                        </Box>
                    </TableCell>
                    <TableCell sx={columnVisibility[4]}>{formatRelative(item.updatedAt)}</TableCell>
                </TableRow>
            ))}
        </>
    );
};
