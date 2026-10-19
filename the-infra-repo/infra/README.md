# GTBS Infrastructure Repo

Terraform, operational scripts, and container templates for the **Global Traffic Booking Service**.

## Repo structure

```
.
├── README.md                   # Repo-level onboarding guide
├── docs/
│   └── architecture.md         # GTBS system architecture
├── dockerfiles/
│   ├── Dockerfile.java         # Template for Java / Spring Boot services
│   ├── Dockerfile.go           # Template for the Go API Gateway
│   └── Dockerfile.frontend     # Template for the React TypeScript client
└── infra/
    ├── README.md               # This file
    ├── scripts/
    │   ├── start-aks.sh        # Start AKS and refresh kubectl credentials
    │   └── stop-aks.sh         # Stop AKS to save Azure credits
    └── terraform/
        ├── main.tf             # Root module — wires everything together
        ├── variables.tf        # Input variables with docs + defaults
        ├── outputs.tf          # Useful post-deploy outputs
        ├── backend-configs/    # State key per env/region (init -backend-config=...)
        ├── tfvars/             # Checked-in dev/prod variable files
        └── modules/
            ├── networking/     # VNet, subnets, private DNS
            ├── aks/            # AKS cluster
            ├── postgres/       # PostgreSQL Flexible Server + per-service DBs
            ├── redis/          # Azure Cache for Redis
            ├── kafka/          # Event Hubs (Kafka-compatible)
            └── region/         # Shared region-level naming / helpers
```

## Cost estimate (dev, 1 region)

| Resource | SKU | Est. $/mo |
|---|---|---|
| AKS (2× Standard_B2s) | B2s | ~$60 |
| PostgreSQL Flexible Server | B_Standard_B1ms | ~$13 |
| Azure Cache for Redis | Basic C0 | ~$16 |
| Event Hubs (Kafka) | Standard 1 TU | ~$10 |
| VNet / DNS | Free tier | $0 |
| **Total** | | **~$99–130** |

> **Save credits**: run `az aks stop --name <cluster> --resource-group ds-gtbs` when not using it.

## Prerequisites

- Azure CLI logged in: `az login`
- Terraform >= 1.7.0
- GitHub CLI authenticated: `gh auth login`
- Access to the `ds-gtbs` resource group

## First-time setup

```bash
cd infra/terraform

# 1. Init with backend config for this env + region (state key = dev/region-a.tfstate)
terraform init -backend-config=backend-configs/dev.region-a.hcl

# 2. Set secrets (never commit these)

# If GitHub CLI is not installed yet on macOS:
# brew install gh

# login
gh auth login

# Export secrets
export TF_VAR_postgres_admin_password=$(az account get-access-token --resource https://ossrdbms-aad.database.windows.net --query accessToken --output tsv)
export TF_VAR_ghcr_username="<github-username>"
export TF_VAR_ghcr_pat="<generate-personal-access-token>"

# 3. Plan
terraform plan -var-file=tfvars/dev.tfvars

# 4. Apply
terraform apply -var-file=tfvars/dev.tfvars

# 5. Configure kubectl (or use `terraform output aks_kube_config_command`)
az aks get-credentials --resource-group ds-gtbs --name <aks-cluster-name>
```

## Deploying to different envs and regions

State is split by **environment** and **region** via backend config. Each combo uses its own state file in the same storage account (e.g. `dev/region-a.tfstate`, `prod/region-b.tfstate`).

| Backend config           | State key               | Use with var-file / env              |
|--------------------------|-------------------------|--------------------------------------|
| `backend-configs/dev.region-a.hcl`  | `dev/region-a.tfstate`  | `tfvars/dev.tfvars` |
| `backend-configs/dev.region-b.hcl`  | `dev/region-b.tfstate`  | `tfvars/dev.tfvars` or a region-specific override |
| `backend-configs/prod.region-a.hcl` | `prod/region-a.tfstate` | `tfvars/prod.tfvars` |
| `backend-configs/prod.region-b.hcl` | `prod/region-b.tfstate` | `tfvars/prod.tfvars` or a region-specific override |

**Workflow:**

1. **Init** for the desired env + region (required every time you switch):
   ```bash
   terraform init -backend-config=backend-configs/<env>.<region>.hcl
   ```
