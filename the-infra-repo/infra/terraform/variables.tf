# ============================================================
# GTBS — Terraform Variables
# ============================================================

variable "project" {
  description = "Project name used as a label/prefix across all resources"
  type        = string
  default     = "gtbs"
}

variable "regions" {
  description = "Map of GTBS logical region labels to Azure region names"
  type        = map(string)
  default = {
    "region-a" = "spaincentral"
    "region-b" = "germanywestcentral"
  }
}

variable "tags" {
  description = "Common tags applied to all Azure resources"
  type        = map(string)
  default     = {}
}

variable "resource_group_name" {
  description = "Existing Azure resource group"
  type        = string
  default     = "ds-gtbs"
}

variable "location" {
  description = "Azure region for this deployment"
  type        = string
  default     = "spaincentral"
}

variable "environment" {
  description = "Deployment environment (dev | staging | prod)"
  type        = string
  default     = "dev"
}

variable "region_name" {
  description = "Logical GTBS region label (e.g. region-a, region-b)"
  type        = string
  default     = "region-a"
}

# ── AKS ──────────────────────────────────────────────────────
variable "aks_node_count" {
  description = "Number of nodes in the default node pool"
  type        = number
  default     = 2
}

variable "aks_node_vm_size" {
  description = "VM size for AKS nodes. Standard_B2s is the cheapest that runs K8s reliably."
  type        = string
  default     = "Standard_B2s"
}

variable "aks_kubernetes_version" {
  type    = string
  default = "1.29"
}

# ── PostgreSQL ───────────────────────────────────────────────
variable "postgres_sku" {
  description = "Flexible Server SKU. B_Standard_B1ms is the cheapest tier (~$13/mo)."
  type        = string
  default     = "B_Standard_B1ms"
}

variable "postgres_backup_retention_days" {
  description = "Number of days to retain PostgreSQL backups. Set to 0 to disable (dev); 7–35 for enabled (prod)."
  type        = number
  default     = 7
}

variable "postgres_storage_mb" {
  type    = number
  default = 32768
}

variable "postgres_admin_username" {
  type    = string
  default = "gtbsadmin"
}

variable "postgres_admin_password" {
  type      = string
  sensitive = true
}

variable "postgres_service_databases" {
  description = "Database names to create on the shared Postgres server (one per microservice). Schema is owned by each service repo via migrations."
  type        = list(string)
  default = [
    "gtbs_booking",
    "gtbs_journey_compat",
    "gtbs_route_management",
    "gtbs_traffic_authority",
    "gtbs_user_notification",
    "gtbs_audit",
  ]
}

# ── Redis ────────────────────────────────────────────────────
variable "redis_sku" {
  type    = string
  default = "Basic"
}

variable "redis_capacity" {
  type    = number
  default = 0
}

variable "redis_family" {
  type    = string
  default = "C"
}

# ── Kafka (Event Hubs) ───────────────────────────────────────
variable "eventhubs_sku" {
  type    = string
  default = "Standard"
}

variable "eventhubs_capacity" {
  type    = number
  default = 1
}

variable "kafka_topics" {
  description = "Event Hub (Kafka) topic names. New topics are added here when needed by a microservice."
  type        = list(string)
  default = [
    "booking-confirmed",
    "booking-rejected",
    "booking-cancelled",
    "capacity-reservation",
    "route-policy-updated",
    "notification-dispatch",
    "audit-events",
  ]
}

# ── GHCR ─────────────────────────────────────────────────────
variable "ghcr_server" {
  type    = string
  default = "ghcr.io"
}

variable "ghcr_username" {
  type      = string
  sensitive = true
}

variable "ghcr_pat" {
  description = "GitHub PAT with read:packages scope"
  type        = string
  sensitive   = true
}
