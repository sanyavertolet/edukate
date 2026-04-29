import { createContext, FC, ReactNode, useCallback, useContext, useEffect, useState } from "react";

interface BeforeInstallPromptEvent extends Event {
    prompt(): Promise<{ outcome: "accepted" | "dismissed" }>;
}

type PwaContextType = {
    canInstall: boolean;
    promptInstall: () => void;
    updateAvailable: boolean;
    applyUpdate: () => void;
};

const PwaContext = createContext<PwaContextType>({
    canInstall: false,
    promptInstall: () => {},
    updateAvailable: false,
    applyUpdate: () => {},
});

export const PwaProvider: FC<{ children: ReactNode }> = ({ children }) => {
    const [installEvent, setInstallEvent] = useState<BeforeInstallPromptEvent | null>(null);
    const [updateAvailable, setUpdateAvailable] = useState(false);

    useEffect(() => {
        const handler = (e: Event) => {
            e.preventDefault();
            setInstallEvent(e as BeforeInstallPromptEvent);
        };
        window.addEventListener("beforeinstallprompt", handler);
        return () => {
            window.removeEventListener("beforeinstallprompt", handler);
        };
    }, []);

    useEffect(() => {
        const handler = () => {
            setUpdateAvailable(true);
        };
        window.addEventListener("sw-update-available", handler);
        return () => {
            window.removeEventListener("sw-update-available", handler);
        };
    }, []);

    const promptInstall = useCallback(() => {
        if (installEvent) {
            void installEvent.prompt().then(({ outcome }) => {
                if (outcome === "accepted") setInstallEvent(null);
            });
        }
    }, [installEvent]);

    const applyUpdate = useCallback(() => {
        const updateSW = (window as unknown as { __updateSW?: (reload?: boolean) => Promise<void> }).__updateSW;
        if (updateSW) void updateSW(true);
    }, []);

    return (
        <PwaContext.Provider value={{ canInstall: installEvent !== null, promptInstall, updateAvailable, applyUpdate }}>
            {children}
        </PwaContext.Provider>
    );
};

export const usePwaContext = () => useContext(PwaContext);
