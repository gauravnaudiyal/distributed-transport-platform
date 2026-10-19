import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { userApiService } from '../../services/userApi';
import { Button } from '../common/Button';

export function UserPreferencesPanel() {
    const { userId, username } = useAuth();
    const [emailEnabled, setEmailEnabled] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [savedState, setSavedState] = useState<string | null>(null);

    const resolvedUserId = userId || username || 'civilian-demo';

    const loadPreferences = async () => {
        try {
            setError(null);
            const response = await userApiService.getPreferences(resolvedUserId);
            setEmailEnabled(response.emailEnabled);
            setSavedState('Preferences loaded');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load preferences');
        }
    };

    const savePreferences = async () => {
        try {
            setError(null);
            await userApiService.updatePreferences(resolvedUserId, { emailEnabled });
            setSavedState('Preferences saved');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to save preferences');
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-4">
                Notification Preferences
            </h2>
            <div className="flex items-center justify-between bg-traffic-bg border border-traffic-border p-3 mb-3">
                <span className="font-mono text-xs text-traffic-text-2">Email Enabled</span>
                <input
                    type="checkbox"
                    checked={emailEnabled}
                    onChange={(e) => setEmailEnabled(e.target.checked)}
                />
            </div>
            <div className="grid grid-cols-2 gap-3">
                <Button onClick={loadPreferences}>Load Preferences</Button>
                <Button onClick={savePreferences} variant="primary">
                    Save Preferences
                </Button>
            </div>
            {savedState && (
                <p className="text-xs text-traffic-green font-mono mt-3">{savedState}</p>
            )}
            {error && <p className="text-xs text-traffic-red font-mono mt-3">{error}</p>}
        </div>
    );
}
