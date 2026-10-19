terraform {
  required_version = ">= 1.5"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

# Azure Container Registry (Basic tier - cheapest)
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true
}

# PostgreSQL Flexible Server (Burstable B1ms - cheapest)
resource "azurerm_postgresql_flexible_server" "postgres" {
  name                   = var.postgres_server_name
  resource_group_name    = azurerm_resource_group.main.name
  location               = azurerm_resource_group.main.location
  version                = "16"
  administrator_login    = var.postgres_admin_user
  administrator_password = var.postgres_admin_password
  storage_mb             = 32768  # 32GB minimum
  sku_name               = "B_Standard_B1ms"  # 1 vCore, 2GB RAM - $12/month

  # Allow public access for demo
  public_network_access_enabled = true

  # Ignore zone changes for different regions
  lifecycle {
    ignore_changes = [zone]
  }
}

# PostgreSQL Firewall - Allow Azure services
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure" {
  name             = "allow-azure-services"
  server_id        = azurerm_postgresql_flexible_server.postgres.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# PostgreSQL Database
resource "azurerm_postgresql_flexible_server_database" "booking_db" {
  name      = "booking_db"
  server_id = azurerm_postgresql_flexible_server.postgres.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

# Azure Cache for Redis (Basic C0 - cheapest)
resource "azurerm_redis_cache" "redis" {
  name                = var.redis_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  capacity            = 0
  family              = "C"
  sku_name            = "Basic"  # $16/month, 250MB
  enable_non_ssl_port = true     # For demo simplicity
  minimum_tls_version = "1.2"
}

# Kafka Container in ACI (self-hosted - cheapest option)
resource "azurerm_container_group" "kafka" {
  name                = "kafka-zk"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  os_type             = "Linux"
  ip_address_type     = "Public"
  dns_name_label      = "${var.prefix}-kafka"

  # Zookeeper container
  container {
    name   = "zookeeper"
    image  = "confluentinc/cp-zookeeper:7.6.0"
    cpu    = "0.25"
    memory = "0.5"

    ports {
      port     = 2181
      protocol = "TCP"
    }

    environment_variables = {
      ZOOKEEPER_CLIENT_PORT = "2181"
      ZOOKEEPER_TICK_TIME   = "2000"
    }
  }

  # Kafka container
  container {
    name   = "kafka"
    image  = "confluentinc/cp-kafka:7.6.0"
    cpu    = "0.5"
    memory = "1.0"

    ports {
      port     = 9092
      protocol = "TCP"
    }

    environment_variables = {
      KAFKA_BROKER_ID                        = "1"
      KAFKA_ZOOKEEPER_CONNECT                = "localhost:2181"
      KAFKA_ADVERTISED_LISTENERS             = "PLAINTEXT://${var.prefix}-kafka.${var.location}.azurecontainer.io:9092"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR = "1"
    }
  }
}

# Booking Service Container in ACI
resource "azurerm_container_group" "booking_service" {
  name                = "booking-service"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  os_type             = "Linux"
  ip_address_type     = "Public"
  dns_name_label      = "${var.prefix}-booking"

  # Wait for dependencies
  depends_on = [
    azurerm_postgresql_flexible_server_database.booking_db,
    azurerm_redis_cache.redis,
    azurerm_container_group.kafka
  ]

  image_registry_credential {
    server   = azurerm_container_registry.acr.login_server
    username = azurerm_container_registry.acr.admin_username
    password = azurerm_container_registry.acr.admin_password
  }

  container {
    name   = "booking-service"
    image  = "${azurerm_container_registry.acr.login_server}/booking-service:latest"
    cpu    = "1.0"
    memory = "1.5"

    ports {
      port     = 8081
      protocol = "TCP"
    }

    # Override application.yml with environment variables
    environment_variables = {
      SPRING_PROFILES_ACTIVE                           = "azure"
      SERVER_PORT                                      = "8081"
      SPRING_DATASOURCE_URL                            = "jdbc:postgresql://${azurerm_postgresql_flexible_server.postgres.fqdn}:5432/booking_db?sslmode=require"
      SPRING_DATASOURCE_USERNAME                       = "${var.postgres_admin_user}@${var.postgres_server_name}"
      SPRING_DATA_REDIS_HOST                           = azurerm_redis_cache.redis.hostname
      SPRING_DATA_REDIS_PORT                           = azurerm_redis_cache.redis.port
      SPRING_KAFKA_BOOTSTRAP_SERVERS                   = "${azurerm_container_group.kafka.fqdn}:9092"
      BOOKING_KAFKA_TOPIC                              = "booking-events"
      BOOKING_OUTBOX_POLL_INTERVAL_MS                  = "1000"
      BOOKING_IDEMPOTENCY_TTL_MINUTES                  = "60"
    }

    secure_environment_variables = {
      SPRING_DATASOURCE_PASSWORD = var.postgres_admin_password
      SPRING_DATA_REDIS_PASSWORD = azurerm_redis_cache.redis.primary_access_key
    }
  }
}
