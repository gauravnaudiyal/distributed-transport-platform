# GTBS Meta Repository

This repository aggregates the GTBS codebase as Git submodules and provides a single local Docker workflow to run the platform end-to-end.

## Included submodules

- `api-gateway`
- `booking-service`
- `journey-compatibility-service`
- `notification-service`
- `observability-service`
- `route-management`
- `admin-authority-service`
- `traffic-authority-frontend`
- `the-infra-repo`

## Prerequisites

- Git 2.30+
- Docker Desktop (or Docker Engine + Compose plugin)
- Bash
- Python 3 (only needed for generating a demo JWT)

## Clone this meta repo

If you are cloning this repository from GitHub:

```bash
git clone <repo-url>.git
cd the-gtbs-full
git submodule update --init --recursive
```

If submodule branches move later:

```bash
git submodule sync --recursive
git submodule update --init --recursive --remote
```

## Get Latest Sub Repos Changes 

```bash
make submodules
```

## Run locally

Start the full local stack (builds images from submodule source):

```bash
make up
```

Services:

- API Gateway: `http://localhost:8080`
- Booking: `http://localhost:8081`
- Journey Compatibility: `http://localhost:8082`
- Route Management: `http://localhost:8083`
- Notification: `http://localhost:8084`
- Observability: `http://localhost:8085`
- Admin Authority: `http://localhost:8086`
- Traffic Authority Frontend: `http://localhost:3000`

Stop everything:

```bash
make down
```

Check status/logs:

```bash
make status
make logs
make logs-booking-service
```

## Makefile shortcuts

Run `make help` to list all commands.

Common setup commands:

```bash
make init
make submodules-remote
make submodules-main
```

## JWT for protected endpoints

The local gateway uses `local-dev-secret` for HS256 in dev mode.

Generate a token:

```bash
./scripts/set-demo-jwt.sh
```

Export a raw token for scripts:

```bash
export DEMO_JWT="$(./scripts/set-demo-jwt.sh --raw demo-user 3600)"
```

## Run smoke checks

```bash
export DEMO_JWT="$(./scripts/set-demo-jwt.sh --raw demo-user 3600)"
make smoke
```

## Fault-injection helpers

```bash
make fault SCENARIO=redis-down
make recover SCENARIO=redis-down
```

Supported scenarios:

- `redis-down`
- `compatibility-down`
- `booking-down`
- `notification-down`
- `route-down`
- `observability-down`

## Notes

- This repo intentionally tracks service commit SHAs via submodules for reproducible demos.
