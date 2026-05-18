import { FC } from "react";
import { TableRow, TableCell, Skeleton, Box, Tooltip, SxProps, Theme } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { useTranslation } from "react-i18next";
import { Submission, SubmissionStatus } from "@/features/submissions/types";
import { formatRelative } from "@/shared/utils/date";
import { BookLabel } from "@/shared/components/BookLabel";
import { ProblemLabel } from "@/shared/components/ProblemLabel";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";

type SubmissionTableRowsProps = {
    items: Submission[] | undefined;
    loading: boolean;
    error: unknown;
    onRowClick: (submission: Submission) => void;
    onBookSlugClick: (bookSlug: string) => void;
    onUserNameClick: (userName: string) => void;
    onProblemKeyClick: (problemKey: string) => void;
};

function SubmissionStatusIcon({ status }: { status: SubmissionStatus }) {
    const { t } = useTranslation("submissions");
    switch (status) {
        case "SUCCESS":
            return (
                <Tooltip title={t("status_success")} slotProps={defaultTooltipSlotProps}>
                    <CheckCircleOutlineIcon color="success" />
                </Tooltip>
            );
        case "PENDING":
            return (
                <Tooltip title={t("status_pending")} slotProps={defaultTooltipSlotProps}>
                    <HourglassEmptyIcon color="warning" />
                </Tooltip>
            );
        case "FAILED":
            return (
                <Tooltip title={t("status_failed")} slotProps={defaultTooltipSlotProps}>
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
    onProblemKeyClick,
}) => {
    const { i18n } = useTranslation();
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
                        <BookLabel bookSlug={item.problemKey.split("/")[0]} onClick={onBookSlugClick} />
                    </TableCell>
                    <TableCell>
                        <ProblemLabel problemKey={item.problemKey} onClick={onProblemKeyClick} />
                    </TableCell>
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
                    <TableCell sx={columnVisibility[4]}>{formatRelative(item.updatedAt, i18n.language)}</TableCell>
                </TableRow>
            ))}
        </>
    );
};
