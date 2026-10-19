output "resource_group_name" {
  description = "Resource Group name"
  value       = azurerm_resource_group.main.name
}

output "booking_service_url" {
  description = "Public URL of the booking service"
  value       = "http://${azurerm_container_group.booking_service.fqdn}:8081"
}

output "acr_login_server" {
  description = "ACR login server"
  value       = azurerm_container_registry.acr.login_server
}

output "postgres_fqdn" {
  description = "PostgreSQL server FQDN"
  value       = azurerm_postgresql_flexible_server.postgres.fqdn
}

output "redis_hostname" {
  description = "Redis cache hostname"
  value       = azurerm_redis_cache.redis.hostname
}

output "kafka_fqdn" {
  description = "Kafka FQDN"
  value       = azurerm_container_group.kafka.fqdn
}
