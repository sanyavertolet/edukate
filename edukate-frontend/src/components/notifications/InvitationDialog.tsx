import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import { FC } from "react";

interface InvitationDialogProps {
    bundleName: string | undefined;
    inviterName: string | undefined;
    onClose: (isAccepted: boolean | undefined) => void;
    open: boolean;
}

export const InvitationDialog: FC<InvitationDialogProps> = ({inviterName, bundleName, onClose, open}) => {
    return (
        <Dialog
            open={open}
            onClose={() => onClose(undefined)}
            aria-labelledby="invitation-dialog-title"
            aria-describedby="invitation-dialog-description"
        >
            <DialogTitle id="invitation-dialog-title">
                {`${bundleName} invite`}
            </DialogTitle>
            <DialogContent>
                <DialogContentText id="invitation-dialog-description">
                    {`${inviterName} has invited you to ${bundleName} bundle. Do you wish to join the bundle?`}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => onClose(undefined)}>Close</Button>
                <Button onClick={() => onClose(false)}>Decline</Button>
                <Button onClick={() => onClose(true)} autoFocus>Accept</Button>
            </DialogActions>
        </Dialog>
    );
};
