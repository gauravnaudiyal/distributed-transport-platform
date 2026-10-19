#!/usr/bin/env bash
set -euo pipefail

SECRET="${JWT_SECRET:-local-dev-secret}"
RAW_OUTPUT="false"
if [[ "${1:-}" == "--raw" ]]; then
  RAW_OUTPUT="true"
  shift
fi
SUBJECT="${1:-demo-user}"
TTL_SECONDS="${2:-3600}"

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required to generate JWT." >&2
  exit 1
fi

NOW="$(date +%s)"
EXP="$((NOW + TTL_SECONDS))"

TOKEN="$(JWT_SECRET="$SECRET" JWT_SUB="$SUBJECT" JWT_IAT="$NOW" JWT_EXP="$EXP" python3 - <<'PY'
import base64
import hashlib
import hmac
import json
import os

def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")

secret = os.environ["JWT_SECRET"].encode("utf-8")
sub = os.environ["JWT_SUB"]
iat = int(os.environ["JWT_IAT"])
exp = int(os.environ["JWT_EXP"])

header = {"alg": "HS256", "typ": "JWT"}
payload = {"sub": sub, "iat": iat, "exp": exp}

header_b64 = b64url(json.dumps(header, separators=(",", ":"), sort_keys=True).encode("utf-8"))
payload_b64 = b64url(json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8"))
signing_input = f"{header_b64}.{payload_b64}".encode("ascii")
signature = hmac.new(secret, signing_input, hashlib.sha256).digest()

print(f"{header_b64}.{payload_b64}.{b64url(signature)}")
PY
)"

echo "$TOKEN"
if [[ "$RAW_OUTPUT" != "true" ]]; then
  echo
  echo "Generated JWT for sub='$SUBJECT' exp=$EXP"
  echo "To export for demo checks:"
  echo "export DEMO_JWT=\"$TOKEN\""
fi
