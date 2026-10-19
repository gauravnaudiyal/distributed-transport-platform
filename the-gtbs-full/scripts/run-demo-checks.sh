#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
JWT="${DEMO_JWT:-}"
FAULT_SCENARIO="${FAULT_SCENARIO:-}"

PASS=0
FAIL=0
BODY_FILE="/tmp/gtbs_demo_body.$$"

usage() {
  cat <<'EOF'
Usage:
  ./scripts/run-demo-checks.sh
  DEMO_JWT="<jwt>" ./scripts/run-demo-checks.sh
  DEMO_JWT="<jwt>" FAULT_SCENARIO=compatibility-down ./scripts/run-demo-checks.sh

Env vars:
  BASE_URL        Default: http://localhost:8080
  DEMO_JWT        JWT for protected happy-path endpoints
  FAULT_SCENARIO  Optional: compatibility-down | booking-down | redis-down | notification-down | route-down | observability-down
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd python3

check_code() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  if [[ "$expected" == "$actual" ]]; then
    echo "PASS: $name -> $actual"
    PASS=$((PASS + 1))
  else
    echo "FAIL: $name -> expected $expected, got $actual"
    FAIL=$((FAIL + 1))
  fi
}

request_code() {
  local method="$1"
  local url="$2"
  local data="${3:-}"
  local auth="${4:-}"
  local args=(-s -o "$BODY_FILE" -w "%{http_code}" -X "$method" "$url")
  if [[ -n "$auth" ]]; then
    args+=(-H "Authorization: Bearer $auth")
  fi
  if [[ -n "$data" ]]; then
    args+=(-H "Content-Type: application/json" -d "$data")
  fi
  curl "${args[@]}"
}

echo "== GTBS demo checks =="
echo "Base URL: $BASE_URL"

# Public health
code="$(request_code GET "$BASE_URL/health")"
check_code "gateway health" "200" "$code"

# Auth error checks
code="$(request_code GET "$BASE_URL/api/bookings/nonexistent")"
check_code "missing auth header" "401" "$code"
code="$(request_code GET "$BASE_URL/api/bookings/nonexistent" "" "invalid.token.value")"
check_code "invalid jwt" "401" "$code"

BOOKING_ID=""
if [[ -n "$JWT" ]]; then
  now="$(date +%s)"
  start="2026-04-08T08:00:00Z"
  end="2026-04-08T09:00:00Z"
  idem="run-demo-$now"
  booking_payload="$(cat <<EOF
{
  "user_id":"demo-user-001",
  "origin":"Dublin-6",
  "destination":"Dublin-2",
  "idempotency_key":"$idem",
  "time_window_start":"$start",
  "time_window_end":"$end"
}
EOF
)"

  code="$(request_code POST "$BASE_URL/api/bookings" "$booking_payload" "$JWT")"
  if [[ "$code" == "200" || "$code" == "201" ]]; then
    echo "PASS: create booking -> $code"
    PASS=$((PASS + 1))
    BOOKING_ID="$(BODY_FILE="$BODY_FILE" python3 - <<'PY'
import json
from pathlib import Path
p = Path(__import__("os").environ["BODY_FILE"])
try:
    d = json.loads(p.read_text())
except Exception:
    print("")
    raise SystemExit(0)
print(d.get("booking_id") or d.get("bookingId") or "")
PY
)"
  else
    echo "FAIL: create booking -> expected 200/201, got $code"
    FAIL=$((FAIL + 1))
  fi

  if [[ -n "$BOOKING_ID" ]]; then
    code="$(request_code GET "$BASE_URL/api/bookings/$BOOKING_ID" "" "$JWT")"
    check_code "get booking by id" "200" "$code"
    code="$(request_code GET "$BASE_URL/api/bookings/$BOOKING_ID/verify" "" "$JWT")"
    check_code "verify booking" "200" "$code"
    code="$(request_code POST "$BASE_URL/api/bookings/$BOOKING_ID/cancel" "" "$JWT")"
    check_code "cancel booking" "200" "$code"
    code="$(request_code POST "$BASE_URL/api/bookings/$BOOKING_ID/cancel" "" "$JWT")"
    check_code "cancel again conflict" "409" "$code"
  else
    echo "WARN: booking id not parsed; skipping booking follow-up checks"
  fi

  bad_compat_payload='{"bookingId":"compat-bad-001","routeId":"dublin6-dublin2","routeName":"Dublin6-Dublin2","travelDate":"2026-04-08","travelTime":"08:00:00","vehicleType":"BIKE","passengerCount":0,"driverId":"driver-001"}'
  code="$(request_code POST "$BASE_URL/compatibility/check-compatibility" "$bad_compat_payload" "$JWT")"
  check_code "compatibility validation error" "400" "$code"

  if [[ -n "$FAULT_SCENARIO" ]]; then
    echo "Applying fault: $FAULT_SCENARIO"
    ./scripts/stack.sh fault "$FAULT_SCENARIO" >/dev/null
    trap './scripts/stack.sh recover "$FAULT_SCENARIO" >/dev/null || true' EXIT
    sleep 2
    code="$(request_code POST "$BASE_URL/compatibility/check-compatibility" "$bad_compat_payload" "$JWT")"
    if [[ "$code" =~ ^5 ]]; then
      echo "PASS: fault response returned 5xx ($code)"
      PASS=$((PASS + 1))
    else
      echo "FAIL: fault response expected 5xx, got $code"
      FAIL=$((FAIL + 1))
    fi
    ./scripts/stack.sh recover "$FAULT_SCENARIO" >/dev/null
    trap - EXIT
  fi
else
  echo "INFO: DEMO_JWT not set; skipping protected happy-path checks."
fi

echo
echo "Summary: PASS=$PASS FAIL=$FAIL"
if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi
