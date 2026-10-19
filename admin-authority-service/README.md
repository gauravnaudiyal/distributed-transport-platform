# admin-authority-service

Spring Boot implementation of the **GTBS Traffic Authority Interface** described in the architecture: authenticated traffic-authority operators submit **closures**, **restrictions**, and **capacity overrides**; those mutations are applied **synchronously through Route Management** (not by writing booking tables directly); **Kafka** publishes non–correctness-critical fan-out after a successful apply; **booking verification** and **segment forecast** are proxied to peer services.

## Run locally

- **PostgreSQL** with database `gtbs_authority` (or override `DATABASE_URL`).
- Optional: **Kafka** (set `KAFKA_PUBLISH_ENABLED=true` and remove Kafka auto-config exclude if you use the `dev` profile defaults).

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Default port: **8083**. OpenAPI UI: **http://localhost:8083/api/swagger-ui** (when security allows).

## Security

- **`dev`** profile: JWT security is off for quick local runs (and OAuth2 resource server auto-config is disabled so you do not need `JWT_ISSUER_URI`).
- **Default / production**: `/authority/**` requires JWT with claim `roles` containing `TRAFFIC_AUTHORITY` (mapped to `ROLE_TRAFFIC_AUTHORITY`). You **must** set **`JWT_ISSUER_URI`** to your OpenID issuer (Entra ID, Keycloak, etc.); an empty issuer will break startup when security is on.

## Downstream integration (contracts)

Configure `ROUTE_MANAGEMENT_BASE_URL` and `BOOKING_SERVICE_BASE_URL`. This service calls:

| Purpose | Method | Route (on downstream) |
|--------|--------|------------------------|
| Closure | `POST` | `/api/v1/internal/authority/closures` |
| Restriction | `POST` | `/api/v1/internal/authority/restrictions` |
| Capacity override | `POST` | `/api/v1/internal/authority/capacity-overrides` |
| Segment forecast | `GET` | `/api/v1/internal/segments/{segmentId}/forecast` |
| Booking verify | `GET` | `/api/v1/internal/bookings/{bookingId}/authority-verification` |

Apply endpoints must return JSON: `{ "mapVersion": number, "policyReference": "string" }`.

## Authority API surface

- `POST /authority/closures`
- `POST /authority/restrictions`
- `POST /authority/capacity-overrides`
- `GET /authority/bookings/{bookingId}/verify`
- `GET /authority/segments/{segmentId}/forecast`
- `GET /authority/audit/{segmentId}`

## Container image

Build locally:

```bash
docker build -t admin-authority-service:local .
docker run --rm -p 8083:8083 -e SPRING_PROFILES_ACTIVE=dev admin-authority-service:local
```

(`SPRING_PROFILES_ACTIVE=dev` matches the repo’s local-dev defaults; for a real deploy use `prod` or your own profile and set env vars.)

## GitHub Actions → GHCR

Workflow: [`.github/workflows/publish-ghcr.yml`](.github/workflows/publish-ghcr.yml).

- **Triggers**: pushes to `main` (image tagged `latest` and Git SHA), SemVer tags `v*.*.*` (e.g. `v1.2.3`), and **workflow_dispatch**.
- **Registry**: `ghcr.io` with `GITHUB_TOKEN` (`packages: write` is set on the job).
- **Image name**: `ghcr.io/<owner>/<repo>` in **lowercase** (required by OCI).

**Repository settings**

1. **Actions → General**: allow workflows to read/write packages (default for `GITHUB_TOKEN` in many orgs; if pushes fail, enable “Read and write permissions” for workflows or use a PAT in a secret).
2. **Package visibility**: after the first push, open the package in GitHub → **Package settings** → set **Public** (simplest for pull-only clusters) or keep **Private** and use a pull secret on AKS (below).

## Deploy on Azure Kubernetes Service (AKS)

### 1. Prerequisites

- An AKS cluster and [`kubectl` configured](https://learn.microsoft.com/en-us/azure/aks/learn-kubernetes-cli) for it (`az aks get-credentials`).
- This app needs **PostgreSQL**, **Kafka** (if `KAFKA_PUBLISH_ENABLED=true`), and reachable **Route Management** / **Booking** URLs inside the cluster (or peer VNet).

### 2. Pull private images from GHCR

If the package is **private**, create a classic **Personal Access Token** with `read:packages` (and `read` if the image is under another org). Then:

```bash
kubectl create secret docker-registry ghcr-pull \
  --namespace=gtbs \
  --docker-server=ghcr.io \
  --docker-username=YOUR_GITHUB_USERNAME \
  --docker-password=YOUR_PAT
```

Reference `ghcr-pull` under `imagePullSecrets` (already set in [`deploy/k8s/deployment.yaml`](deploy/k8s/deployment.yaml)).

### 3. Config and secrets

1. Copy [`deploy/k8s/configmap.example.yaml`](deploy/k8s/configmap.example.yaml) → `configmap.yaml`, set service URLs and region.
2. Create secrets safely (prefer **Azure Key Vault** + [CSI driver](https://learn.microsoft.com/en-us/azure/aks/csi-secrets-store-driver), or `kubectl create secret generic` with literals). Keys must match the `secretKeyRef` names in `deployment.yaml`.

Apply:

```bash
kubectl apply -n gtbs -f configmap.yaml
kubectl apply -n gtbs -f your-secrets.yaml
```

### 4. Point the Deployment at your image

Edit [`deploy/k8s/deployment.yaml`](deploy/k8s/deployment.yaml): replace `ghcr.io/OWNER/admin-authority-service:latest` with your image, for example `ghcr.io/myorg/admin-authority-service:latest` or a SHA tag from CI.

Apply:

```bash
kubectl apply -n gtbs -f deploy/k8s/deployment.yaml
kubectl apply -n gtbs -f deploy/k8s/service.yaml
```

### 5. Ingress / API exposure

Expose with your ingress controller (App Gateway, NGINX, etc.) or an internal `LoadBalancer`/`ClusterIP` + port-forward for testing. Set **`JWT_ISSUER_URI`** (and OAuth2 client config at the gateway if used) so JWT validation matches your Entra ID / Keycloak issuer.

### 6. (Optional) Attach ACR instead of GHCR

If you prefer **Azure Container Registry**, add a step after build to `docker tag` + `az acr login` + `docker push`, or use [ACR Tasks](https://learn.microsoft.com/en-us/azure/container-registry/container-registry-tutorial-quick-task). AKS can then use an **[AcrPull role assignment](https://learn.microsoft.com/en-us/azure/aks/cluster-nodes-vm-sso-acr-pull)** on the kubelet identity instead of `imagePullSecrets`.
