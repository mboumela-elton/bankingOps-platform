#!/bin/bash

################################################################################
# ArgoCD Installation & Configuration Script
# Usage: ./install-argocd.sh <github-token>
# Example: ./install-argocd.sh ghp_xxxxxxxxxxxx
################################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

GITHUB_TOKEN=${1:-""}
GITHUB_USER="mboumela-elton"
REPO_URL="https://github.com/${GITHUB_USER}/bankingOps-platform.git"
NAMESPACE="argocd"

if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${RED}Usage: $0 <github-token>${NC}"
    echo "Generate a token at: https://github.com/settings/tokens/new"
    echo "Required scope: repo (read)"
    exit 1
fi

echo -e "${YELLOW}=== Installing ArgoCD ===${NC}\n"

# 1. Create namespace
echo -e "${YELLOW}1. Creating argocd namespace...${NC}"
sudo kubectl create namespace $NAMESPACE --dry-run=client -o yaml | sudo kubectl apply -f -
echo -e "${GREEN}✓ Namespace ready${NC}\n"

# 2. Install ArgoCD
echo -e "${YELLOW}2. Installing ArgoCD...${NC}"
sudo kubectl apply -n $NAMESPACE -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml 2>&1 | tail -3
echo -e "${GREEN}✓ ArgoCD manifests applied${NC}\n"

# 3. Wait for ArgoCD to be ready
echo -e "${YELLOW}3. Waiting for ArgoCD server...${NC}"
sudo kubectl wait --for=condition=available deployment/argocd-server -n $NAMESPACE --timeout=180s
echo -e "${GREEN}✓ ArgoCD ready${NC}\n"

# 4. Configure GitHub repo with HTTPS token
echo -e "${YELLOW}4. Configuring GitHub repository...${NC}"
sudo kubectl create secret generic repo-bankingops \
    --namespace=$NAMESPACE \
    --from-literal=type=git \
    --from-literal=url=$REPO_URL \
    --from-literal=username=$GITHUB_USER \
    --from-literal=password=$GITHUB_TOKEN \
    --dry-run=client -o yaml | sudo kubectl apply -f -

sudo kubectl label secret repo-bankingops \
    argocd.argoproj.io/secret-type=repository \
    -n $NAMESPACE --overwrite
echo -e "${GREEN}✓ Repository configured${NC}\n"

# 5. Deploy ArgoCD applications
echo -e "${YELLOW}5. Deploying ArgoCD applications...${NC}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
sudo kubectl apply -f "$SCRIPT_DIR/argocd-application-dev.yaml"
echo -e "${GREEN}✓ Applications deployed${NC}\n"

# 6. Get admin password
ADMIN_PASSWORD=$(sudo kubectl -n $NAMESPACE get secret argocd-initial-admin-secret \
    -o jsonpath="{.data.password}" | base64 -d)

echo -e "${GREEN}=== ArgoCD Installation Complete ===${NC}\n"
echo "UI Access:"
echo "  Command: sudo kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "  URL:     https://localhost:8080"
echo "  Login:   admin"
echo "  Password: $ADMIN_PASSWORD"
echo ""
echo "Check status:"
echo "  sudo kubectl get applications -n argocd"
