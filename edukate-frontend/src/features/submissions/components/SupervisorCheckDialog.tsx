import { FC } from "react";
import {
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    Stack,
    Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import SupervisorAccountOutlinedIcon from "@mui/icons-material/SupervisorAccountOutlined";
import { useTranslation } from "react-i18next";
import { useDeviceContext } from "@/shared/context/DeviceContext";
import { Submission } from "@/features/submissions/types";
import { useSupervisorCheckForm } from "@/features/submissions/hooks/useSupervisorCheckForm";
import { SupervisorCheckFormFields } from "./SupervisorCheckForm";

type SupervisorCheckDialogProps = {
    submission: Submission | null;
    defaultProblemSetCode?: string;
    onClose: () => void;
};

export const SupervisorCheckDialog: FC<SupervisorCheckDialogProps> = ({ submission, defaultProblemSetCode, onClose }) => {
    const { t } = useTranslation("submissions");
    const { isMobile } = useDeviceContext();

    return (
        <Dialog
            open={submission !== null}
            onClose={onClose}
            maxWidth="sm"
            fullWidth
            fullScreen={isMobile}
            sx={{ zIndex: (theme) => theme.zIndex.snackbar + 2 }}
        >
            {submission && (
                <DialogBody
                    submission={submission}
                    defaultProblemSetCode={defaultProblemSetCode}
                    onClose={onClose}
                    titleLabel={t("supervisor_check_dialog_title")}
                    descriptionLabel={t("supervisor_check_description")}
                    submitLabel={t("request_supervisor_check_button")}
                    cancelLabel={t("cancel_button", { ns: "problem-sets" })}
                    closeAriaLabel={t("close_drawer_label")}
                />
            )}
        </Dialog>
    );
};

type DialogBodyProps = {
    submission: Submission;
    defaultProblemSetCode?: string;
    onClose: () => void;
    titleLabel: string;
    descriptionLabel: string;
    submitLabel: string;
    cancelLabel: string;
    closeAriaLabel: string;
};

const DialogBody: FC<DialogBodyProps> = ({
    submission,
    defaultProblemSetCode,
    onClose,
    titleLabel,
    descriptionLabel,
    submitLabel,
    cancelLabel,
    closeAriaLabel,
}) => {
    const form = useSupervisorCheckForm({
        submissionId: String(submission.id),
        problemKey: submission.problemKey,
        defaultProblemSetCode,
        onSubmitted: onClose,
    });

    return (
        <>
            <DialogTitle
                component="div"
                sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 1, pr: 1 }}
            >
                <Stack direction="row" alignItems="center" spacing={1.5} sx={{ minWidth: 0 }}>
                    <SupervisorAccountOutlinedIcon color="primary" />
                    <Typography variant="h6" noWrap>
                        {titleLabel}
                    </Typography>
                </Stack>
                <IconButton onClick={onClose} size="small" aria-label={closeAriaLabel}>
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2}>
                    <Typography variant="body2" color="text.secondary">
                        {descriptionLabel}
                    </Typography>
                    <SupervisorCheckFormFields form={form} />
                </Stack>
            </DialogContent>

            <DialogActions sx={{ px: 3, py: 2 }}>
                <Button onClick={onClose} color="inherit">
                    {cancelLabel}
                </Button>
                <Button
                    onClick={form.handleSubmit}
                    disabled={!form.isValid || form.isPending}
                    variant="contained"
                    startIcon={form.isPending ? <CircularProgress size={14} color="inherit" /> : undefined}
                >
                    {submitLabel}
                </Button>
            </DialogActions>
        </>
    );
};
