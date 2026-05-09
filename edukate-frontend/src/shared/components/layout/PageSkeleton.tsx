import { Box, CircularProgress, Container, Typography } from "@mui/material";
import { EdukateTopBar } from "./topbar/EdukateTopBar";
import { Outlet } from "react-router-dom";
import { useAuthContext } from "@/features/auth/context";
import { lazy, Suspense, useEffect } from "react";
import { toast } from "react-toastify";
import { AppFooter } from "./AppFooter";
import { ErrorBoundary, FallbackProps } from "react-error-boundary";
import { OfflineBanner } from "@/shared/components/pwa/OfflineBanner";
import { InstallPrompt } from "@/shared/components/pwa/InstallPrompt";
import { UpdatePrompt } from "@/shared/components/pwa/UpdatePrompt";
import { useTranslation } from "react-i18next";

const ParticlesComponent = lazy(() => import("@/shared/components/Particles"));

function RouteFallback({ error }: FallbackProps) {
    const { t } = useTranslation("common");
    const message = error instanceof Error ? error.message : String(error);
    return (
        <Box sx={{ py: 4, textAlign: "center" }}>
            <Typography variant="h6" color="error" gutterBottom>
                {t("something_went_wrong")}
            </Typography>
            <Typography variant="body2" color="text.secondary">
                {message}
            </Typography>
        </Box>
    );
}

export default function PageSkeleton() {
    const { user } = useAuthContext();
    const { t } = useTranslation("common");

    useEffect(() => {
        if (user?.status === "PENDING") {
            toast.info(t("account_pending_approval"));
        }
    }, [user?.status, t]);

    return (
        <Box>
            <OfflineBanner />
            <EdukateTopBar />
            <Container maxWidth={"lg"} sx={{ pt: "120px", pb: "2rem" }}>
                <ErrorBoundary FallbackComponent={RouteFallback}>
                    <Suspense
                        fallback={
                            <Box sx={{ display: "flex", justifyContent: "center", pt: 4 }}>
                                <CircularProgress />
                            </Box>
                        }
                    >
                        <Outlet />
                    </Suspense>
                </ErrorBoundary>
                <Suspense fallback={null}>
                    <ParticlesComponent />
                </Suspense>
            </Container>
            <AppFooter />
            <InstallPrompt />
            <UpdatePrompt />
        </Box>
    );
}
