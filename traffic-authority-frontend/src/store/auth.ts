import { create } from 'zustand';

export type UserRole = 'admin' | 'civilian';

interface AuthState {
    isLoggedIn: boolean;
    userRole: UserRole | null;
    username: string | null;
    userId: string | null;
    token: string | null;
    login: (username: string, password: string, jwtToken?: string) => Promise<void>;
    logout: () => void;
    setRole: (role: UserRole) => void;
    toggleTestRole: () => void; // For development/testing
}

export const useAuthStore = create<AuthState>((set, get) => ({
    isLoggedIn: false,
    userRole: null,
    username: null,
    userId: null,
    token: null,

    login: async (username: string, password: string, jwtToken?: string) => {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 500));

        // Simple test credentials
        let role: UserRole = 'civilian';
        if (username === 'admin' && password === 'admin123') {
            role = 'admin';
        } else if (username === 'civilian' && password === 'civic123') {
            role = 'civilian';
        } else {
            throw new Error('Invalid credentials');
        }

        const envToken = import.meta.env.VITE_DEMO_JWT as string | undefined;
        const resolvedToken = jwtToken?.trim() || envToken || null;

        set({
            isLoggedIn: true,
            userRole: role,
            username,
            userId: username,
            token: resolvedToken,
        });
    },

    logout: () => {
        set({
            isLoggedIn: false,
            userRole: null,
            username: null,
            userId: null,
            token: null,
        });
    },

    setRole: (role: UserRole) => {
        set({ userRole: role });
    },

    toggleTestRole: () => {
        const current = get().userRole;
        const newRole = current === 'admin' ? 'civilian' : 'admin';
        set({ userRole: newRole });
    },
}));
