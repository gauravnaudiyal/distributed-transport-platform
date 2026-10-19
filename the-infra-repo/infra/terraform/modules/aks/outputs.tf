output "cluster_name" { value = azurerm_kubernetes_cluster.main.name }
output "cluster_id"   { value = azurerm_kubernetes_cluster.main.id }
output "kube_config" {
  sensitive = true
  value = {
    host                   = azurerm_kubernetes_cluster.main.kube_config[0].host
    client_certificate     = azurerm_kubernetes_cluster.main.kube_config[0].client_certificate
    client_key             = azurerm_kubernetes_cluster.main.kube_config[0].client_key
    cluster_ca_certificate = azurerm_kubernetes_cluster.main.kube_config[0].cluster_ca_certificate
  }
}
output "node_resource_group" { value = azurerm_kubernetes_cluster.main.node_resource_group }
