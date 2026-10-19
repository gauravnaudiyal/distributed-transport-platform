# ============================================================
# GTBS dev environment — single region, cost-optimised
# Fill in sensitive values via CI secrets or a local .auto.tfvars
# ============================================================

resource_group_name = "ds-gtbs"
location            = "spaincentral"   # Fallback if region_name is not present in the regions map
environment         = "dev"
region_name         = "region-a"

# AKS — 2 nodes, scale to 1 overnight to save credits
aks_node_count   = 2
aks_node_vm_size = "Standard_B2s"

# Postgres — cheapest Flexible Server tier; one DB per microservice (schema via migrations in each service repo)
postgres_sku           = "B_Standard_B1ms"
postgres_backup_retention_days = 0
postgres_storage_mb    = 32768
postgres_admin_username = "gtbsadmin"
# postgres_admin_password = ""   # Set via env: TF_VAR_postgres_admin_password
postgres_service_databases = [
  "gtbs_booking",
  "gtbs_journey_compat",
  "gtbs_route_management",
  "gtbs_traffic_authority",
  "gtbs_user_notification",
  "gtbs_audit",
]

# Redis — Basic C0 (non-HA, fine for dev)
redis_sku      = "Basic"
redis_capacity = 0
redis_family   = "C"

# Event Hubs (Kafka) — Standard 1 TU; add topic names here when a microservice needs a new topic
eventhubs_sku      = "Standard"
eventhubs_capacity = 1
kafka_topics = [
  "booking-confirmed",
  "booking-rejected",
  "booking-cancelled",
  "capacity-reservation",
  "route-policy-updated",
  "notification-dispatch",
  "audit-events",
]

# GHCR
ghcr_server   = "ghcr.io"
# ghcr_username = ""   # Set via env: TF_VAR_ghcr_username
# ghcr_pat      = ""   # Set via env: TF_VAR_ghcr_pat
