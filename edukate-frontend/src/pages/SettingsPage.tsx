import { Container, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { Card } from "@/shared/components/Styled";
import { ProfileSection } from "@/features/settings/components/ProfileSection";

export default function SettingsPage() {
    const { t } = useTranslation("settings");
    return (
        <Container maxWidth="md">
            <Typography component="h1" color="primary" variant="h5" align="center">
                {t("page_title")}
            </Typography>
            <Card sx={{ p: 3, mt: "1rem" }}>
                <ProfileSection />
            </Card>
        </Container>
    );
}
