import { createContext, FC, ReactNode, useCallback, useContext, useEffect, useState } from "react";
import { ErrorSnackbar } from "./ErrorSnackbar";

/**
 * Interface for notification options
 */
export interface NotificationOptions {
    /** Severity level of the notification */
    severity?: "error" | "warning" | "info" | "success";
    /** Visual style of the notification */
    variant?: "filled" | "outlined" | "standard";
    /** Duration in milliseconds to show the notification */
    autoHideDuration?: number;
}

/**
 * Interface for the notification context
 */
interface NotificationContextType {
    /** Current notification message to display */
    notificationMessage: string | undefined;
    /** Current notification options */
    notificationOptions: NotificationOptions;
    /** Function to set or clear the notification message with optional configuration */
    showNotification: (message: string | undefined, options?: NotificationOptions) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

interface NotificationProviderProps {
    children: ReactNode;
}

/**
 * Provider component that makes notification context available to all child components
 */
export const NotificationProvider: FC<NotificationProviderProps> = ({ children }) => {
    const [notificationMessage, setNotificationMessage] = useState<string | undefined>(undefined);
    const [notificationOptions, setNotificationOptions] = useState<NotificationOptions>({
        severity: "info",
        variant: "standard",
        autoHideDuration: 5000
    });

    // Create a stable reference to the context value
    const notificationContextValue = {
        notificationMessage,
        notificationOptions,
        showNotification: useCallback((message: string | undefined, options?: NotificationOptions) => {
            setNotificationMessage(message);
            if (options) {
                setNotificationOptions(prevOptions => ({ ...prevOptions, ...options }));
            }
        }, [])
    };

    // Add an event listener for custom notification events from non-React code
    useEffect(() => {
        const handleNotificationEvent = (event: CustomEvent<{message: string, options?: NotificationOptions}>) => {
            setNotificationMessage(event.detail.message);
            if (event.detail.options) {
                setNotificationOptions(prevOptions => ({ ...prevOptions, ...event.detail.options }));
            }
        };

        window.addEventListener('show-notification-message', handleNotificationEvent as EventListener);

        return () => {
            window.removeEventListener('show-notification-message', handleNotificationEvent as EventListener);
        };
    }, []);

    return (
        <NotificationContext.Provider value={notificationContextValue}>
            {children}
            <ErrorSnackbar 
                errorText={notificationMessage}
                severity={notificationOptions.severity}
                variant={notificationOptions.variant}
                autoHideDuration={notificationOptions.autoHideDuration}
            />
        </NotificationContext.Provider>
    );
};

/**
 * Custom hook to use the notification context
 * @returns The notification context
 * @throws Error if used outside NotificationProvider
 */
export const useNotificationContext = (): NotificationContextType => {
    const context = useContext(NotificationContext);
    if (context === undefined) {
        throw new Error('useNotificationContext must be used within a NotificationProvider');
    }
    return context;
};

export const showNotification = (message: string, options?: NotificationOptions): void => {
    const event = new CustomEvent('show-notification-message', {
        detail: { 
            message, 
            options 
        } 
    });
    window.dispatchEvent(event);
};
