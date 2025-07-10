import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import { FC } from "react";

type BundleInviteInfo = {
    bundleName: string;
    inviterName: string;
};

interface InvitationDialogProps {
    bundleInfo: BundleInviteInfo | undefined;
    onClose: (isAccepted: boolean | undefined) => void;
}

export const InvitationDialog: FC<InvitationDialogProps> = ({bundleInfo, onClose}) => {
    return (
        <Dialog
            open={bundleInfo != undefined}
            onClose={() => onClose(undefined)}
            aria-labelledby="invitation-dialog-title"
            aria-describedby="invitation-dialog-description"
        >
            <DialogTitle id="invitation-dialog-title">
                {`${bundleInfo?.bundleName} invite`}
            </DialogTitle>
            <DialogContent>
                <DialogContentText id="invitation-dialog-description">
                    {`${bundleInfo?.inviterName} has invited you to ${bundleInfo?.bundleName} bundle. Do you wish to join the bundle?`}
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
