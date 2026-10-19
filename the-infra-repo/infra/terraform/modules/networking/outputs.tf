output "aks_subnet_id"          { value = azurerm_subnet.aks.id }
output "postgres_subnet_id"     { value = azurerm_subnet.postgres.id }
output "postgres_dns_zone_id"   { value = azurerm_private_dns_zone.postgres.id }
output "postgres_dns_zone_name" { value = azurerm_private_dns_zone.postgres.name }
output "vnet_id"                { value = azurerm_virtual_network.main.id }
output "redis_pe_subnet_id"     { value = azurerm_subnet.redis_pe.id }
output "redis_dns_zone_id"      { value = azurerm_private_dns_zone.redis.id }
output "redis_dns_zone_name"    { value = azurerm_private_dns_zone.redis.name }
