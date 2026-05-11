#!/bin/bash

################################################################################
# ArgoCD Installation & Configuration Script (Local Gitea)
# Usage: ./install-argocd.sh
################################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

GITEA_URL="http://gitea.gitea.svc.cluster.local:3000"
GITEA_USER="msel"
GITEA_PASSWORD="msel123"
REPO_URL="${GITEA_URL}/${GITEA_USER}/bankingops-platform.git"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo -e "${YELLOW}=== Installing ArgoCD (local Gitea) ===${NC}\n"

# 1. Install ArgoCD
echo -e "${YELLOW}1. Installing ArgoCD...${NC}"
sudo kubectl create namespace argocd --dry-run=client -o yaml | sudo kubectl apply -f -
sudo kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml 2>&1 | tail -3
sudo kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=180s
echo -e "${GREEN}✓ ArgoCD ready${NC}\n"

# 2. Configure Gitea repo (no auth needed for public repo)
echo -e "${YELLOW}2. Configuring Gitea repository...${NC}"
sudo kubectl create secret generic repo-gitea-bankingops \
    --namespace=argocd \
    --from-literal=type=git \
    --from-literal=url="${REPO_URL}" \
    --from-literal=username="${GITEA_USER}" \
    --from-literal=password="${GITEA_PASSWORD}" \
    --dry-run=client -o yaml | sudo kubectl apply -f -
sudo kubectl label secret repo-gitea-bankingops \
    argocd.argoproj.io/secret-type=repository \
    -n argocd --overwrite
echo -e "${GREEN}✓ Repository configured${NC}\n"

# 3. Deploy ArgoCD applications
echo -e "${YELLOW}3. Deploying ArgoCD applications...${NC}"
sudo kubectl apply -f "$SCRIPT_DIR/argocd-application-dev.yaml"
echo -e "${GREEN}✓ Applications deployed${NC}\n"

# 4. Get admin password
ADMIN_PASSWORD=$(sudo kubectl -n argocd get secret argocd-initial-admin-secret \
    -o jsonpath="{.data.password}" | base64 -d)

echo -e "${GREEN}=== Done! ===${NC}\n"
echo "ArgoCD UI:"
echo "  kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "  https://localhost:8080  |  admin / ${ADMIN_PASSWORD}"
echo ""
echo "Check sync:"
echo "  kubectl get applications -n argocd"
