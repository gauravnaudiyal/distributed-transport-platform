output "namespace_name"           { value = azurerm_eventhub_namespace.main.name }
output "bootstrap_servers"        { value = "${azurerm_eventhub_namespace.main.name}.servicebus.windows.net:9093" }
output "primary_connection_string" {
  value     = azurerm_eventhub_namespace_authorization_rule.gtbs.primary_connection_string
  sensitive = true
}
