project     = "gtbs"
environment = "dev"

regions = {
  "region-a" = "spaincentral"
  "region-b" = "germanywestcentral"
}

aks_node_count   = 2
aks_node_vm_size = "Standard_D2s_v3"

postgres_sku                   = "B_Standard_B2ms"
postgres_backup_retention_days = 7
redis_capacity                 = 1
redis_family                   = "C"
redis_sku                      = "Basic"

ghcr_server   = "ghcr.io"
ghcr_username = "Tribunal-of-Human-Expansion"

tags = {
  project     = "gtbs"
  environment = "dev"
  terraform   = "true"
}
