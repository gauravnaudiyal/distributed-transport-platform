# Kubernetes manifests (GTBS on AKS)

For a cluster with **AKS Application routing** enabled (`az aks approuting enable`), use `ingressClassName: webapprouting.kubernetes.azure.com`.

## 1. Build and push images

From each service repo (adjust registry/org; ACR is fine too):

```bash
# API gateway (repo root contains Dockerfile)
docker build -t ghcr.io/<org>/api-gateway:latest /path/to/api-gateway
docker push ghcr.io/<org>/api-gateway:latest

# Frontend — embed API prefix so the SPA calls /api/... on the same host as Ingress
docker build \
  --build-arg VITE_API_BASE=/api \
  --build-arg VITE_API_BASE_URL=/api \
  -t ghcr.io/<org>/traffic-authority-frontend:latest \
  /path/to/traffic-authority-frontend
docker push ghcr.io/<org>/traffic-authority-frontend:latest
```

Replace placeholder images in the YAML files before apply.

## 2. Image pull secret (GHCR private)

```bash
kubectl create secret docker-registry ghcr-pull \
  --docker-server=ghcr.io \
  --docker-username=<github-username> \
  --docker-password=<pat-with-read-packages> \
  -n default
```

Uncomment `imagePullSecrets` in the Deployments when using private images.

## 3. Apply workloads

```bash
kubectl apply -f the-infra-repo/infra/k8s/api-gateway.yaml
kubectl apply -f the-infra-repo/infra/k8s/traffic-authority-frontend.yaml
kubectl apply -f the-infra-repo/infra/k8s/gtbs-ingress.yaml
```

## 4. Ingress IP

```bash
kubectl get svc -n app-routing-system nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}{"\n"}'
curl -sS "http://<INGRESS_IP>/health"        # gateway
curl -sS "http://<INGRESS_IP>/api/bookings"  # GET via gateway → booking (adjust if 405)
```

Open `http://<INGRESS_IP>/` for the SPA. The gateway exposes **`/api/bookings`** without JWT; legacy **`/bookings/*`** still expects `Authorization: Bearer ...`.

## 5. Optional env vars

When notification and observability run in-cluster, add to `api-gateway` Deployment:

- `USER_SERVICE` → `http://notification-service:8084` (confirm port in that service’s K8s Service)
- `AUDIT_SERVICE_URL` → observability base URL (path `/audit/*` on the gateway must match what observability exposes, or add a dedicated route later)
