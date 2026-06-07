import { FC } from "react";
import { Box, Divider, IconButton, Stack, Typography } from "@mui/material";
import { Drawer } from "@/shared/components/Styled";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";
import { SupervisorTicketDto } from "@/features/supervisor-tickets/types";
import { SupervisorTicketContent } from "./SupervisorTicketContent";

type SupervisorTicketDrawerProps = {
    ticket: SupervisorTicketDto | null;
    problemSetShareCode: string;
    page: number;
    size: number;
    onClose: () => void;
};

export const SupervisorTicketDrawer: FC<SupervisorTicketDrawerProps> = ({
    ticket,
    problemSetShareCode,
    page,
    size,
    onClose,
}) => {
    const { t } = useTranslation("supervisor-tickets");
    return (
        <Drawer
            anchor="right"
            open={ticket !== null}
            onClose={onClose}
            slotProps={{ paper: { sx: { width: { xs: "100%", sm: 640 } } } }}
        >
            <Stack direction="row" alignItems="center" justifyContent="space-between" px={2} py={1.5}>
                <Typography variant="h6" noWrap sx={{ flex: 1, mr: 1 }}>
                    {ticket ? t("drawer_title", { id: String(ticket.id), problemKey: ticket.problemKey }) : ""}
                </Typography>
                <IconButton onClick={onClose} size="small">
                    <CloseIcon />
                </IconButton>
            </Stack>

            <Divider />

            <Box sx={{ overflowY: "auto", flex: 1 }}>
                {ticket && (
                    <SupervisorTicketContent
                        ticket={ticket}
                        problemSetShareCode={problemSetShareCode}
                        page={page}
                        size={size}
                        onVerdictSubmitted={onClose}
                    />
                )}
            </Box>
        </Drawer>
    );
};
