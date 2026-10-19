import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../common/Button';

export const LoginModal = () => {
    const { login } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const envToken = import.meta.env.VITE_DEMO_JWT as string | undefined;
    const [jwtToken, setJwtToken] = useState(
        () => envToken || localStorage.getItem('gtbs_demo_jwt') || ''
    );
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            localStorage.setItem('gtbs_demo_jwt', jwtToken);
            await login(username, password, jwtToken);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Login failed');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-traffic-bg border border-traffic-accent p-8 w-96">
                <h1 className="text-2xl font-bold text-traffic-accent mb-6">Traffic Authority</h1>

                <form onSubmit={handleSubmit} className="space-y-4 mt-6">
                    <div>
                        <label className="block text-traffic-text text-sm mb-2">Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2  focus:outline-none focus:border-traffic-bright"
                            placeholder="Enter username"
                            disabled={isLoading}
                        />
                    </div>

                    <div>
                        <label className="block text-traffic-text text-sm mb-2">Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 focus:outline-none focus:border-traffic-bright"
                            placeholder="Enter password"
                            disabled={isLoading}
                        />
                    </div>

                    <div>
                        <label className="block text-traffic-text text-sm mb-2">
                            Gateway JWT {envToken && <span className="text-traffic-bright text-xs">(auto-loaded from env)</span>}
                        </label>
                        <textarea
                            value={envToken ? '••••••••••••••••' : jwtToken}
                            onChange={(e) => !envToken && setJwtToken(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 focus:outline-none focus:border-traffic-bright font-mono text-xs"
                            placeholder={envToken ? "JWT loaded from environment variable" : "Paste DEMO_JWT from local/set-demo-jwt.sh"}
                            rows={3}
                            disabled={isLoading || !!envToken}
                            readOnly={!!envToken}
                        />
                    </div>

                    {error && <p className="text-red-400 text-sm">{error}</p>}

                    <Button
                        onClick={handleSubmit}
                        disabled={isLoading || !username || !password}
                        className="w-full"
                    >
                        {isLoading ? 'Logging in...' : 'Login'}
                    </Button>
                </form>

                {/* Test credentials info */}
                <div className="mt-6 pt-6 border-t border-traffic-accent text-xs text-traffic-text/70">
                    <p className="mb-2">Test Credentials:</p>
                    <p>Admin: admin / admin123</p>
                    <p>Civilian: civilian / civic123</p>
                    <p className="mt-2">
                        Generate JWT: ./local/set-demo-jwt.sh --raw demo-user 3600
                    </p>
                </div>
            </div>
        </div>
    );
};