2. **Plan / Apply** with the matching var-file:
   ```bash
   terraform plan -var-file=tfvars/dev.tfvars
   terraform apply -var-file=tfvars/dev.tfvars
   ```
   For prod, use `tfvars/prod.tfvars`. If you need values that differ by region, create an additional tfvars file and pass that instead.

**Switching env/region:** run `terraform init -backend-config=backend-configs/<other-env>.<other-region>.hcl` again so Terraform uses the correct state file. No need to change `main.tf`.

## Using Dockerfiles in service repos

1. Copy the appropriate Dockerfile from `dockerfiles/` into the service repo root.
2. Add or update the service repo CI/CD workflow to build and publish that image.
3. Make sure the workflow points at the correct service name, image name, and deployment target.
4. Add these secrets to the service repo (or org-level):
   - `AZURE_CREDENTIALS` — service principal JSON from `az ad sp create-for-rbac`
   - `AKS_CLUSTER_NAME` — output from Terraform
   - `POSTGRES_ADMIN_PASSWORD`
   - `GHCR_USERNAME` / `GHCR_PAT`

## Services and their Dockerfiles

| Service | Language | Dockerfile |
|---|---|---|
| api-gateway | Go | `Dockerfile.go` |
| booking-service | Java/Spring Boot | `Dockerfile.java` |
| journey-compatibility-service | Java/Spring Boot | `Dockerfile.java` |
| route-management-service | Java/Spring Boot | `Dockerfile.java` |
| traffic-authority-service | Java/Spring Boot | `Dockerfile.java` |
| user-notification-service | Java/Spring Boot | `Dockerfile.java` |
| audit-service | Java/Spring Boot | `Dockerfile.java` |
| client | React/TypeScript | `Dockerfile.frontend` |

## Adding a new env or region

1. **Backend:** add `backend-configs/<env>.<region>.hcl` with `key = "<env>/<region>.tfstate"`.
2. **Vars:** add or duplicate a tfvars file under `tfvars/` if that env or region needs different values.
3. **Init and apply:**  
   `terraform init -backend-config=backend-configs/<env>.<region>.hcl`  
   then `terraform plan -var-file=...` / `terraform apply -var-file=...`.

## Microservice ownership: schema, Kafka, Redis

### How microservices define their **database schema**

Infra creates the **Postgres server** and **one database per service**. The database names are controlled by `postgres_service_databases` in Terraform inputs. The **schema** (tables, indexes, migrations) is owned by each microservice repo:

- **Java/Spring Boot**: Put migrations in the service repo (e.g. `src/main/resources/db/migration/`) and use **Flyway** or **Liquibase**. The app runs migrations on startup or as an init container; it connects to its DB using the same name as in `postgres_service_databases` (e.g. `gtbs_booking`).
- **Go**: Use **golang-migrate** or similar; migration files live in the service repo and run at deploy/startup.
- **Convention**: Database name in infra (e.g. `gtbs_booking`) must match what the service config uses. Add new DBs by adding the name to `postgres_service_databases` in this repo, then the service owns all schema inside that DB.

So: **infra = create empty DB; microservice repo = define and evolve schema via migrations.**

### Kafka (Event Hubs): same “list in infra” pattern

Kafka **topics** are created in this repo. The list is **variable-driven** via `kafka_topics` in Terraform inputs. Adding a new topic (e.g. for a new event type) means adding it there and applying Terraform, the same way you add a new Postgres DB name. Consumer groups are created per topic; services use the shared connection from the `kafka-credentials` secret and publish/consume to the topic names defined here. So yes: **new topics are created in this repo**, not in the microservice repo.

### Redis: no per-service “resources” in infra

Infra provisions **one Redis instance** (shared by all services). Redis does not have a list of “databases” to create like Postgres or topics like Kafka; you use the same instance and separate data by **key naming** (e.g. `booking:session:123`, `route:cache:xyz`) or by **logical DB index** (Redis 0–15). So there is **no similar issue**: no list to maintain in this repo; each microservice just uses the shared Redis with its own key prefix or DB number. If you later need isolation (e.g. separate caches per service), you’d add more Redis resources or variables here — but for typical caching, the single shared instance is enough.
