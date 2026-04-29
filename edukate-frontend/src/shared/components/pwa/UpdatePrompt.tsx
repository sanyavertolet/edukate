import { useState } from "react";
import { Button, IconButton, Snackbar } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { usePwaContext } from "@/shared/context/PwaContext";

export function UpdatePrompt() {
    const { updateAvailable, applyUpdate } = usePwaContext();
    const [dismissed, setDismissed] = useState(false);

    return (
        <Snackbar
            open={updateAvailable && !dismissed}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            message="A new version of Edukate is available"
            action={
                <>
                    <Button color="secondary" size="small" onClick={applyUpdate}>
                        Update
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
