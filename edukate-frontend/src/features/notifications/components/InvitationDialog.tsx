import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import { FC } from "react";

type ProblemSetInviteInfo = {
    problemSetName: string;
    inviterName: string;
};

interface InvitationDialogProps {
    problemSetInfo: ProblemSetInviteInfo | undefined;
    onClose: (isAccepted: boolean | undefined) => void;
}

export const InvitationDialog: FC<InvitationDialogProps> = ({ problemSetInfo, onClose }) => {
    return (
        <Dialog
            open={problemSetInfo != undefined}
            onClose={() => {
                onClose(undefined);
            }}
            aria-labelledby="invitation-dialog-title"
            aria-describedby="invitation-dialog-description"
        >
            <DialogTitle id="invitation-dialog-title">{`${problemSetInfo?.problemSetName ?? ""} invite`}</DialogTitle>
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
                    onClick={() => {
                        onClose(false);
                    }}
                >
                    Decline
                </Button>
                <Button
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
