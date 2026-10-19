# ─────────────────────────────────────────────
# modules/region/main.tf
# Deploys a complete GTBS regional stack:
#   VNet → AKS → PostgreSQL → Redis → Event Hubs
# ─────────────────────────────────────────────

# Resource group per region
resource "azurerm_resource_group" "region" {
  name     = "${var.project}-${var.region_key}-${var.environment}-rg"
  location = var.azure_location
  tags     = var.tags
}

# ─── Networking ───────────────────────────────
resource "azurerm_virtual_network" "main" {
  name                = "${var.project}-${var.region_key}-vnet"
  resource_group_name = azurerm_resource_group.region.name
  location            = azurerm_resource_group.region.location
  address_space       = [var.vnet_cidr]
  tags                = var.tags
}

resource "azurerm_subnet" "aks" {
  name                 = "aks-subnet"
  resource_group_name  = azurerm_resource_group.region.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = [var.aks_subnet_cidr]
}

resource "azurerm_subnet" "data" {
  name                 = "data-subnet"
  resource_group_name  = azurerm_resource_group.region.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = [var.data_subnet_cidr]
  service_endpoints    = ["Microsoft.Storage"]

  delegation {
    name = "postgres-delegation"
    service_delegation {
      name = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = [
        "Microsoft.Network/virtualNetworks/subnets/join/action",
      ]
    }
  }
}

# ─── AKS ──────────────────────────────────────
resource "azurerm_kubernetes_cluster" "main" {
  name                = "${var.project}-${var.region_key}-${var.environment}-aks"
  resource_group_name = azurerm_resource_group.region.name
  location            = azurerm_resource_group.region.location
  dns_prefix          = "${var.project}-${var.region_key}-${var.environment}"
  kubernetes_version  = var.kubernetes_version
  tags                = var.tags

  default_node_pool {
    name                = "system"
    node_count          = var.aks_node_count
    vm_size             = var.aks_node_vm_size
    vnet_subnet_id      = azurerm_subnet.aks.id
    os_disk_size_gb     = 128
    type                = "VirtualMachineScaleSets"
    enable_auto_scaling = true
    min_count           = var.aks_node_count
    max_count           = var.aks_node_count * 3

    upgrade_settings {
      max_surge = "10%"
    }
  }

  # Workload node pool for GTBS services
  # (system pool kept clean for kube-system)
  # Add extra node pools via azurerm_kubernetes_cluster_node_pool if needed.

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    network_policy    = "azure"
    load_balancer_sku = "standard"
    outbound_type     = "loadBalancer"
  }

  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  }

  azure_active_directory_role_based_access_control {
    managed                = true
    azure_rbac_enabled     = true
  }
}

# Workload node pool (separate from system)
resource "azurerm_kubernetes_cluster_node_pool" "workload" {
  name                  = "workload"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.main.id
  vm_size               = var.aks_node_vm_size
  node_count            = var.aks_node_count
  vnet_subnet_id        = azurerm_subnet.aks.id
  enable_auto_scaling   = true
  min_count             = var.aks_node_count
  max_count             = var.aks_node_count * 4
  os_disk_size_gb       = 128
  tags                  = var.tags

  node_labels = {
    "gtbs/role" = "workload"
  }
}

# Allow AKS to pull from GHCR via a Kubernetes image pull secret
# (secret is created in the k8s namespace by the Helm chart;
#  values are passed through from variables)
resource "kubernetes_secret" "ghcr_pull_secret" {
  metadata {
    name      = "ghcr-pull-secret"
    namespace = "gtbs"
  }

  type = "kubernetes.io/dockerconfigjson"

  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        "${var.ghcr_server}" = {
          username = var.ghcr_username
          password = var.ghcr_token
          auth     = base64encode("${var.ghcr_username}:${var.ghcr_token}")
        }
      }
    })
  }

  depends_on = [azurerm_kubernetes_cluster.main]
}

