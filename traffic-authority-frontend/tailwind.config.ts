import type { Config } from 'tailwindcss';

export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                traffic: {
                    bg: '#0a0b0d',
                    'bg-2': '#0e1014',
                    'bg-3': '#13161b',
                    panel: '#111418',
                    border: '#1e2530',
                    'border-2': '#2a3340',
                    red: '#c0392b',
                    'red-2': '#e74c3c',
                    amber: '#d4830a',
                    'amber-2': '#f39c12',
                    green: '#00ff88',
                    'green-2': '#00cc6a',
                    blue: '#4fc3f7',
                    'blue-2': '#81d4fa',
                    text: '#c8d6e5',
                    'text-2': '#8899aa',
                    'text-3': '#556677',
                    white: '#eef2f7',
                },
            },
            fontFamily: {
                mono: ['Share Tech Mono', 'monospace'],
                barlow: ['Barlow Condensed', 'sans-serif'],
                courier: ['Courier Prime', 'monospace'],
            },
            boxShadow: {
                glow: '0 0 10px rgba(0,255,136,0.4)',
                'green-glow': '0 0 10px rgba(0,255,136,0.4)',
                'red-glow': '0 0 10px rgba(231,76,60,0.4)',
                'amb-glow': '0 0 10px rgba(243,156,18,0.4)',
                'blue-glow': '0 0 10px rgba(79,195,247,0.4)',
            },
            opacity: {
                '3': '0.03',
                '4': '0.04',
                '5': '0.05',
                '8': '0.08',
            },
            animation: {
                scanMove: 'scanMove 8s linear infinite',
                scanH: 'scanH 4s ease-in-out infinite',
                'crest-pulse': 'crestPulse 3s ease-in-out infinite',
                blink: 'blink 2s step-end infinite',
                'dot-pulse': 'dotPulse 1.5s ease-in-out infinite',
                'amb-pulse': 'ambPulse 1s ease-in-out infinite',
                'red-flash': 'redFlash 0.8s ease-in-out infinite',
                shimmer: 'shimmer 2s ease-in-out infinite',
                tickerScroll: 'tickerScroll 25s linear infinite',
                glow: 'glow 2s ease-in-out infinite',
            },
            keyframes: {
                scanMove: {
                    '0%': { backgroundPosition: '0 0' },
                    '100%': { backgroundPosition: '0 100px' },
                },
                scanH: {
                    '0%, 100%': { transform: 'scaleX(0.3)', opacity: '0.4' },
                    '50%': { transform: 'scaleX(1)', opacity: '1' },
                },
                crestPulse: {
                    '0%, 100%': { boxShadow: '0 0 0 0 rgba(192,57,43,0.3)' },
                    '50%': { boxShadow: '0 0 12px 4px rgba(192,57,43,0.15), inset 0 0 8px rgba(192,57,43,0.1)' },
                },
                blink: {
                    '0%, 100%': { opacity: '1' },
                    '50%': { opacity: '0.3' },
                },
                dotPulse: {
                    '0%, 100%': { boxShadow: '0 0 0 0 rgba(0,255,136,0.6)' },
                    '50%': { boxShadow: '0 0 0 5px rgba(0,255,136,0)' },
                },
                ambPulse: {
                    '0%, 100%': { opacity: '1' },
                    '50%': { opacity: '0.5' },
                },
                redFlash: {
                    '0%, 100%': { opacity: '1' },
                    '50%': { opacity: '0.3' },
                },
                shimmer: {
                    '0%, 100%': { opacity: '0' },
                    '50%': { opacity: '1' },
                },
                tickerScroll: {
                    '0%': { transform: 'translateX(0)' },
                    '100%': { transform: 'translateX(-50%)' },
                },
                glow: {
                    '0%, 100%': { textShadow: '0 0 10px rgba(0,255,136,0.4)' },
                    '50%': { textShadow: '0 0 25px rgba(0,255,136,0.7)' },
                },
            },
        },
    },
    plugins: [],
} satisfies Config;
