import { useCallback } from "react";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

export function useCopyToClipboard() {
    const { t } = useTranslation("common");
    return useCallback(
        (text: string) => {
            void navigator.clipboard.writeText(text).then(() => {
                toast.info(t("copied_to_clipboard"));
            });
        },
        [t],
    );
}
