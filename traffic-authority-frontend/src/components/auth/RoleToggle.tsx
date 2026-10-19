import { useAuth } from '../../context/AuthContext';

export const RoleToggle = () => {
    const { userRole, toggleTestRole } = useAuth();

    return (
        <button
            onClick={toggleTestRole}
            className="text-xs bg-traffic-accent/20 border border-traffic-accent px-2 py-1 rounded hover:bg-traffic-accent/40 transition-colors text-traffic-accent"
            title="Click to toggle between admin and civilian roles"
        >
            Role: {userRole}
        </button>
    );
};
