import { FC } from "react";
import { Box, Divider, Drawer, IconButton, Stack, Typography } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { Submission } from "@/features/submissions/types";
import { SubmissionComponent } from "@/features/submissions/components/SubmissionComponent";

type SubmissionDrawerProps = {
    submission: Submission | null;
    onClose: () => void;
};

export const SubmissionDrawer: FC<SubmissionDrawerProps> = ({ submission, onClose }) => {
    return (
        <Drawer
            anchor="right"
            open={submission !== null}
            onClose={onClose}
            PaperProps={{ sx: { width: { xs: "100%", sm: 520 } } }}
        >
            <Stack direction="row" alignItems="center" justifyContent="space-between" px={2} py={1.5}>
                <Typography variant="h6">Submission #{submission?.id ?? ""}</Typography>
                <IconButton onClick={onClose} size="small" aria-label="Close submission drawer">
                    <CloseIcon />
                </IconButton>
            </Stack>

            <Divider />

            <Box sx={{ overflowY: "auto", flex: 1 }}>{submission && <SubmissionComponent submission={submission} />}</Box>
        </Drawer>
    );
};
