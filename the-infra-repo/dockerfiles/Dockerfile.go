# ============================================================
# Dockerfile template — Go API Gateway
# Copy into the api-gateway repo as ./Dockerfile
#
# Produces a minimal scratch-based image (~10 MB)
# ============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM golang:1.22-alpine AS build

# Install certificates for TLS calls to Azure/GHCR
RUN apk add --no-cache ca-certificates git

WORKDIR /workspace

# Cache go modules before copying source
COPY go.mod go.sum ./
RUN go mod download

COPY . .

# Build a static binary — no CGO, no OS library deps
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -trimpath -ldflags="-s -w" -o /out/gateway ./cmd/gateway

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM scratch AS runtime

# Pull in TLS certs and user info from build stage
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=build /etc/passwd /etc/passwd

# Copy the compiled binary
COPY --from=build /out/gateway /gateway

# Run as non-root (uid 1000)
USER 1000

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD ["/gateway", "healthcheck"]

ENTRYPOINT ["/gateway"]
