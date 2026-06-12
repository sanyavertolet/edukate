import { FC, useState } from "react";
import { Alert, Box, Chip, CircularProgress, Divider, Stack, Typography } from "@mui/material";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import { useTranslation } from "react-i18next";
import { SupervisorTicketDto } from "@/features/supervisor-tickets/types";
import { useProblemRequest } from "@/features/problems/api";
import ProblemCard from "@/features/problems/components/cards/ProblemCard";
import { AnswerAccordionComponent } from "@/features/problems/components/AnswerAccordion";
import { ImageLightbox } from "@/shared/components/images/ImageLightbox";
import { VerdictForm } from "./VerdictForm";

type SupervisorTicketContentProps = {
    ticket: SupervisorTicketDto;
    problemSetShareCode: string;
    page: number;
    size: number;
    onVerdictSubmitted: () => void;
};

export const SupervisorTicketContent: FC<SupervisorTicketContentProps> = ({
    ticket,
    problemSetShareCode,
    page,
    size,
    onVerdictSubmitted,
}) => {
    const { t } = useTranslation("supervisor-tickets");
    const [lightboxIndex, setLightboxIndex] = useState(-1);

    const [bookSlug, code] = ticket.problemKey.split("/", 2) as [string, string];
    const { data: problem, isLoading } = useProblemRequest(bookSlug, code);

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, p: 2 }}>
            {isLoading && (
                <Box display="flex" justifyContent="center" py={4}>
                    <CircularProgress />
                </Box>
            )}
            {!isLoading && problem && (
                <Stack spacing={2}>
                    <ProblemCard problem={problem} />
                    <AnswerAccordionComponent problem={problem} fullWidth />
                    <Box>
                        <Typography variant="overline" color="text.secondary">
                            {t("solution_heading")}
                        </Typography>
                        {ticket.fileUrls.length === 0 ? (
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                                {t("solution_no_files")}
                            </Typography>
                        ) : (
                            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mt: 0.5 }}>
                                {ticket.fileUrls.map((_, i) => (
                                    <Chip
                                        key={i}
                                        icon={<AttachFileIcon />}
                                        label={`File ${String(i + 1)}`}
                                        size="small"
                                        variant="outlined"
                                        color="primary"
                                        onClick={() => {
                                            setLightboxIndex(i);
                                        }}
                                        clickable
                                    />
                                ))}
                            </Stack>
                        )}
                    </Box>
                </Stack>
            )}
            {!isLoading && !problem && <Alert severity="error">{t("tickets_load_error")}</Alert>}

            <ImageLightbox
                images={ticket.fileUrls}
                index={lightboxIndex}
                open={lightboxIndex >= 0}
                onClose={() => {
                    setLightboxIndex(-1);
                }}
            />

            {ticket.status === "PENDING" && (
                <>
                    <Divider />
                    <VerdictForm
                        key={ticket.id}
                        ticketId={ticket.id}
                        problemSetShareCode={problemSetShareCode}
                        page={page}
                        size={size}
                        onSubmitSuccess={onVerdictSubmitted}
                    />
                </>
            )}
        </Box>
    );
};
