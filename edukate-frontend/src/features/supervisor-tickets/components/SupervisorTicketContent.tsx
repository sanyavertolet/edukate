import { FC, useState } from "react";
import { Alert, Box, Chip, CircularProgress, Divider, Stack, Tab, Tabs, Typography } from "@mui/material";
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
    const [tab, setTab] = useState(0);
    const [lightboxIndex, setLightboxIndex] = useState(-1);

    const [bookSlug, code] = ticket.problemKey.split("/", 2) as [string, string];
    const { data: problem, isLoading } = useProblemRequest(bookSlug, code);

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, p: 2 }}>
            <Tabs
                value={tab}
                onChange={(_e, v: number) => {
                    setTab(v);
                }}
                variant="fullWidth"
            >
                <Tab label={t("problem_tab")} />
                <Tab label={t("solution_tab")} />
            </Tabs>

            {tab === 0 && (
                <Box>
                    {isLoading && (
                        <Box display="flex" justifyContent="center" py={4}>
                            <CircularProgress />
                        </Box>
                    )}
                    {!isLoading && problem && (
                        <Stack spacing={2}>
                            <ProblemCard problem={problem} />
                            <AnswerAccordionComponent problem={problem} />
                        </Stack>
                    )}
                    {!isLoading && !problem && <Alert severity="error">{t("tickets_load_error")}</Alert>}
                </Box>
            )}

            {tab === 1 && (
                <Box>
                    {ticket.fileUrls.length === 0 ? (
                        <Typography variant="body2" color="text.secondary" textAlign="center" py={4}>
                            {t("solution_no_files")}
                        </Typography>
                    ) : (
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
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
                    <ImageLightbox
                        images={ticket.fileUrls}
                        index={lightboxIndex}
                        open={lightboxIndex >= 0}
                        onClose={() => {
                            setLightboxIndex(-1);
                        }}
                    />
                </Box>
            )}

            {ticket.status === "PENDING" && (
                <>
                    <Divider />
                    <VerdictForm
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
