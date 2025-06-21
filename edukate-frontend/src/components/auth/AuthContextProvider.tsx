import { createContext, FC, ReactNode, useContext } from "react";
import { useWhoamiQuery } from "../../http/auth";
import { User } from "../../types/User";

/**
 * Auth context interface that provides user information and methods to manage authentication
 */
interface AuthContextType {
    user: User | undefined;
    isAuthorized: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

/**
 * Provider component that makes auth context available to all child components
 */
export const AuthProvider: FC<AuthProviderProps> = ({ children }) => {
    const { data: user } = useWhoamiQuery();

    const authContextValue = { user: user || undefined, isAuthorized: user != undefined };

    return (
        <AuthContext.Provider value={authContextValue}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * Custom hook to use the auth context
 * @returns The auth context
 * @throws Error if used outside AuthProvider
 */
export const useAuthContext = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuthContext must be used within an AuthProvider');
    }
    return context;
};
