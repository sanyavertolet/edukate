import { registerSW } from "virtual:pwa-register";

const updateSW = registerSW({
    onNeedRefresh() {
        window.dispatchEvent(new CustomEvent("sw-update-available"));
    },
    onOfflineReady() {
        console.log("Edukate is ready for offline use.");
    },
});

(window as unknown as { __updateSW: typeof updateSW }).__updateSW = updateSW;
