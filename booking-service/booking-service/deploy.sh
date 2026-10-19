#!/bin/bash
set -e

echo "🚀 Azure Deployment Script for Booking Service"
echo "==============================================="

# Step 1: Build the Docker image locally
echo ""
echo "📦 Step 1: Building Docker image..."
docker build -t booking-service:latest .

# Step 2: Login to Azure
echo ""
echo "🔐 Step 2: Logging into Azure..."
az login

# Step 3: Set subscription (if you have multiple)
# Uncomment and set your subscription ID if needed
# az account set --subscription "YOUR_SUBSCRIPTION_ID"

# Step 4: Initialize Terraform
echo ""
echo "🏗️  Step 3: Initializing Terraform..."
cd terraform
terraform init

# Step 5: Plan infrastructure
echo ""
echo "📋 Step 4: Planning Terraform infrastructure..."
terraform plan -out=tfplan

# Step 6: Apply infrastructure (will prompt for postgres password if not set)
echo ""
echo "⚙️  Step 5: Creating Azure infrastructure..."
if [ -z "$TF_VAR_postgres_admin_password" ]; then
  read -sp "Enter PostgreSQL admin password: " POSTGRES_PASSWORD
  echo ""
  export TF_VAR_postgres_admin_password="$POSTGRES_PASSWORD"
fi
terraform apply tfplan

# Step 7: Get ACR credentials
echo ""
echo "🔑 Step 6: Getting ACR credentials..."
ACR_NAME=$(terraform output -raw acr_login_server | cut -d'.' -f1)
az acr login --name $ACR_NAME

# Step 8: Tag and push image to ACR
echo ""
echo "📤 Step 7: Pushing image to ACR..."
ACR_SERVER=$(terraform output -raw acr_login_server)
docker tag booking-service:latest $ACR_SERVER/booking-service:latest
docker push $ACR_SERVER/booking-service:latest

# Step 9: Restart container to pull new image
echo ""
echo "🔄 Step 8: Restarting booking service container..."
RESOURCE_GROUP=$(terraform output -raw resource_group_name)
az container restart --resource-group $RESOURCE_GROUP --name booking-service

# Step 10: Display URLs
echo ""
echo "✅ Deployment complete!"
echo "======================="
echo ""
echo "Booking Service URL:"
terraform output booking_service_url
echo ""
echo "To test the API, run:"
echo "curl -X POST \$(terraform output -raw booking_service_url)/api/bookings \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"user_id\":\"user-123\",\"origin\":\"London\",\"destination\":\"Paris\",\"idempotency_key\":\"test-001\",\"time_window_start\":\"2025-04-01T08:00:00Z\",\"time_window_end\":\"2025-04-01T10:00:00Z\"}'"

cd ..
