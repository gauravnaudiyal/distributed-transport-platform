FROM node:20-alpine AS build

WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/Tribunal-of-Human-Expansion/traffic-authority-frontend"
LABEL org.opencontainers.image.description="Traffic Authority Frontend"

# Cache node_modules layer
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --prefer-offline --no-audit

COPY . .

# Inject API base at build time (Vite inlines these). Same-origin Ingress: /api
ARG VITE_API_BASE_URL
ARG VITE_GATEWAY_BASE_URL
ARG VITE_AUTHORITY_API_BASE_URL
ARG VITE_ROUTE_API_BASE_URL
ARG VITE_COMPATIBILITY_API_BASE_URL
ARG VITE_AUDIT_API_BASE_URL
ARG VITE_USER_API_BASE_URL
ARG VITE_NOTIFICATION_API_BASE_URL
ARG VITE_DEMO_JWT
ARG VITE_API_BASE
ARG VITE_REGION
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_GATEWAY_BASE_URL=${VITE_GATEWAY_BASE_URL}
ENV VITE_AUTHORITY_API_BASE_URL=${VITE_AUTHORITY_API_BASE_URL}
ENV VITE_ROUTE_API_BASE_URL=${VITE_ROUTE_API_BASE_URL}
ENV VITE_COMPATIBILITY_API_BASE_URL=${VITE_COMPATIBILITY_API_BASE_URL}
ENV VITE_AUDIT_API_BASE_URL=${VITE_AUDIT_API_BASE_URL}
ENV VITE_USER_API_BASE_URL=${VITE_USER_API_BASE_URL}
ENV VITE_NOTIFICATION_API_BASE_URL=${VITE_NOTIFICATION_API_BASE_URL}
ENV VITE_DEMO_JWT=${VITE_DEMO_JWT}
ENV VITE_API_BASE=${VITE_API_BASE}
ENV VITE_REGION=${VITE_REGION}

RUN npm run build

FROM nginx:1.27-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# SPA routing config: all unknown paths → index.html
COPY --from=build /app/dist /usr/share/nginx/html
COPY <<'NGINX' /etc/nginx/conf.d/gtbs.conf
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets aggressively
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # No cache for index.html (always get latest shell)
    location = /index.html {
        add_header Cache-Control "no-cache";
    }

    # Health check endpoint (used by K8s liveness probe)
    location /health {
        access_log off;
        return 200 'ok';
        add_header Content-Type text/plain;
    }
}
NGINX

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -qO- http://localhost/health || exit 1
