#!/bin/bash
# Resume AKS cluster and refresh local kubeconfig
set -e

RG="ds-gtbs"

# 1. Automatically find the cluster name in the Resource Group
CLUSTER=$(az aks list --resource-group "$RG" --query "[0].name" -o tsv 2>/dev/null)

if [ -z "$CLUSTER" ]; then
  echo "❌ Error: No AKS cluster found in resource group: $RG"
  exit 1
fi

# 2. Start the cluster
echo "▶️ Starting AKS cluster: '$CLUSTER' in '$RG'..."
az aks start --name "$CLUSTER" --resource-group "$RG"

# 3. Update local credentials
# This ensures your 'kubectl' is pointing to the right place and has a fresh token
echo "🔐 Refreshing kubectl credentials..."
az aks get-credentials --resource-group "$RG" --name "$CLUSTER" --overwrite-existing

echo "✅ Done. Cluster is running and kubectl is ready."