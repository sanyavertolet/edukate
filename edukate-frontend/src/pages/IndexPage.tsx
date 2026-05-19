import { Box, Button, CardContent, Typography } from "@mui/material";
import { Card } from "@/shared/components/Styled";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function IndexPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const handleClick = () => {
        void navigate("/problems");
    };

    return (
        <Box>
            <Typography component="h1" color="primary" variant="h5" align="center">
                {t("welcome_title")}
            </Typography>

            <Card sx={{ textAlign: "center", marginTop: "2rem", padding: "1rem" }}>
                <CardContent>
                    <Typography variant="body1" marginBottom={4}>
                        {t("welcome_description")}
                    </Typography>
                    <Button variant="contained" color="primary" onClick={handleClick}>
                        {t("explore_problems_button")}
                    </Button>
                </CardContent>
            </Card>
        </Box>
    );
}
