import { useState } from "react";
import { Button, IconButton, Snackbar } from "@mui/material";
import { alpha } from "@mui/material/styles";
import CloseIcon from "@mui/icons-material/Close";
import { usePwaContext } from "@/shared/context/PwaContext";
import { frostedGlass } from "@/shared/components/Styled";

export function UpdatePrompt() {
    const { updateAvailable, applyUpdate } = usePwaContext();
    const [dismissed, setDismissed] = useState(false);

    return (
        <Snackbar
            open={updateAvailable && !dismissed}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            message="A new version of Edukate is available"
            slotProps={{
                content: {
                    sx: (theme) => ({
                        ...frostedGlass(theme),
                        backgroundColor: alpha(theme.palette.background.paper, 0.65),
                        border: "1px solid",
                        borderColor: theme.palette.divider,
                        color: theme.palette.text.primary,
                    }),
                },
            }}
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
