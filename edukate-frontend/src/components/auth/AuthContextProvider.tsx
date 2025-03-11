import { createContext, FC, ReactNode, useContext, useEffect, useState } from "react";
import { useWhoamiQuery } from "../../http/auth";
import { User } from "../../types/User";

type AuthContextType = {
    user: User | undefined;
    setUser: (user: User | undefined) => void;
};

const AuthContext = createContext<AuthContextType>({
    user: undefined,
    setUser: () => {},
});

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User>();
    const authQuery = useWhoamiQuery();

    useEffect(
        () => { if (authQuery.data && authQuery.isSuccess) { setUser(authQuery.data) }},
        [authQuery.isSuccess],
    );

    return (
        <AuthContext.Provider value={{ user: user, setUser: setUser }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuthContext = () => useContext(AuthContext);
