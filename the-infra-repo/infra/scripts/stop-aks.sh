#!/bin/bash
# Save student credits by stopping AKS when not in use
set -e

RG="ds-gtbs"

# 1. Try to find the cluster name manually using Azure CLI
# This looks for the first AKS cluster name in your specified Resource Group
CLUSTER=$(az aks list --resource-group "$RG" --query "[0].name" -o tsv 2>/dev/null)

# 2. Safety check: If no cluster is found, ask for manual input
if [ -z "$CLUSTER" ]; then
  echo "❌ Error: No AKS cluster found in resource group: $RG"
  echo "Make sure you are logged in (az login) and the RG name is correct."
  exit 1
fi

# 3. Stop the cluster
echo "🛑 Stopping AKS cluster: '$CLUSTER' in Resource Group: '$RG'..."
az aks stop --name "$CLUSTER" --resource-group "$RG"

echo "✅ Done. The control plane and nodes are shut down (you won't be charged for compute)."
echo "▶️ Resume with: az aks start --name $CLUSTER --resource-group $RG"