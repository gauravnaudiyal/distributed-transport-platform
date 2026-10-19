# Backend Configs

This folder contains the backend config files used by Terraform remote state.

The root backend block in [`main.tf`](../main.tf) defines the Azure Storage account, container, and resource group, but it intentionally does **not** hardcode the `key`. The `key` comes from one of the files in this directory so each environment and region gets its own separate state file.

## Current backend configs

| File | State key in blob storage |
|------|---------------------------|
| `dev.region-a.hcl` | `dev/region-a.tfstate` |
| `dev.region-b.hcl` | `dev/region-b.tfstate` |
| `prod.region-a.hcl` | `prod/region-a.tfstate` |
| `prod.region-b.hcl` | `prod/region-b.tfstate` |

## How to use

Run `terraform init` from [`infra/terraform/`](../) with the backend config for the environment and region you want:

```bash
terraform init -backend-config=backend-configs/dev.region-a.hcl
```

Then plan or apply with the matching tfvars file, for example:

```bash
terraform plan -var-file=tfvars/dev.tfvars
terraform apply -var-file=tfvars/dev.tfvars
```

For production:

```bash
terraform init -backend-config=backend-configs/prod.region-a.hcl
terraform plan -var-file=tfvars/prod.tfvars
```

## Switching env or region

Whenever you switch to a different environment or region, run `terraform init` again with the matching backend config. That changes which remote state file Terraform reads and writes.

Examples:

```bash
terraform init -backend-config=backend-configs/dev.region-b.hcl
```

```bash
terraform init -backend-config=backend-configs/prod.region-b.hcl
```

## Adding a new backend config

To add another env/region combination, create a new file named `<env>.<region>.hcl` with:

```hcl
key = "<env>/<region>.tfstate"
```

Example:

```hcl
key = "staging/region-a.tfstate"
```
