variable "resource_group_name" { type = string }
variable "location"            { type = string }
variable "environment"         { type = string }
variable "region_name"         { type = string }
variable "subnet_id"           { type = string }
variable "node_count" {
  type    = number
  default = 2
}
variable "node_vm_size" {
  type    = string
  default = "Standard_B2s"
}
variable "kubernetes_version" {
  type    = string
  default = "1.29"
}
