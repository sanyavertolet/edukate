import { Container } from "@mui/material";
import { ResetPasswordForm } from "@/features/auth/components/ResetPasswordForm";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

export default function ResetPasswordPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { t } = useTranslation("auth");
    const token = searchParams.get("token") ?? "";

    return (
        <Container maxWidth="sm">
            <ResetPasswordForm
                token={token}
                onSuccess={() => {
                    toast.success(t("reset_password_success"));
                    void navigate("/sign-in", { replace: true });
                }}
            />
        </Container>
    );
}
