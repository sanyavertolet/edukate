import { Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import { GroupAdd } from "@mui/icons-material";
import { FC } from "react";
import { useDeviceContext } from "@/shared/context/DeviceContext";

type ProblemSetInviteInfo = {
    problemSetName: string;
    inviterName: string;
};

interface InvitationDialogProps {
    problemSetInfo: ProblemSetInviteInfo | undefined;
    onClose: (isAccepted: boolean | undefined) => void;
}

export const InvitationDialog: FC<InvitationDialogProps> = ({ problemSetInfo, onClose }) => {
    const { isMobile } = useDeviceContext();
    return (
        <Dialog
            open={problemSetInfo != undefined}
            fullWidth
            fullScreen={isMobile}
            onClose={() => {
                onClose(undefined);
            }}
            aria-labelledby="invitation-dialog-title"
            aria-describedby="invitation-dialog-description"
        >
            <DialogTitle id="invitation-dialog-title">
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <GroupAdd color="primary" />
                    {`${problemSetInfo?.problemSetName ?? ""} invite`}
                </Box>
            </DialogTitle>
            <DialogContent>
                <DialogContentText id="invitation-dialog-description">
                    {`${problemSetInfo?.inviterName ?? ""} has invited you to ${problemSetInfo?.problemSetName ?? ""} problem set. Do you wish to join the problem set?`}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button
                    onClick={() => {
                        onClose(undefined);
                    }}
                >
                    Close
                </Button>
                <Button
                    color="error"
                    onClick={() => {
                        onClose(false);
                    }}
                >
                    Decline
                </Button>
                <Button
                    variant="contained"
                    onClick={() => {
                        onClose(true);
                    }}
                    autoFocus
                >
                    Accept
                </Button>
            </DialogActions>
        </Dialog>
    );
};
