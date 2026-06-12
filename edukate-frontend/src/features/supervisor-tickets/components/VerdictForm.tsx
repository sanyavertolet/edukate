import { FC, useEffect, useState } from "react";
import {
    Box,
    Button,
    CircularProgress,
    FormControl,
    IconButton,
    MenuItem,
    Select,
    Stack,
    TextField,
    ToggleButton,
    ToggleButtonGroup,
    Tooltip,
    Typography,
} from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { useTranslation } from "react-i18next";
import { SupervisorVerdictErrorType } from "@/generated/backend";
import { useSubmitVerdictMutation } from "@/features/supervisor-tickets/api";
import { clearDraft, loadDraft, pruneOldDrafts, saveDraft } from "@/features/supervisor-tickets/storage";
import { VerdictDraft } from "@/features/supervisor-tickets/types";
import { ErrorTypeGlossary } from "./ErrorTypeGlossary";

const ERROR_TYPE_KEYS = Object.values(SupervisorVerdictErrorType);

type VerdictFormProps = {
    ticketId: number;
    problemSetShareCode: string;
    page: number;
    size: number;
    onSubmitSuccess: () => void;
};

export const VerdictForm: FC<VerdictFormProps> = ({ ticketId, problemSetShareCode, page, size, onSubmitSuccess }) => {
    const { t } = useTranslation("supervisor-tickets");
    // Hydrate from draft synchronously during mount. Lazy init runs ONCE per
    // mount, so callers must pass `key={ticket.id}` if they reuse the form across tickets.
    const [status, setStatus] = useState<"SUCCESS" | "MISTAKE" | null>(() => {
        pruneOldDrafts();
        return loadDraft(ticketId)?.status ?? null;
    });
    const [errorType, setErrorType] = useState<string>(
        () => loadDraft(ticketId)?.errorType ?? SupervisorVerdictErrorType.NONE,
    );
    const [explanation, setExplanation] = useState(() => loadDraft(ticketId)?.explanation ?? "");
    const [glossaryOpen, setGlossaryOpen] = useState(false);
    const verdictMutation = useSubmitVerdictMutation(problemSetShareCode, page, size);

    useEffect(() => {
        const draft: VerdictDraft = { status, errorType, explanation, savedAt: Date.now() };
        saveDraft(ticketId, draft);
    }, [ticketId, status, errorType, explanation]);

    const isMistake = status === "MISTAKE";
    const hasValidErrorType = errorType !== "" && errorType !== SupervisorVerdictErrorType.NONE;
    const isValid = status !== null && explanation.trim().length > 0 && (!isMistake || hasValidErrorType);

    const visibleErrorTypes = isMistake
        ? ERROR_TYPE_KEYS.filter((k) => k !== SupervisorVerdictErrorType.NONE)
        : ERROR_TYPE_KEYS;

    const handleSubmit = () => {
        if (!isValid) return;
        verdictMutation.mutate(
            {
                ticketId,
                verdict: {
                    status: status,
                    errorType: errorType as SupervisorVerdictErrorType,
                    explanation: explanation.trim(),
                },
            },
            {
                onSuccess: () => {
                    clearDraft(ticketId);
                    onSubmitSuccess();
                },
            },
        );
    };

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
            <Typography variant="subtitle2" color="text.secondary">
                {t("verdict_form_title")}
            </Typography>

            <FormControl>
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1 }}>
                    {t("status_label")}
                </Typography>
                <ToggleButtonGroup
                    exclusive
                    value={status}
                    onChange={(_e, val: "SUCCESS" | "MISTAKE" | null) => {
                        if (val === null) return;
                        setStatus(val);
                        if (val === "SUCCESS") {
                            setErrorType(SupervisorVerdictErrorType.NONE);
                        } else if (errorType === SupervisorVerdictErrorType.NONE) {
                            setErrorType("");
                        }
                    }}
                    size="small"
                >
                    <ToggleButton value="SUCCESS" color="success">
                        <CheckCircleOutlineIcon sx={{ mr: 0.5 }} fontSize="small" />
                        {t("status_success_label")}
                    </ToggleButton>
                    <ToggleButton value="MISTAKE" color="error">
                        <CancelOutlinedIcon sx={{ mr: 0.5 }} fontSize="small" />
                        {t("status_mistake_label")}
                    </ToggleButton>
                </ToggleButtonGroup>
            </FormControl>

            <Box>
                <Stack direction="row" alignItems="center" spacing={0.5} sx={{ mb: 0.5 }}>
                    <Typography variant="caption" color="text.secondary">
                        {t("error_type_label")}
                    </Typography>
                    <Tooltip title={t("error_type_info_button")}>
                        <IconButton
                            size="small"
                            onClick={() => {
                                setGlossaryOpen(true);
                            }}
                        >
                            <InfoOutlinedIcon sx={{ fontSize: 14 }} />
                        </IconButton>
                    </Tooltip>
                </Stack>
                <FormControl fullWidth size="small" disabled={status === "SUCCESS"}>
                    <Select
                        displayEmpty
                        value={status === "SUCCESS" ? SupervisorVerdictErrorType.NONE : errorType}
                        onChange={(e) => {
                            setErrorType(e.target.value);
                        }}
                    >
                        {visibleErrorTypes.map((key) => (
                            <MenuItem key={key} value={key}>
                                {t(`error_type_${key.toLowerCase()}`)}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Box>

            <TextField
                label={t("explanation_label")}
                placeholder={t("explanation_placeholder")}
                multiline
                rows={4}
                fullWidth
                value={explanation}
                onChange={(e) => {
                    setExplanation(e.target.value);
                }}
                size="small"
            />

            <Button
                variant="contained"
                disabled={!isValid || verdictMutation.isPending}
                startIcon={verdictMutation.isPending ? <CircularProgress size={16} /> : undefined}
                onClick={handleSubmit}
            >
                {t("submit_verdict_button")}
            </Button>

            <ErrorTypeGlossary
                open={glossaryOpen}
                onClose={() => {
                    setGlossaryOpen(false);
                }}
            />
        </Box>
    );
};
