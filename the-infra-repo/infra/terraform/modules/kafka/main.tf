# ============================================================
# Module: kafka
# Azure Event Hubs with Kafka surface — Standard 1TU ≈ $10/mo
# Kafka-compatible: services use standard Kafka client libraries
# Topics are variable-driven (envs/*/terraform.tfvars)
# ============================================================

locals {
  ns_name = "gtbs-${var.region_name}-${var.environment}-eh"
  tags = {
    project     = "gtbs"
    region      = var.region_name
    environment = var.environment
    managed-by  = "terraform"
  }
}

resource "azurerm_eventhub_namespace" "main" {
  name                = local.ns_name
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = var.sku
  capacity            = var.capacity
  tags                = local.tags
}

resource "azurerm_eventhub" "topics" {
  for_each            = toset(var.topics)
  name                = each.value
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = var.resource_group_name
  partition_count     = 4    # 4 partitions per topic — enough for dev
  message_retention   = 1    # 1 day retention — increase for prod
}

resource "azurerm_eventhub_consumer_group" "services" {
  for_each            = toset(var.topics)
  name                = var.consumer_group_name
  namespace_name      = azurerm_eventhub_namespace.main.name
  eventhub_name       = azurerm_eventhub.topics[each.value].name
  resource_group_name = var.resource_group_name
}

resource "azurerm_eventhub_namespace_authorization_rule" "gtbs" {
  name                = "gtbs-app"
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = var.resource_group_name
  listen              = true
  send                = true
  manage              = false
}
