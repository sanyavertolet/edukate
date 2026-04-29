import { Alert, Collapse } from "@mui/material";
import { useOnlineStatus } from "@/shared/hooks/useOnlineStatus";

export function OfflineBanner() {
    const isOnline = useOnlineStatus();

    return (
        <Collapse in={!isOnline}>
            <Alert severity="warning" sx={{ borderRadius: 0 }}>
                You are offline. Some features may be unavailable.
            </Alert>
        </Collapse>
    );
}
