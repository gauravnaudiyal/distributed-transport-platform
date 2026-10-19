# ============================================================
# Module: redis
# Azure Cache for Redis — Basic C0 ≈ $16/mo
# Not HA (Basic tier) — acceptable for dev/demo
# Upgrade to Standard C1 for prod HA (~$50/mo)
# ============================================================

locals {
  cache_name = "gtbs-${var.region_name}-${var.environment}-redis"
  tags = {
    project     = "gtbs", region = var.region_name
    environment = var.environment, managed-by = "terraform"
  }
}

resource "azurerm_redis_cache" "main" {
  name                          = local.cache_name
  location                      = var.location
  resource_group_name           = var.resource_group_name
  capacity                      = var.capacity
  family                        = var.family
  sku_name                      = var.sku_name
  non_ssl_port_enabled          = false
  minimum_tls_version           = "1.2"
  public_network_access_enabled = false

  redis_configuration {
    # Evict least-recently-used keys when memory is full
    maxmemory_policy = "allkeys-lru"
  }

  tags = local.tags
}

resource "azurerm_private_endpoint" "redis" {
  name                = "${local.cache_name}-pe"
  location            = var.location
  resource_group_name = var.resource_group_name
  subnet_id           = var.subnet_id

  private_service_connection {
    name                           = "${local.cache_name}-psc"
    private_connection_resource_id = azurerm_redis_cache.main.id
    subresource_names              = ["redisCache"]
    is_manual_connection           = false
  }

  private_dns_zone_group {
    name                 = "redis-dns-zone-group"
    private_dns_zone_ids = [var.private_dns_zone_id]
  }

  tags = local.tags
}
