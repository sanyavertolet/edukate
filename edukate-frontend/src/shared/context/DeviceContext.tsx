import { createContext, FC, ReactNode, useContext, useEffect, useState } from "react";
import { useMediaQuery, useTheme } from "@mui/material";

function areNavigationsEqual(nav1: AdditionalNavigationElement[], nav2: AdditionalNavigationElement[]) {
    if (nav1.length !== nav2.length) return false;
    for (let i = 0; i < nav1.length; i++) {
        if (nav1[i].text !== nav2[i].text) return false;
        if (nav1[i].isSelected !== nav2[i].isSelected) return false;
    }
    return true;
}

export type AdditionalNavigationElement = {
    text: string;
    onClick: () => void;
    icon?: ReactNode;
    isSelected?: boolean;
};

type DeviceContextType = {
    isMobile: boolean;
    pageSpecificNavigation: AdditionalNavigationElement[];
    setPageSpecificNavigation: (navigation: AdditionalNavigationElement[]) => void;
};

const DeviceContext = createContext<DeviceContextType>({
    isMobile: false,
    pageSpecificNavigation: [],
    setPageSpecificNavigation: () => {},
});

type DeviceProviderProps = {
    children: ReactNode;
};

export const usePageSpecificNavigation = (navigationElements: AdditionalNavigationElement[]) => {
    const { isMobile, pageSpecificNavigation, setPageSpecificNavigation } = useDeviceContext();
    useEffect(() => {
        if (isMobile && !areNavigationsEqual(pageSpecificNavigation, navigationElements)) {
            setPageSpecificNavigation(navigationElements || []);
        }
        return () => setPageSpecificNavigation([]);
    }, [isMobile, navigationElements]);
};

export const DeviceProvider: FC<DeviceProviderProps> = ({ children }) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("md"));
    const [pageSpecificNavigation, setPageSpecificNavigation] = useState<AdditionalNavigationElement[]>([]);

    return (
        <DeviceContext.Provider value={{ isMobile, pageSpecificNavigation, setPageSpecificNavigation }}>
            {children}
        </DeviceContext.Provider>
    );
};

export const useDeviceContext = () => useContext(DeviceContext);
