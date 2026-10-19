variable "resource_group_name" { type = string }
variable "location"            { type = string }
variable "environment"         { type = string }
variable "region_name"         { type = string }
variable "subnet_id"           { type = string }
variable "private_dns_zone_id" { type = string }
variable "admin_username" { type = string }
variable "admin_password" {
  type      = string
  sensitive = true
}
variable "sku_name" {
  type    = string
  default = "B_Standard_B1ms"
}
variable "storage_mb" {
  type    = number
  default = 32768
}

variable "backup_retention_days" {
  description = "Number of days to retain backups. Set to 0 to disable (dev); 7–35 for enabled (prod)."
  type        = number
  default     = 7
}

# One database per microservice. Schema/migrations live in each service repo (e.g. Flyway/Liquibase).
variable "service_databases" {
  description = "List of database names to create on the shared Postgres server (one per microservice)."
  type        = list(string)
  default     = []
}
