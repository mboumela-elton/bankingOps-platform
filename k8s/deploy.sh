#!/bin/bash

################################################################################
# Kubernetes Deployment Script
# Usage: ./deploy.sh [deploy|delete|status|logs]
################################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

NAMESPACE="bankingops-dev"
CLUSTER_NAME="bankingops"
IMAGE_NAME="bankingops-api:latest"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

deploy() {
    echo -e "${YELLOW}=== Deploying Banking Operations Platform to Kubernetes ===${NC}\n"

    # Build Docker image
    echo -e "${YELLOW}1. Building Docker image...${NC}"
    sudo docker build -t $IMAGE_NAME "$SCRIPT_DIR/../app"
    echo -e "${GREEN}✓ Image built${NC}\n"

    # Load image into kind cluster
    echo -e "${YELLOW}2. Loading image into kind cluster...${NC}"
    sudo kind load docker-image $IMAGE_NAME --name $CLUSTER_NAME
    echo -e "${GREEN}✓ Image loaded into kind${NC}\n"

    # Apply manifests in order
    echo -e "${YELLOW}3. Applying Kubernetes manifests...${NC}"
    sudo kubectl apply -f "$SCRIPT_DIR/namespace.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/configmap.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/secret.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/postgres-service.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/postgres-statefulset.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/api-service.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/api-deployment.yaml"
    sudo kubectl apply -f "$SCRIPT_DIR/hpa.yaml"
    echo -e "${GREEN}✓ Manifests applied${NC}\n"

    # Wait for postgres
    echo -e "${YELLOW}4. Waiting for PostgreSQL to be ready...${NC}"
    sudo kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=120s
    echo -e "${GREEN}✓ PostgreSQL ready${NC}\n"

    # Wait for API
    echo -e "${YELLOW}5. Waiting for API to be ready...${NC}"
    sudo kubectl wait --for=condition=ready pod -l app=transaction-api -n $NAMESPACE --timeout=120s
    echo -e "${GREEN}✓ API ready${NC}\n"

    echo -e "${GREEN}=== Deployment complete! ===${NC}\n"
    status
}

delete() {
    echo -e "${RED}Deleting all resources in namespace $NAMESPACE...${NC}"
    sudo kubectl delete namespace $NAMESPACE --ignore-not-found
    echo -e "${GREEN}✓ Resources deleted${NC}"
}

status() {
    echo -e "${YELLOW}=== Cluster Status ===${NC}"
    echo -e "\nNodes:"
    sudo kubectl get nodes
    echo -e "\nPods:"
    sudo kubectl get pods -n $NAMESPACE
    echo -e "\nServices:"
    sudo kubectl get svc -n $NAMESPACE
    echo -e "\nHPA:"
    sudo kubectl get hpa -n $NAMESPACE 2>/dev/null || echo "No HPA found"
}

logs() {
    echo -e "${YELLOW}=== API Logs ===${NC}"
    sudo kubectl logs deploy/transaction-api -n $NAMESPACE --tail=50
}

port_forward() {
    echo -e "${YELLOW}Port-forwarding transaction-api to localhost:8000...${NC}"
    echo -e "Access the API at: http://localhost:8000"
    echo -e "Press Ctrl+C to stop\n"
    sudo kubectl port-forward svc/transaction-api 8000:80 -n $NAMESPACE
}

case "${1:-deploy}" in
    deploy)       deploy ;;
    delete)       delete ;;
    status)       status ;;
    logs)         logs ;;
    port-forward) port_forward ;;
    *)
        echo "Usage: $0 {deploy|delete|status|logs|port-forward}"
        exit 1
        ;;
esac