# ─── Log Analytics (for AKS OMS) ──────────────
resource "azurerm_log_analytics_workspace" "main" {
  name                = "${var.project}-${var.region_key}-${var.environment}-law"
  resource_group_name = azurerm_resource_group.region.name
  location            = azurerm_resource_group.region.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = var.tags
}

# ─── PostgreSQL Flexible Server ───────────────
resource "azurerm_private_dns_zone" "postgres" {
  name                = "${var.project}-${var.region_key}.postgres.database.azure.com"
  resource_group_name = azurerm_resource_group.region.name
  tags                = var.tags
}

resource "azurerm_private_dns_zone_virtual_network_link" "postgres" {
  name                  = "postgres-vnet-link"
  resource_group_name   = azurerm_resource_group.region.name
  private_dns_zone_name = azurerm_private_dns_zone.postgres.name
  virtual_network_id    = azurerm_virtual_network.main.id
  tags                  = var.tags
}

resource "azurerm_postgresql_flexible_server" "main" {
  name                   = "${var.project}-${var.region_key}-${var.environment}-pg"
  resource_group_name    = azurerm_resource_group.region.name
  location               = azurerm_resource_group.region.location
  version                = "16"
  delegated_subnet_id    = azurerm_subnet.data.id
  private_dns_zone_id    = azurerm_private_dns_zone.postgres.id
  administrator_login    = "gtbsadmin"
  administrator_password = var.postgres_admin_password
  zone                   = "1"
  sku_name               = var.postgres_sku
  storage_mb             = 65536
  backup_retention_days  = var.postgres_backup_retention_days
  tags                   = var.tags

  high_availability {
    mode                      = "ZoneRedundant"
    standby_availability_zone = "2"
  }

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]
}

# One database per GTBS service
locals {
  pg_databases = [
    "booking",
    "journey_compatibility",
    "route_management",
    "traffic_authority",
    "user_notification",
    "audit",
  ]
}

resource "azurerm_postgresql_flexible_server_database" "services" {
  for_each  = toset(local.pg_databases)
  name      = each.key
  server_id = azurerm_postgresql_flexible_server.main.id
  collation = "en_US.utf8"
  charset   = "utf8"
}

# ─── Redis Cache ──────────────────────────────
resource "azurerm_redis_cache" "main" {
  name                = "${var.project}-${var.region_key}-${var.environment}-redis"
  resource_group_name = azurerm_resource_group.region.name
  location            = azurerm_resource_group.region.location
  capacity            = var.redis_capacity
  family              = var.redis_family
  sku_name            = var.redis_sku
  enable_non_ssl_port = false
  minimum_tls_version = "1.2"

  redis_configuration {
    maxmemory_policy = "allkeys-lru"
  }

  tags = var.tags
}

# ─── Event Hubs (Kafka-compatible) ────────────
resource "azurerm_eventhub_namespace" "main" {
  name                = "${var.project}-${var.region_key}-${var.environment}-ehn"
  resource_group_name = azurerm_resource_group.region.name
  location            = azurerm_resource_group.region.location
  sku                 = "Standard"
  capacity            = 2
  kafka_enabled       = true  # exposes Kafka endpoint
  tags                = var.tags

  network_rulesets {
    default_action = "Deny"
    virtual_network_rule {
      subnet_id = azurerm_subnet.aks.id
    }
  }
}

# One Event Hub (topic) per async workflow
locals {
  kafka_topics = [
    "booking-lifecycle",
    "booking-cancelled",
    "route-policy-updated",
    "notification-outbox",
    "audit-events",
  ]
}

resource "azurerm_eventhub" "topics" {
  for_each            = toset(local.kafka_topics)
  name                = each.key
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = azurerm_resource_group.region.name
  partition_count     = 4
  message_retention   = 7
}

resource "azurerm_eventhub_namespace_authorization_rule" "gtbs_rw" {
  name                = "gtbs-rw"
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = azurerm_resource_group.region.name
  listen              = true
  send                = true
  manage              = false
}
