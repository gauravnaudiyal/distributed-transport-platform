output "aks_cluster_name" {
  description = "AKS cluster name — use with: az aks get-credentials --name <value> --resource-group ds-gtbs"
  value       = module.aks.cluster_name
}

output "aks_kube_config_command" {
  description = "Run this to configure kubectl"
  value       = "az aks get-credentials --resource-group ${var.resource_group_name} --name ${module.aks.cluster_name}"
}

output "postgres_fqdn" {
  description = "PostgreSQL Flexible Server FQDN"
  value       = module.postgres.fqdn
}

output "redis_hostname" {
  description = "Redis Cache hostname"
  value       = module.redis.hostname
}

output "kafka_bootstrap_servers" {
  description = "Event Hubs Kafka bootstrap endpoint"
  value       = module.kafka.bootstrap_servers
}

output "k8s_namespace" {
  description = "Kubernetes namespace for GTBS services"
  value       = kubernetes_namespace.gtbs.metadata[0].name
}
