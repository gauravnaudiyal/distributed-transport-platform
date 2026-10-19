#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.local.yml"

IMAGES=(
  "ghcr.io/tribunal-of-human-expansion/api-gateway:latest"
  "ghcr.io/tribunal-of-human-expansion/booking-service:latest"
  "ghcr.io/tribunal-of-human-expansion/journey-compatibility-service:latest"
  "ghcr.io/tribunal-of-human-expansion/observability-service:latest"
  "ghcr.io/tribunal-of-human-expansion/traffic-authority-frontend:latest"
)

usage() {
  cat <<'EOF'
Usage:
  ./scripts/stack.sh pull
  ./scripts/stack.sh up
  ./scripts/stack.sh down
  ./scripts/stack.sh status
  ./scripts/stack.sh logs [service]
  ./scripts/stack.sh fault <scenario>
  ./scripts/stack.sh recover <scenario>

Commands:
  pull     Optional GHCR pre-pull (only if USE_GHCR=true).
  up       Build local images and start stack.
  down     Stop and remove local stack.
  status   Show running container status and health.
  logs     Show logs for all services or a single service.
  fault    Trigger one error scenario.
  recover  Undo one error scenario.

Fault scenarios:
  redis-down           Stop Redis (compatibility should fall back to DB path).
  compatibility-down   Stop journey-compatibility service (gateway breaker path).
  booking-down         Stop booking service (gateway breaker path).
  notification-down    Stop notification service.
  route-down           Stop route-management service.
  observability-down   Stop observability service.
EOF
}

require_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required but not installed." >&2
    exit 1
  fi
}

ghcr_login_if_configured() {
  if [[ -n "${GHCR_USERNAME:-}" && -n "${GHCR_PAT:-}" ]]; then
    echo "Logging into GHCR as $GHCR_USERNAME..."
    printf '%s' "$GHCR_PAT" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin >/dev/null
  fi
}

pull_images() {
  require_docker
  if [[ "${USE_GHCR:-false}" != "true" ]]; then
    echo "USE_GHCR is not true; skipping GHCR pulls."
    echo "Local build mode is the default."
    return 0
  fi
  ghcr_login_if_configured
  echo "Pulling GHCR images..."
  for image in "${IMAGES[@]}"; do
    echo "  -> $image"
    if ! out="$(docker pull "$image" 2>&1)"; then
      if [[ "$out" == *"no matching manifest for linux/arm64"* ]]; then
        echo "     arm64 manifest missing; retrying with linux/amd64 emulation..."
        docker pull --platform linux/amd64 "$image"
      elif [[ "$out" == *"unauthorized"* || "$out" == *"denied"* ]]; then
        echo "GHCR authorization failed for $image." >&2
        echo "Export GHCR credentials and retry:" >&2
        echo "  export GHCR_USERNAME=\"<github-username>\"" >&2
        echo "  export GHCR_PAT=\"<github-token-with-read:packages>\"" >&2
        echo "  ./scripts/stack.sh pull" >&2
        return 1
      else
        echo "$out" >&2
        return 1
      fi
    else
      echo "$out"
    fi
  done
  echo "GHCR pre-pull completed."
}

stack_up() {
  require_docker
  if [[ "${USE_GHCR:-false}" == "true" ]]; then
    pull_images
  fi
  docker compose -f "$COMPOSE_FILE" build
  docker compose -f "$COMPOSE_FILE" up -d
  echo
  echo "Stack is up."
  echo "Gateway: http://localhost:8080/health"
  echo "JWT secret for local gateway: local-dev-secret"
  echo "Notification service: http://localhost:8084"
  echo "Route management service: http://localhost:8083"
  echo "Observability service: http://localhost:8085"
  echo
  echo "Tip: use './scripts/stack.sh fault redis-down' to test fallback paths."
}

stack_down() {
  require_docker
  docker compose -f "$COMPOSE_FILE" down
}

stack_status() {
  require_docker
  docker compose -f "$COMPOSE_FILE" ps
}

show_logs() {
  require_docker
  local service="${1:-}"
  if [[ -n "$service" ]]; then
    docker compose -f "$COMPOSE_FILE" logs -f "$service"
  else
    docker compose -f "$COMPOSE_FILE" logs -f
  fi
}

fault_on() {
  local scenario="${1:-}"
  case "$scenario" in
    redis-down)
      docker compose -f "$COMPOSE_FILE" stop redis
      ;;
    compatibility-down)
      docker compose -f "$COMPOSE_FILE" stop journey-compatibility-service
      ;;
    booking-down)
      docker compose -f "$COMPOSE_FILE" stop booking-service
      ;;
    notification-down)
      docker compose -f "$COMPOSE_FILE" stop notification-service
      ;;
    route-down)
      docker compose -f "$COMPOSE_FILE" stop route-management
      ;;
    observability-down)
      docker compose -f "$COMPOSE_FILE" stop observability-service
      ;;
    *)
      echo "Unknown fault scenario: $scenario" >&2
      usage
      exit 1
      ;;
  esac
}

fault_off() {
  local scenario="${1:-}"
  case "$scenario" in
    redis-down)
      docker compose -f "$COMPOSE_FILE" start redis
      ;;
    compatibility-down)
      docker compose -f "$COMPOSE_FILE" start journey-compatibility-service
      ;;
    booking-down)
      docker compose -f "$COMPOSE_FILE" start booking-service
      ;;
    notification-down)
      docker compose -f "$COMPOSE_FILE" start notification-service
      ;;
    route-down)
      docker compose -f "$COMPOSE_FILE" start route-management
      ;;
    observability-down)
      docker compose -f "$COMPOSE_FILE" start observability-service
      ;;
    *)
      echo "Unknown recover scenario: $scenario" >&2
      usage
      exit 1
      ;;
  esac
}

main() {
  local cmd="${1:-}"
  case "$cmd" in
    pull) pull_images ;;
    up) stack_up ;;
    down) stack_down ;;
    status) stack_status ;;
    logs) show_logs "${2:-}" ;;
    fault) fault_on "${2:-}" ;;
    recover) fault_off "${2:-}" ;;
    ""|-h|--help|help) usage ;;
    *)
      echo "Unknown command: $cmd" >&2
      usage
      exit 1
      ;;
  esac
}

main "${1:-}"
