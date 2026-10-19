# GTBS Infrastructure Repo

This repository contains the infrastructure code and deployment templates for the **Global Traffic Booking Service (GTBS)**. It is the shared operational base for the wider GTBS system: Terraform provisions the Azure platform, Kubernetes secrets and namespace, Dockerfile templates support service repos, and the docs explain how the distributed system is meant to fit together.

If you are new to the project, start here for the repo overview and first-time setup, then use [`infra/README.md`](infra/README.md) for the infra-specific details.

## What this repo is for

GTBS is a federated distributed system for pre-booking journeys while enforcing regional road-capacity constraints. This repo is responsible for the shared infrastructure needed to run that system in Azure.

It currently manages:

- Azure networking and supporting private DNS
- AKS for Kubernetes workloads
- PostgreSQL Flexible Server plus one logical database per service
- Redis for shared caching
- Kafka-compatible Event Hubs topics for asynchronous workflows
- Kubernetes namespace and connection secrets used by GTBS services
- Reusable Dockerfile templates for backend and frontend repos

This repo does **not** contain the business logic for the GTBS microservices themselves. Those live in their own service repositories and consume the infrastructure created here.

## Repo layout

```text
.
├── README.md                   # Top-level onboarding guide
├── docs/
│   └── architecture.md         # System architecture and consistency model
├── dockerfiles/
│   ├── Dockerfile.java         # Template for Java / Spring Boot services
│   ├── Dockerfile.go           # Template for the Go API gateway
│   └── Dockerfile.frontend     # Template for the React client
└── infra/
    ├── README.md               # Infra-specific setup and deployment notes
    ├── scripts/
    │   ├── start-aks.sh        # Resume the AKS cluster and fetch credentials
    │   └── stop-aks.sh         # Stop AKS to save Azure credits
    └── terraform/
        ├── main.tf             # Root module wiring
        ├── variables.tf        # Terraform inputs
        ├── outputs.tf          # Useful outputs after apply
        ├── backend-configs/    # Remote state config per env / region
        ├── tfvars/             # Environment-specific variable files
        └── modules/            # Reusable Terraform modules
```

## How the system is organized

The wider GTBS platform is designed as a set of region-aware microservices. Each region runs its own deployment and owns the road segments it authoritatively controls. This repo provisions the shared runtime that those services depend on.

At a high level:

- each service gets its own Postgres database on the shared server
- services share one Redis instance and separate data by keys or logical DB usage
- Kafka topics are declared centrally in Terraform
- Kubernetes namespace and secrets are created here so service repos can deploy into a known environment

For the full architecture rationale, read [`docs/architecture.md`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/docs/architecture.md).

## First-time setup

### 1. Prerequisites

You need:

- Azure CLI installed and authenticated with `az login`
- Terraform `>= 1.7.0`
- GitHub CLI installed and authenticated with `gh auth login`
- Access to the Azure resource group `ds-gtbs`
- Permission to read the required GitHub secrets from the relevant repo

On macOS, you can install GitHub CLI with:

```bash
brew install gh
```

### 2. Initialize Terraform

From the repo root:

```bash
cd infra/terraform
terraform init -backend-config=backend-configs/dev.region-a.hcl
```

That selects the remote Terraform state for the chosen environment and region. Re-run `terraform init` with a different backend config whenever you switch env or region.

### 3. Export required secrets

These values are required by Terraform and should never be committed.

```bash
export TF_VAR_postgres_admin_password=$(gh secret view POSTGRES_ADMIN_PASSWORD --repo Tribunal-of-Human-Expansion/<your-repo> --raw)
export TF_VAR_ghcr_username=$(gh secret view GHCR_USERNAME --repo Tribunal-of-Human-Expansion/<your-repo> --raw)
export TF_VAR_ghcr_pat=$(gh secret view GHCR_PAT --repo Tribunal-of-Human-Expansion/<your-repo> --raw)
```

#TODO : If secrets stored in a different GitHub repo or at org level, use the correct source instead.

### 4. Plan and apply

This repo currently uses tfvars files under `infra/terraform/tfvars/`.

```bash
terraform plan -var-file=tfvars/dev.tfvars
terraform apply -var-file=tfvars/dev.tfvars
```

For production, use:

```bash
terraform plan -var-file=tfvars/prod.tfvars
terraform apply -var-file=tfvars/prod.tfvars
```

### 5. Configure kubectl

After apply completes, either run the command shown in Terraform output or use:

```bash
az aks get-credentials --resource-group ds-gtbs --name <aks-cluster-name>
```

Terraform also exposes a ready-made output:

```bash
terraform output aks_kube_config_command
```

## Common day-to-day tasks

### Switch environment or region

Use the matching backend config, then the matching tfvars file.

Examples:

```bash
terraform init -backend-config=backend-configs/dev.region-b.hcl
terraform plan -var-file=tfvars/dev.tfvars
```

```bash
terraform init -backend-config=backend-configs/prod.region-a.hcl
terraform plan -var-file=tfvars/prod.tfvars
```

The backend config controls which remote state file Terraform uses. The tfvars file controls deployment values such as environment, location, region label, and service settings.

### Stop and restart AKS to save credits

From [`infra/`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/infra):

```bash
./scripts/stop-aks.sh
./scripts/start-aks.sh
```

These scripts use Terraform output to discover the cluster name automatically.

### Inspect useful outputs

After apply, these outputs are especially useful:

- `aks_cluster_name`
- `aks_kube_config_command`
- `postgres_fqdn`
- `redis_hostname`
- `kafka_bootstrap_servers`
- `k8s_namespace`

Run:

```bash
terraform output
```

## How service repos use this repo

Service repos depend on this repo for infrastructure, but they still own their application-level behavior.

- Postgres: this repo creates the server and databases; each service repo owns its schema and migrations
- Kafka: topic names are declared here; adding a new topic means updating Terraform here
- Redis: one shared instance is provisioned here; services isolate usage through key naming or DB numbering
- Container builds: service repos can reuse templates from [`dockerfiles/`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/dockerfiles)

Typical flow for a new service:

1. Copy the relevant Dockerfile template from `dockerfiles/`.
2. Add its database name or Kafka topic in Terraform if needed.
3. Apply infra changes.
4. Deploy the service into the Kubernetes namespace created by this repo.

## Where to read next

- [`infra/README.md`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/infra/README.md): infra-specific walkthrough and conventions
- [`docs/architecture.md`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/docs/architecture.md): distributed system architecture and service boundaries
- [`infra/terraform/backend-configs/README.md`](/Users/mogboella/School/DISTRIBUTED_SYS/gtbs/the-infra-repo/infra/terraform/backend-configs/README.md): backend state layout details
