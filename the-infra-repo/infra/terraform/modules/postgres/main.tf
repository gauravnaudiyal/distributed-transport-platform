# ============================================================
# Module: postgres
# PostgreSQL Flexible Server — B_Standard_B1ms ≈ $13/mo
# Each GTBS service gets its own database within the server
# ============================================================

locals {
  server_name = "gtbs-${var.region_name}-${var.environment}-pg"
  tags = {
    project = "gtbs", region = var.region_name
    environment = var.environment, managed-by = "terraform"
  }
}

resource "azurerm_postgresql_flexible_server" "main" {
  name                          = local.server_name
  resource_group_name           = var.resource_group_name
  location                      = var.location
  version                       = "16"
  delegated_subnet_id           = var.subnet_id
  private_dns_zone_id           = var.private_dns_zone_id
  administrator_login           = var.admin_username
  administrator_password        = var.admin_password
  zone                          = "1"
  sku_name                      = var.sku_name
  storage_mb                    = var.storage_mb
  backup_retention_days         = var.backup_retention_days
  geo_redundant_backup_enabled  = false
  public_network_access_enabled = false
  tags                          = local.tags
}

# One database per service — shared server, isolated schemas. Schema/migrations live in each service repo.
resource "azurerm_postgresql_flexible_server_database" "services" {
  for_each  = toset(var.service_databases)
  name      = each.value
  server_id = azurerm_postgresql_flexible_server.main.id
  collation = "en_US.utf8"
  charset   = "utf8"
}
