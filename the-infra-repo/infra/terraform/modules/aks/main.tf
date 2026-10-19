locals {
  cluster_name = "gtbs-${var.region_name}-${var.environment}-aks"
  tags = {
    project = "gtbs", region = var.region_name
    environment = var.environment, managed-by = "terraform"
  }
}

resource "azurerm_kubernetes_cluster" "main" {
  name                = local.cluster_name
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = "gtbs-${var.region_name}-${var.environment}"
  kubernetes_version  = var.kubernetes_version

  identity {
    type = "SystemAssigned"
  }

  default_node_pool {
    name           = "system"
    node_count     = var.node_count
    vm_size        = var.node_vm_size
    vnet_subnet_id = var.subnet_id

    # Enable autoscaler so idle nodes can scale down during demos
    enable_auto_scaling = true
    min_count           = 1
    max_count           = var.node_count

    # OS disk type: Ephemeral is faster and cheaper than Managed
    os_disk_type = "Ephemeral"

    upgrade_settings {
      max_surge = "10%"
    }
  }

  # Disable add-ons to save cost — we'll install our own observability
  http_application_routing_enabled = false

  network_profile {
    network_plugin    = "azure"
    load_balancer_sku = "standard"
    outbound_type     = "loadBalancer"
  }

  # Omit automatic_channel_upgrade to keep upgrades manual for demo stability.

  tags = local.tags
}
