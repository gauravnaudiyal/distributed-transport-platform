import React, { createContext, useContext } from 'react';
import { useAuthStore } from '../store/auth';
import type { UserRole } from '../store/auth';

interface AuthContextType {
    isLoggedIn: boolean;
    userRole: UserRole | null;
    username: string | null;
    userId: string | null;
    login: (username: string, password: string, jwtToken?: string) => Promise<void>;
    logout: () => void;
    setRole: (role: UserRole) => void;
    toggleTestRole: () => void;
    isCivilian: boolean;
    isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const {
        isLoggedIn,
        userRole,
        username,
        userId,
        login,
        logout,
        setRole,
        toggleTestRole,
    } =
        useAuthStore();

    const value: AuthContextType = {
        isLoggedIn,
        userRole,
        username,
        userId,
        login,
        logout,
        setRole,
        toggleTestRole,
        isCivilian: userRole === 'civilian',
        isAdmin: userRole === 'admin',
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};
