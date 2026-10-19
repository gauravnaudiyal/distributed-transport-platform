variable "resource_group_name" {
  description = "Name of the Azure Resource Group"
  type        = string
  default     = "rg-booking-demo"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "switzerlandnorth"  # Cheap region
}

variable "prefix" {
  description = "Prefix for resource names (must be globally unique)"
  type        = string
  default     = "bkdemo"  # Change this to something unique
}

variable "acr_name" {
  description = "Azure Container Registry name (must be globally unique, alphanumeric only)"
  type        = string
  default     = "bkdemoacr"  # Change this
}

variable "postgres_server_name" {
  description = "PostgreSQL server name (must be globally unique)"
  type        = string
  default     = "bkdemo-postgres"  # Change this
}

variable "postgres_admin_user" {
  description = "PostgreSQL admin username"
  type        = string
  default     = "bookingadmin"
}

variable "postgres_admin_password" {
  description = "PostgreSQL admin password"
  type        = string
  sensitive   = true
}

variable "redis_name" {
  description = "Redis cache name (must be globally unique)"
  type        = string
  default     = "bkdemo-redis"  # Change this
}
