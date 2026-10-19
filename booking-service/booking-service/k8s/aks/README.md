# AKS Deployment (Workload Identity + Key Vault)

This folder contains production-style Kubernetes manifests for deploying `booking-service` to an existing AKS cluster using:

- Azure Workload Identity
- Secrets Store CSI driver
- Azure Key Vault synced into a Kubernetes secret

## Files

- `serviceaccount.yml`: ServiceAccount mapped to Azure Managed Identity
- `secretproviderclass.yml`: Key Vault secret mapping for the pod
- `configmap.yml`: Non-secret runtime config
- `deployment.yml`: Deployment, Service, and Ingress
- `kustomization.yml`: Apply all resources together
- `set-keyvault-secrets.sh`: Helper to create/update required Key Vault secrets

## 1) Required values before deploy

Replace placeholders in these files:

1. `serviceaccount.yml`: `<MANAGED_IDENTITY_CLIENT_ID>`
2. `secretproviderclass.yml`: `<MANAGED_IDENTITY_CLIENT_ID>`, `<KEY_VAULT_NAME>`, `<TENANT_ID>`
3. `deployment.yml`: container image (`ghcr.io/tribunal-of-human-expansion/booking-service:latest`) if needed

Also ensure `ghcr-secret` exists in `default` namespace for private GHCR pulls.

## 2) Required Key Vault secrets

These secret names are expected by `secretproviderclass.yml`:

- `booking-datasource-url`
- `booking-datasource-username`
- `booking-datasource-password`
- `spring-data-redis-host`
- `spring-data-redis-port`
- `spring-data-redis-password`
- `spring-kafka-bootstrap-servers`

Create/update them using `set-keyvault-secrets.sh`.

## 3) Federated identity credential (Azure side)

If not already created for this service account, add a federated identity credential to the Managed Identity:

```bash
az identity federated-credential create \
  --name booking-service-fic \
  --identity-name <MANAGED_IDENTITY_NAME> \
  --resource-group <IDENTITY_RESOURCE_GROUP> \
  --issuer "$(az aks show -g <AKS_RESOURCE_GROUP> -n <AKS_CLUSTER_NAME> --query oidcIssuerProfile.issuerUrl -o tsv)" \
  --subject system:serviceaccount:default:booking-service \
  --audience api://AzureADTokenExchange
```

## 4) Deploy

```bash
kubectl apply -k k8s/aks
kubectl rollout status deployment/booking-service -n default
```

## 5) Verify

```bash
kubectl get pods -n default -l app=booking-service
kubectl logs -n default deploy/booking-service --tail=100
kubectl get secret booking-kv-synced -n default
kubectl describe pod -n default -l app=booking-service | grep -A3 -i 'Mounts\|Service Account'
```

## 6) Smoke test

If your ingress is already exposed:

```bash
curl -i -X POST http://<INGRESS_HOST>/booking/api/bookings \
  -H 'Content-Type: application/json' \
  -d '{
    "user_id":"aks-user-001",
    "origin":"Dublin-6",
    "destination":"Dublin-2",
    "idempotency_key":"aks-test-001",
    "time_window_start":"2026-04-05T08:00:00Z",
    "time_window_end":"2026-04-05T09:00:00Z"
  }'
```

## 7) If you get `AADSTS700213`

This means Azure could not find a federated identity credential that matches the pod's service account subject.

For this deployment, the subject must be exactly:

```text
system:serviceaccount:default:booking-service
```

Recreate or update the federated identity credential on the Managed Identity with:

```bash
az identity federated-credential create \
  --name booking-service-fic \
  --identity-name <MANAGED_IDENTITY_NAME> \
  --resource-group <IDENTITY_RESOURCE_GROUP> \
  --issuer "$(az aks show -g <AKS_RESOURCE_GROUP> -n <AKS_CLUSTER_NAME> --query oidcIssuerProfile.issuerUrl -o tsv)" \
  --subject system:serviceaccount:default:booking-service \
  --audience api://AzureADTokenExchange
```

If the error persists, verify all three values match exactly:

- `issuer` from the AKS cluster OIDC issuer URL
- `subject` from `namespace + service account name`
- `audience` must be `api://AzureADTokenExchange`

## 8) If you get `remaining connection slots are reserved`

This indicates Azure PostgreSQL has reached its connection limit.

This deployment now applies conservative defaults for AKS:

- `replicas: 1` (in `deployment.yml`)
- Hikari pool defaults in `application-azure.yml` / `configmap.yml`:
  - `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3`
  - `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=1`
  - startup schema init disabled (`spring.sql.init.mode=never`)

If needed, tune pool size without rebuilding:

```bash
kubectl patch configmap booking-service-config -n default --type merge -p '{"data":{"SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE":"2","SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE":"1"}}'
kubectl rollout restart deployment/booking-service -n default
```

