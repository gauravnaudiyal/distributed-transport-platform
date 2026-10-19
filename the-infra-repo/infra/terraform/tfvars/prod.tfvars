project     = "gtbs"
environment = "prod"

regions = {
  "region-a" = "spaincentral"
  "region-b" = "germanywestcentral"
}

aks_node_count   = 3
aks_node_vm_size = "Standard_D4s_v3"

postgres_sku   = "GP_Standard_D4s_v3"
postgres_backup_retention_days = 7
redis_capacity = 1
redis_family   = "C"
redis_sku      = "Standard"

ghcr_server   = "ghcr.io"
ghcr_username = "Tribunal-of-Human-Expansion"

tags = {
  project     = "gtbs"
  environment = "prod"
  terraform   = "true"
  cost-center = "platform"
}
