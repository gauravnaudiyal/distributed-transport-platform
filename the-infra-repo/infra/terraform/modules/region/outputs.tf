output "aks_cluster_name" {
  value = azurerm_kubernetes_cluster.main.name
}

output "kube_config_raw" {
  sensitive = true
  value     = azurerm_kubernetes_cluster.main.kube_config_raw
}

output "postgres_fqdn" {
  value = azurerm_postgresql_flexible_server.main.fqdn
}

output "redis_hostname" {
  value = azurerm_redis_cache.main.hostname
}

output "redis_primary_key" {
  sensitive = true
  value     = azurerm_redis_cache.main.primary_access_key
}

output "eventhub_namespace" {
  value = azurerm_eventhub_namespace.main.name
}

output "eventhub_connection_string" {
  sensitive = true
  value     = azurerm_eventhub_namespace_authorization_rule.gtbs_rw.primary_connection_string
}

output "resource_group_name" {
  value = azurerm_resource_group.region.name
}
