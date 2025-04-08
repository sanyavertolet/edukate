import { createContext, FC, ReactNode, useContext, useEffect, useState } from "react";
import { useMediaQuery, useTheme } from "@mui/material";
import { AdditionalNavigationElement } from "./AdditionalNavigationElement";
import { areNavigationsEqual } from "../../utils/utils";

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

interface DeviceProviderProps {
    children: ReactNode;
}

export const usePageSpecificNavigation = (navigationElements: AdditionalNavigationElement[]) => {
    const { isMobile, pageSpecificNavigation, setPageSpecificNavigation } = useDeviceContext();
    useEffect(
        () => {
            if (isMobile && !areNavigationsEqual(pageSpecificNavigation, navigationElements)) {
                setPageSpecificNavigation(navigationElements || []);
            }
            return () => setPageSpecificNavigation([]);
        }, [isMobile, navigationElements]
    );
}

export const DeviceProvider: FC<DeviceProviderProps> = ({ children }) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [pageSpecificNavigation, setPageSpecificNavigation] = useState<AdditionalNavigationElement[]>([]);

    return (
        <DeviceContext.Provider value={{ isMobile, pageSpecificNavigation, setPageSpecificNavigation }}>
            {children}
        </DeviceContext.Provider>
    );
}

export const useDeviceContext = () => useContext(DeviceContext);
