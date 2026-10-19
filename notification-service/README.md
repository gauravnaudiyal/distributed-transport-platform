# notification-service

## AKS deployment assets

Kubernetes manifests were added under `k8s/`:

- `k8s/service-account.yml`
- `k8s/configmap.yml`
- `k8s/secretproviderclass.yml`
- `k8s/deployment.yml`
- `k8s/kustomization.yml`

The production app reads these env vars from the pod:

- `notification_db_url`
- `notification_db_username`
- `notification_db_password`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`

## 1) Add DB secrets to Azure Key Vault

Use your Key Vault `ds-gtbs-kv-prod` and set the DB values:

```zsh
az login
az account set --subscription "<YOUR_SUBSCRIPTION_ID_OR_NAME>"

az keyvault secret set --vault-name ds-gtbs-kv-prod --name notification-db-url --value "jdbc:postgresql://<host>:5432/<db>?sslmode=require"
az keyvault secret set --vault-name ds-gtbs-kv-prod --name notification-db-username --value "<db-username>"
az keyvault secret set --vault-name ds-gtbs-kv-prod --name notification-db-password --value "<db-password>"
```

Kafka is already present in vault. This setup expects key name `notification-kafka-bootstrap-servers`.

## 2) Publish container image to GHCR

A GitHub Actions workflow was added at `.github/workflows/docker-publish.yml`.

- Trigger manually from Actions tab (`workflow_dispatch`) or push to `main`.
- Image is pushed to `ghcr.io/<owner>/notification-service`.

## 3) Deploy to AKS

Before deploy, set your final image tag in `k8s/kustomization.yml` (`images[].newName` and `newTag`) or override inline.

```zsh
az aks get-credentials --resource-group "<AKS_RESOURCE_GROUP>" --name "<AKS_CLUSTER_NAME>"

kubectl apply -k k8s
kubectl rollout status deployment/notification-service -n default
kubectl get pods -n default
```

To override image without editing files:

```zsh
cd k8s
kustomize edit set image ghcr.io/your-org/notification-service=ghcr.io/<owner>/notification-service:<tag>
cd ..
kubectl apply -k k8s
```

## 4) Verify secrets and workload identity

```zsh
kubectl describe sa booking-service -n default
kubectl get secretproviderclass booking-kv-secrets -n default -o yaml
kubectl get secret notification-service-secrets -n default -o yaml
kubectl logs deploy/notification-service -n default --tail=100
```
