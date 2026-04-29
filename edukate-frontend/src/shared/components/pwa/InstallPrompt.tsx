import { useState } from "react";
import { Button, IconButton, Snackbar } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { usePwaContext } from "@/shared/context/PwaContext";

export function InstallPrompt() {
    const { canInstall, promptInstall } = usePwaContext();
    const [dismissed, setDismissed] = useState(false);

    return (
        <Snackbar
            open={canInstall && !dismissed}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            message="Install Edukate for a better experience"
            action={
                <>
                    <Button color="secondary" size="small" onClick={promptInstall}>
                        Install
                    </Button>
                    <IconButton
                        size="small"
                        color="inherit"
                        onClick={() => {
                            setDismissed(true);
                        }}
                    >
                        <CloseIcon fontSize="small" />
                    </IconButton>
                </>
            }
        />
    );
}
