output "hostname"           { value = azurerm_redis_cache.main.hostname }
output "ssl_port"           { value = azurerm_redis_cache.main.ssl_port }

output "primary_access_key" {
  sensitive = true
  value     = azurerm_redis_cache.main.primary_access_key
}
