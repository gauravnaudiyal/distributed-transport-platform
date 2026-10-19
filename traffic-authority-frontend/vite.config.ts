import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '');
    const proxyTarget = env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080';

    return {
        plugins: [react()],
        server: {
            proxy: {
                '/api': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/bookings': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/compatibility': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/routes': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/users': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/notifications': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/audit': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/authority': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
                '/health': {
                    target: proxyTarget,
                    changeOrigin: true,
                },
            },
        },
    };
});
