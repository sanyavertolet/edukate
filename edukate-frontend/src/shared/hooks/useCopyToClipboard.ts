import { useCallback } from "react";
import { toast } from "react-toastify";

export function useCopyToClipboard() {
    return useCallback((text: string) => {
        void navigator.clipboard.writeText(text).then(() => {
            toast.info("Copied to clipboard.");
        });
    }, []);
}
