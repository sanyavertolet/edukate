import { FC } from "react";
import { TableRow, TableCell, Skeleton, Chip } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { Submission, SubmissionStatus } from "@/features/submissions/types";
import { formatRelative } from "@/shared/utils/date";

type SubmissionTableRowsProps = {
    items: Submission[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (submission: Submission) => void;
};

function statusIcon(status: SubmissionStatus) {
    switch (status) {
        case "SUCCESS":
            return <CheckCircleOutlineIcon color="success" fontSize="small" />;
        case "PENDING":
            return <HourglassEmptyIcon color="warning" fontSize="small" />;
        case "FAILED":
            return <CancelOutlinedIcon color="error" fontSize="small" />;
    }
}

function statusLabel(status: SubmissionStatus): string {
    switch (status) {
        case "SUCCESS":
            return "Success";
        case "PENDING":
            return "Pending";
        case "FAILED":
            return "Failed";
    }
}

const COLUMN_COUNT = 4;

export const SubmissionTableRows: FC<SubmissionTableRowsProps> = ({ items, loading, error, onRowClick }) => {
    if (loading || error) {
        return (
            <>
                {Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={`placeholder-${String(i)}`}>
                        {Array.from({ length: COLUMN_COUNT }).map((_, j) => (
                            <TableCell key={`cell-${String(j)}`}>
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
                        <Chip icon={statusIcon(item.status)} label={statusLabel(item.status)} size="small" />
                    </TableCell>
                    <TableCell>{item.problemKey}</TableCell>
                    <TableCell>{item.userName}</TableCell>
                    <TableCell>{formatRelative(item.updatedAt)}</TableCell>
                </TableRow>
            ))}
        </>
    );
};
