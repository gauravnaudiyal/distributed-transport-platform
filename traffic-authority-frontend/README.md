# Traffic Authority Frontend

Frontend demo app for GTBS service integration. This UI now exercises booking, compatibility, route management, authority actions, audit/observability, and user/notification flows.

## Prerequisites

- Node.js 20+
- npm 10+
- Running backend services (preferably through API gateway on `http://localhost:8080`)

## Setup

1. Copy env template:

   ```bash
   cp .env.example .env
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Start frontend:

   ```bash
   npm run dev
   ```

By default, Vite proxies API routes to `VITE_DEV_PROXY_TARGET` (gateway), avoiding browser CORS in local development.

For protected gateway routes (`/routes`, `/compatibility`, `/authority`, etc.), paste a valid JWT in the login modal. You can generate one locally:

```bash
./local/set-demo-jwt.sh --raw demo-user 3600
```

## Environment Variables

- `VITE_API_BASE_URL` default booking API base (`/api` recommended in dev).
- `VITE_GATEWAY_BASE_URL` root gateway URL for non-`/api/*` routes (`''` recommended in dev).
- `VITE_DEV_PROXY_TARGET` Vite proxy target for local development (usually `http://localhost:8080`).
- `VITE_COMPATIBILITY_API_BASE_URL` optional direct override for compatibility routes.
- `VITE_ROUTE_API_BASE_URL` optional direct override for route routes.
- `VITE_AUDIT_API_BASE_URL` optional direct override for observability routes.
- `VITE_USER_API_BASE_URL` optional direct override for user routes.
- `VITE_NOTIFICATION_API_BASE_URL` optional direct override for notification routes.
- `VITE_AUTHORITY_API_BASE_URL` optional direct authority service base URL.

If override vars are empty, booking uses `VITE_API_BASE_URL` and other service clients use `VITE_GATEWAY_BASE_URL`. With dev proxy enabled and relative paths, browser CORS is avoided.

## Service-backed Demo Flows

### 1) Civilian Journey Compatibility and Booking

1. Login as civilian (`civilian` / `civic123`).
2. Open `Request Passage`.
3. Select origin, destination, date/hour, vehicle type, and passenger count.
4. Submit request.
5. Expected orchestration:
   - compatibility check
   - route decomposition + map version preview
   - booking create on approval

### 2) Booking Lifecycle and Audit

In `Booking Status`:

- `Refresh Booking` -> `GET /bookings/{bookingId}`
- `Verify Token` -> `GET /bookings/{bookingId}/verify`
- `Cancel Booking` -> `POST /bookings/{bookingId}/cancel`
- `Load Booking Audit` -> `GET /audit/bookings/{bookingId}`

### 3) Admin Policy Actions

1. Toggle to admin role.
2. In authority dashboard:
   - create closure
   - create capacity override
3. These call `admin-authority-service` endpoints and set a policy-update lock while in-flight.
4. Civilians cannot submit booking when policy lock is active.

### 4) Authority Verification and Segment Insights

In authority dashboard:

- Verify booking via authority endpoint.
- Load segment forecast.
- Load authority segment audit.

### 5) User Profile and Notifications

Open `Profile & Alerts`:

- Create/load user profile.
- Load/save notification preferences.
- Fetch notification log for current user.

## Suggested Local Startup Order

1. booking-service
2. journey-compatibility-service
3. route-management
4. admin-authority-service
5. observability-service
6. notification-service
7. api-gateway
8. traffic-authority-frontend

## Quick Validation Checklist

- Compatible journey returns booking.
- Incompatible journey is blocked with reason.
- Admin policy apply toggles booking lock during request.
- Booking verify/cancel/refresh works.
- Booking/segment audit records are visible.
- Profile/preferences/notifications endpoints are reachable from UI.
