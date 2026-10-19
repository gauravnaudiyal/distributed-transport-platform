# Azure Deployment Guide

Complete guide for deploying the Booking Service to Azure using Docker and Terraform.

## Architecture

```
Azure Cloud
├── Azure Container Instances (booking-service)
├── Azure Container Registry (Docker images)
├── Azure Database for PostgreSQL Flexible Server
├── Azure Cache for Redis
└── Kafka/Zookeeper (self-hosted in ACI)
```

## Prerequisites

### 1. Install Required Tools

```bash
# Azure CLI
brew install azure-cli

# Terraform
brew install terraform

# Docker Desktop (already installed)
```

### 2. Azure Account Setup

- Sign up for [Azure for Students](https://azure.microsoft.com/en-us/free/students) ($100 free credits)
- Verify your student status
- Login to Azure CLI:

```bash
az login
az account show  # Verify correct subscription
```

## Configuration

### 1. Edit Terraform Variables

Open `terraform/terraform.tfvars` and change the following to **globally unique** values:

```hcl
prefix               = "yourname123"        # Change this
acr_name             = "yourname123acr"     # Change this (alphanumeric only)
postgres_server_name = "yourname123-pg"     # Change this
redis_name           = "yourname123-redis"  # Change this
```

### 2. Set PostgreSQL Password

**Option A: Environment Variable (Recommended)**
```bash
export TF_VAR_postgres_admin_password="YourSecurePassword123!"
```

**Option B: Let Terraform Prompt**
Just run `terraform apply` and it will ask for the password interactively.

**Option C: Add to terraform.tfvars (Not Recommended - will be in git history)**
```hcl
postgres_admin_password = "YourSecurePassword123!"
```

## Deployment

### Option 1: Automated Deployment (Easiest)

Run the all-in-one deployment script:

```bash
cd booking-service
./deploy.sh
```

This will:
1. Build the Docker image locally
2. Login to Azure
3. Initialize Terraform
4. Create all Azure resources
5. Push the Docker image to Azure Container Registry
6. Deploy the booking service container
7. Display the public URL

### Option 2: Manual Step-by-Step

#### Step 1: Build Docker Image

```bash
cd booking-service
docker build -t booking-service:latest .
```

Expected output: Image size ~550MB

#### Step 2: Initialize Terraform

```bash
cd terraform
terraform init
```

#### Step 3: Review Infrastructure Plan

```bash
terraform plan
```

Review the resources that will be created:
- Resource Group
- Container Registry
- PostgreSQL Flexible Server
- Redis Cache
- 2 Container Groups (Kafka + Booking Service)

#### Step 4: Apply Infrastructure

```bash
terraform apply
```

Type `yes` when prompted. This will take **5-10 minutes**.

#### Step 5: Push Docker Image to ACR

```bash
# Login to ACR
ACR_NAME=$(terraform output -raw acr_login_server | cut -d'.' -f1)
az acr login --name $ACR_NAME

# Tag and push image
ACR_SERVER=$(terraform output -raw acr_login_server)
docker tag booking-service:latest $ACR_SERVER/booking-service:latest
docker push $ACR_SERVER/booking-service:latest
```

#### Step 6: Restart Container

```bash
RESOURCE_GROUP=$(terraform output -raw resource_group_name)
az container restart --resource-group $RESOURCE_GROUP --name booking-service
```

#### Step 7: Get Public URL

```bash
terraform output booking_service_url
```

## Testing the Deployment

### 1. Create a Booking

```bash
BOOKING_URL=$(cd terraform && terraform output -raw booking_service_url)

curl -X POST $BOOKING_URL/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "user-123",
    "origin": "London",
    "destination": "Paris",
    "idempotency_key": "azure-test-001",
    "time_window_start": "2025-04-01T08:00:00Z",
    "time_window_end": "2025-04-01T10:00:00Z"
  }'
```

Expected response:
```json
{
  "bookingId": "abc-123-xyz",
  "userId": "user-123",
  "state": "CONFIRMED",
  "mapVersion": "v2025.03",
  "coordinatorRegion": "eu-west-1",
  "createdAt": "2025-03-31T..."
}
```

### 2. Get Booking

```bash
BOOKING_ID="<booking-id-from-above>"
curl $BOOKING_URL/api/bookings/$BOOKING_ID
```

### 3. Cancel Booking

```bash
curl -X POST $BOOKING_URL/api/bookings/$BOOKING_ID/cancel
```

### 4. Verify Booking

```bash
curl $BOOKING_URL/api/bookings/$BOOKING_ID/verify
```

## Monitoring & Troubleshooting

### View Container Logs

```bash
RESOURCE_GROUP=$(cd terraform && terraform output -raw resource_group_name)

# Booking service logs
az container logs --resource-group $RESOURCE_GROUP --name booking-service

# Kafka logs
az container logs --resource-group $RESOURCE_GROUP --name kafka-zk --container-name kafka

# Zookeeper logs
az container logs --resource-group $RESOURCE_GROUP --name kafka-zk --container-name zookeeper
```

### Check Container Status

```bash
az container show --resource-group $RESOURCE_GROUP --name booking-service \
  --query "{Status:instanceView.state, IP:ipAddress.fqdn}" -o table
```

### Common Issues

#### Issue: Container won't start
```bash
# Check container events
az container show --resource-group $RESOURCE_GROUP --name booking-service
```

**Solution:** Check logs for errors. Common causes:
- Database connection failure (check firewall rules)
- Image pull failure (verify ACR credentials)
- Resource limits too low

#### Issue: Database connection timeout

**Solution:** Check PostgreSQL firewall rules:
```bash
az postgres flexible-server firewall-rule list \
  --resource-group $RESOURCE_GROUP \
  --name <postgres-server-name>
```

Add your IP if needed:
```bash
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --name <postgres-server-name> \
  --rule-name allow-my-ip \
  --start-ip-address <your-ip> \
  --end-ip-address <your-ip>
```

#### Issue: Redis connection failure

**Solution:** Verify Redis is running and get access key:
```bash
az redis show --resource-group $RESOURCE_GROUP --name <redis-name>
az redis list-keys --resource-group $RESOURCE_GROUP --name <redis-name>
```

## Cost Management

### Monthly Cost Estimate (Full-time)

| Resource | Tier | Monthly Cost |
|----------|------|--------------|
| Azure Container Registry | Basic | $5 |
| PostgreSQL Flexible Server | B_Standard_B1ms | $12 |
| Azure Cache for Redis | Basic C0 | $16 |
| Kafka/Zookeeper ACI | 0.75 vCPU, 1.5GB RAM | $4 |
| Booking Service ACI | 1 vCPU, 1.5GB RAM | $6 |
| **Total** | | **~$43/month** |

### Cost Optimization Tips

#### 1. Stop Containers When Not in Use

ACI charges per second. Stop containers between demos:

```bash
RESOURCE_GROUP=$(cd terraform && terraform output -raw resource_group_name)

# Stop booking service
az container stop --resource-group $RESOURCE_GROUP --name booking-service

# Stop Kafka/Zookeeper
az container stop --resource-group $RESOURCE_GROUP --name kafka-zk

# Start them again later
az container start --resource-group $RESOURCE_GROUP --name booking-service
az container start --resource-group $RESOURCE_GROUP --name kafka-zk
```

**With 8 hours/week usage: ~$15-20/month total**

#### 2. Use Lower PostgreSQL Tier

Edit `terraform/main.tf`:
```hcl
sku_name = "B_Standard_B1s"  # Changed from B1ms
```

Saves ~$6/month (but slower performance).

#### 3. Delete Everything After Demo

When completely done:
```bash
cd terraform
terraform destroy
```

Type `yes` to confirm. This removes all resources and stops all charges.

## Updating the Application

After making code changes:

```bash
# 1. Rebuild Docker image
docker build -t booking-service:latest .

# 2. Push to ACR
ACR_SERVER=$(cd terraform && terraform output -raw acr_login_server)
docker tag booking-service:latest $ACR_SERVER/booking-service:latest
docker push $ACR_SERVER/booking-service:latest

# 3. Restart container to pull new image
RESOURCE_GROUP=$(cd terraform && terraform output -raw resource_group_name)
az container restart --resource-group $RESOURCE_GROUP --name booking-service
```

## Accessing Azure Portal

View and manage resources in the Azure Portal:
1. Go to https://portal.azure.com
2. Navigate to Resource Groups → `rg-booking-demo`
3. See all provisioned resources

Useful views:
- **Container Instances** → View logs, restart, see metrics
- **PostgreSQL** → Connect, run queries, view monitoring
- **Redis Cache** → Check connection info, view metrics
- **Container Registry** → See images, repositories

## Security Notes

⚠️ **This is a DEMO configuration - not production-ready!**

Current security gaps:
- Public IP addresses for all services
- No VNet isolation
- Non-SSL Redis connection (Basic tier limitation)
- Admin credentials in Terraform state

For production, you would add:
- Azure Virtual Network (VNet)
- Private endpoints for databases
- Azure Key Vault for secrets
- Azure Front Door or Application Gateway
- SSL/TLS everywhere
- Azure Active Directory authentication

## Next Steps

1. **Add CI/CD**: GitHub Actions to auto-deploy on push
2. **Add Monitoring**: Azure Application Insights
3. **Add Alerts**: Email notifications on errors
4. **Scale Up**: Increase container CPU/RAM in `main.tf`
5. **Deploy Other Services**: Duplicate pattern for API Gateway, etc.

## Support

- **Azure Documentation**: https://learn.microsoft.com/en-us/azure/
- **Terraform Azure Provider**: https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs
- **Azure for Students**: https://azure.microsoft.com/en-us/free/students

## Cleanup Checklist

Before final submission/demo:
- [ ] Test all API endpoints
- [ ] Verify logs show no errors
- [ ] Document the public URL
- [ ] Take screenshots for report
- [ ] Stop containers if not actively demoing
- [ ] After grading, run `terraform destroy`
