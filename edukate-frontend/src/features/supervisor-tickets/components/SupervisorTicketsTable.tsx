import { FC } from "react";
import {
    Alert,
    Box,
    Chip,
    CircularProgress,
    Link,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    Typography,
} from "@mui/material";
import AssignmentOutlinedIcon from "@mui/icons-material/AssignmentOutlined";
import { Link as RouterLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Paper } from "@/shared/components/Styled";
import { SupervisorTicketDto, SupervisorTicketStatus } from "@/features/supervisor-tickets/types";
import { formatDate } from "@/shared/utils/date";

type SupervisorTicketsTableProps = {
    tickets: SupervisorTicketDto[] | undefined;
    totalElements: number;
    isLoading: boolean;
    error: unknown;
    page: number;
    size: number;
    onPageChange: (page: number) => void;
    onRowClick: (ticket: SupervisorTicketDto) => void;
};

function statusChip(status: SupervisorTicketStatus, t: (k: string) => string) {
    return status === "PENDING" ? (
        <Chip label={t("status_pending")} color="warning" size="small" />
    ) : (
        <Chip label={t("status_resolved")} color="success" size="small" />
    );
}

export const SupervisorTicketsTable: FC<SupervisorTicketsTableProps> = ({
    tickets,
    totalElements,
    isLoading,
    error,
    page,
    size,
    onPageChange,
    onRowClick,
}) => {
    const { t, i18n } = useTranslation("supervisor-tickets");

    return (
        <Paper sx={{ overflow: "hidden" }}>
            <TableContainer>
                <Table size="small" aria-label="supervisor tickets">
                    <TableHead>
                        <TableRow>
                            <TableCell>{t("col_problem")}</TableCell>
                            <TableCell>{t("col_status")}</TableCell>
                            <TableCell>{t("col_requested")}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {isLoading && (
                            <TableRow>
                                <TableCell colSpan={3} align="center" sx={{ py: 4 }}>
                                    <CircularProgress size={24} />
                                </TableCell>
                            </TableRow>
                        )}

                        {!isLoading && !!error && (
                            <TableRow>
                                <TableCell colSpan={3}>
                                    <Alert severity="error">{t("tickets_load_error")}</Alert>
                                </TableCell>
                            </TableRow>
                        )}

                        {!isLoading && !error && tickets?.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={3} align="center" sx={{ py: 4 }}>
                                    <Box
                                        display="flex"
                                        flexDirection="column"
                                        alignItems="center"
                                        gap={1}
                                        color="text.disabled"
                                    >
                                        <AssignmentOutlinedIcon sx={{ fontSize: 40 }} />
                                        <Typography variant="body2">{t("tickets_empty")}</Typography>
                                    </Box>
                                </TableCell>
                            </TableRow>
                        )}

                        {!isLoading &&
                            !error &&
                            tickets?.map((ticket) => (
                                <TableRow
                                    key={ticket.id}
                                    hover
                                    sx={{ cursor: "pointer" }}
                                    onClick={() => {
                                        onRowClick(ticket);
                                    }}
                                >
                                    <TableCell>
                                        <Link
                                            component={RouterLink}
                                            to={`/problems/${ticket.problemKey}`}
                                            variant="body2"
                                            fontFamily="monospace"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                            }}
                                        >
                                            {ticket.problemKey}
                                        </Link>
                                    </TableCell>
                                    <TableCell>{statusChip(ticket.status, t)}</TableCell>
                                    <TableCell>
                                        <Typography variant="caption" color="text.secondary">
                                            {formatDate(ticket.createdAt, { locale: i18n.language })}
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <TablePagination
                component="div"
                count={totalElements}
                rowsPerPage={size}
                page={page}
                onPageChange={(_e, p) => {
                    onPageChange(p);
                }}
                rowsPerPageOptions={[size]}
            />
        </Paper>
    );
};
