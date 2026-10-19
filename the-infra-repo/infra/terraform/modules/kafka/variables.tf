variable "resource_group_name" { type = string }
variable "location"            { type = string }
variable "environment"         { type = string }
variable "region_name"         { type = string }
variable "sku" {
  type    = string
  default = "Standard"
}
variable "capacity" {
  type    = number
  default = 1
}

variable "topics" {
  description = "Event Hub (Kafka) topic names to create. Add new topics here when a microservice needs one."
  type        = list(string)
  default     = []
}

variable "consumer_group_name" {
  description = "Consumer group name created for each topic (services share or use per-service groups via client config)."
  type        = string
  default     = "gtbs-consumer"
}
