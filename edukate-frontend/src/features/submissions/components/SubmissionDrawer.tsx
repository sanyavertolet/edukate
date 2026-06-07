import { FC } from "react";
import { Box, Divider, IconButton, Stack, Typography } from "@mui/material";
import { Drawer } from "@/shared/components/Styled";
import CloseIcon from "@mui/icons-material/Close";
import { Submission } from "@/features/submissions/types";
import { SubmissionComponent } from "@/features/submissions/components/SubmissionComponent";
import { useTranslation } from "react-i18next";

type SubmissionDrawerProps = {
    submission: Submission | null;
    onClose: () => void;
    isOwner?: boolean;
};

export const SubmissionDrawer: FC<SubmissionDrawerProps> = ({ submission, onClose, isOwner }) => {
    const { t } = useTranslation("submissions");
    return (
        <Drawer
            anchor="right"
            open={submission !== null}
            onClose={onClose}
            sx={{ zIndex: (theme) => theme.zIndex.snackbar + 1 }}
            slotProps={{ paper: { sx: { width: { xs: "100%", sm: 520 } } } }}
        >
            <Stack direction="row" alignItems="center" justifyContent="space-between" px={2} py={1.5}>
                <Typography variant="h6">{t("submission_heading", { id: submission?.id ?? "" })}</Typography>
                <IconButton onClick={onClose} size="small" aria-label={t("close_drawer_label")}>
                    <CloseIcon />
                </IconButton>
            </Stack>

            <Divider />

            <Box sx={{ overflowY: "auto", flex: 1 }}>
                {submission && <SubmissionComponent submission={submission} isOwner={isOwner} />}
            </Box>
        </Drawer>
    );
};
