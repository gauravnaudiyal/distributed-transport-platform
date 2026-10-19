variable "project"        { type = string }
variable "environment"    { type = string }
variable "region_key"     { type = string }
variable "azure_location" { type = string }
variable "tags"           { type = map(string) }

variable "vnet_cidr"        { type = string; default = "10.0.0.0/16" }
variable "aks_subnet_cidr"  { type = string; default = "10.0.0.0/22" }
variable "data_subnet_cidr" { type = string; default = "10.0.8.0/24" }

variable "kubernetes_version" { type = string; default = "1.29" }
variable "aks_node_count"     { type = number }
variable "aks_node_vm_size"   { type = string }

variable "postgres_sku"            { type = string }
variable "postgres_admin_password" { type = string; sensitive = true }
variable "postgres_backup_retention_days" {
  description = "Number of days to retain PostgreSQL backups. Set to 0 to disable (dev); 7–35 for enabled (prod)."
  type        = number
  default     = 7
}

variable "redis_capacity" { type = number }
variable "redis_family"   { type = string }
variable "redis_sku"      { type = string }

variable "ghcr_server"   { type = string }
variable "ghcr_username" { type = string }
variable "ghcr_token"    { type = string; sensitive = true }
