import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { userApiService, type UserProfile } from '../../services/userApi';
import { Button } from '../common/Button';

export function UserProfilePanel() {
    const { userId, username } = useAuth();
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [fullName, setFullName] = useState(username || '');
    const [email, setEmail] = useState('');
    const [error, setError] = useState<string | null>(null);

    const resolvedUserId = userId || username || 'civilian-demo';

    const loadProfile = async () => {
        try {
            setError(null);
            const response = await userApiService.getUser(resolvedUserId);
            setProfile(response);
            setFullName(response.fullName);
            setEmail(response.email);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load profile');
        }
    };

    const createProfile = async () => {
        try {
            setError(null);
            const response = await userApiService.createUser({
                userId: resolvedUserId,
                fullName: fullName || resolvedUserId,
                email,
            });
            setProfile(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to create profile');
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-4">
                User Profile
            </h2>
            <div className="space-y-3">
                <input
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Full name"
                    className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                />
                <input
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Email"
                    className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                />
                <div className="grid grid-cols-2 gap-3">
                    <Button onClick={loadProfile}>Load Profile</Button>
                    <Button onClick={createProfile} variant="primary">
                        Create/Update Profile
                    </Button>
                </div>
            </div>
            {error && <p className="text-xs text-traffic-red font-mono mt-3">{error}</p>}
            {profile && (
                <div className="mt-4 bg-traffic-bg border border-traffic-border p-3 font-mono text-xs text-traffic-text-2">
                    <p>User ID: {profile.userId}</p>
                    <p>Name: {profile.fullName}</p>
                    <p>Email: {profile.email}</p>
                </div>
            )}
        </div>
    );
}
