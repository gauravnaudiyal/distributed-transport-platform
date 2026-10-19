terraform {
  required_version = ">= 1.7.0"
  required_providers {
    azurerm    = { source = "hashicorp/azurerm", version = "~> 3.100" }
    kubernetes = { source = "hashicorp/kubernetes", version = "~> 2.29" }
    helm       = { source = "hashicorp/helm", version = "~> 2.13" }
  }
  backend "azurerm" {
    resource_group_name  = "ds-gtbs"
    storage_account_name = "gtbstfstate"
    container_name       = "tfstate"
  }
}

provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = true
    }
  }
  skip_provider_registration = true
}

locals {
  # Resolve the active Azure region: prefer an explicit entry in the
  # regions map, then fall back to the standalone location variable.
  location = lookup(var.regions, var.region_name, var.location)

  # Merge caller-supplied tags with baseline tags so every resource
  # carries project/environment/region provenance.
  effective_tags = merge(
    {
      project     = var.project
      environment = var.environment
      region      = var.region_name
      terraform   = "true"
    },
    var.tags,
  )
}

data "azurerm_resource_group" "main" {
  name = var.resource_group_name
}

module "networking" {
  source              = "./modules/networking"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = local.location
  environment         = var.environment
  region_name         = var.region_name
}

module "aks" {
  source              = "./modules/aks"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = local.location
  environment         = var.environment
  region_name         = var.region_name
  subnet_id           = module.networking.aks_subnet_id
  node_count          = var.aks_node_count
  node_vm_size        = var.aks_node_vm_size
  kubernetes_version  = var.aks_kubernetes_version
}

module "postgres" {
  source                = "./modules/postgres"
  resource_group_name   = data.azurerm_resource_group.main.name
  location              = local.location
  environment           = var.environment
  region_name           = var.region_name
  subnet_id             = module.networking.postgres_subnet_id
  private_dns_zone_id   = module.networking.postgres_dns_zone_id
  sku_name              = var.postgres_sku
  storage_mb            = var.postgres_storage_mb
  admin_username        = var.postgres_admin_username
  admin_password        = var.postgres_admin_password
  service_databases     = var.postgres_service_databases
  backup_retention_days = var.postgres_backup_retention_days
}

module "redis" {
  source              = "./modules/redis"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = local.location
  environment         = var.environment
  region_name         = var.region_name
  sku_name            = var.redis_sku
  capacity            = var.redis_capacity
  family              = var.redis_family
  subnet_id           = module.networking.redis_pe_subnet_id
  private_dns_zone_id = module.networking.redis_dns_zone_id
}

module "kafka" {
  source              = "./modules/kafka"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = local.location
  environment         = var.environment
  region_name         = var.region_name
  sku                 = var.eventhubs_sku
  capacity            = var.eventhubs_capacity
  topics              = var.kafka_topics
}

# ── Kubernetes providers (post-AKS) ──────────────────────────
provider "kubernetes" {
  host                   = module.aks.kube_config.host
  client_certificate     = base64decode(module.aks.kube_config.client_certificate)
  client_key             = base64decode(module.aks.kube_config.client_key)
  cluster_ca_certificate = base64decode(module.aks.kube_config.cluster_ca_certificate)
}

resource "kubernetes_namespace" "gtbs" {
  metadata {
    name   = "gtbs-${var.environment}"
    labels = local.effective_tags
  }
}

# ── Image pull secret for GHCR ────────────────────────────────
resource "kubernetes_secret" "ghcr_pull_secret" {
  metadata {
    name      = "ghcr-pull-secret"
    namespace = kubernetes_namespace.gtbs.metadata[0].name
  }
  type = "kubernetes.io/dockerconfigjson"
  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        "${var.ghcr_server}" = {
          username = var.ghcr_username
          password = var.ghcr_pat
          auth     = base64encode("${var.ghcr_username}:${var.ghcr_pat}")
        }
      }
    })
  }
}

# ── Connection secrets for services ──────────────────────────
resource "kubernetes_secret" "postgres_credentials" {
  metadata {
    name      = "postgres-credentials"
    namespace = kubernetes_namespace.gtbs.metadata[0].name
  }
  type = "Opaque"
  data = {
    host     = module.postgres.fqdn
    port     = "5432"
    username = var.postgres_admin_username
    password = var.postgres_admin_password
  }
}

resource "kubernetes_secret" "redis_credentials" {
  metadata {
    name      = "redis-credentials"
    namespace = kubernetes_namespace.gtbs.metadata[0].name
  }
  type = "Opaque"
  data = {
    host        = module.redis.hostname
    port        = "6380"
    primary_key = module.redis.primary_access_key
  }
}

resource "kubernetes_secret" "kafka_credentials" {
  metadata {
    name      = "kafka-credentials"
    namespace = kubernetes_namespace.gtbs.metadata[0].name
  }
  type = "Opaque"
  data = {
    bootstrap_servers = module.kafka.bootstrap_servers
    connection_string = module.kafka.primary_connection_string
  }
}
