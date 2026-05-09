import { Button, ButtonProps } from "@mui/material";
import { useTranslation } from "react-i18next";
import { queryClient } from "@/lib/query-client";

export function LanguageSwitcher(props: ButtonProps) {
    const { i18n } = useTranslation();
    const isRussian = i18n.language.startsWith("ru");

    const toggleLanguage = () => {
        void i18n.changeLanguage(isRussian ? "en" : "ru").then(() => {
            void queryClient.invalidateQueries();
        });
    };

    return (
        <Button onClick={toggleLanguage} size="small" color="primary" sx={{ minWidth: 32 }} {...props}>
            {isRussian ? "EN" : "RU"}
        </Button>
    );
}
