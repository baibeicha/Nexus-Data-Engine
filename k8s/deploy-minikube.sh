#!/bin/bash

# Nexus Data Engine - Minikube Deployment Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Nexus Data Engine - Minikube Deployment${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if minikube is running
if ! minikube status &> /dev/null; then
    echo -e "${YELLOW}Starting minikube...${NC}"
    minikube start --driver=docker --memory=8192 --cpus=4
fi

# Enable required addons
echo -e "${YELLOW}Enabling minikube addons...${NC}"
minikube addons enable ingress
minikube addons enable metrics-server

# Set docker env to use minikube's docker
echo -e "${YELLOW}Setting up docker environment...${NC}"
eval $(minikube docker-env)

# Build images
echo -e "${YELLOW}Building Docker images...${NC}"

cd "../Nexus Discovery Service"
docker build -t nexus-discovery-service:latest .

cd "../Nexus Auth Service"
docker build -t nexus-auth-service:latest .

cd "../Nexus Core Service"
docker build -t nexus-core-service:latest .

cd "../Nexus Data Processor Service"
docker build -t nexus-data-processor-service:latest .

cd "../Nexus Sync Service"
docker build -t nexus-sync-service:latest .

cd "../Nexus Gateway"
docker build -t nexus-gateway:latest .

cd ../k8s

# Apply Kubernetes manifests
echo -e "${YELLOW}Applying Kubernetes manifests...${NC}"

kubectl apply -f 00-namespace.yaml
echo -e "${GREEN}✓ Namespace created${NC}"

kubectl apply -f 01-configmap.yaml
echo -e "${GREEN}✓ ConfigMap and Secrets created${NC}"

kubectl apply -f 02-postgres.yaml
echo -e "${GREEN}✓ PostgreSQL deployed${NC}"

kubectl apply -f 03-redis.yaml
echo -e "${GREEN}✓ Redis deployed${NC}"

kubectl apply -f 04-kafka.yaml
echo -e "${GREEN}✓ Kafka deployed${NC}"

# Wait for infrastructure
echo -e "${YELLOW}Waiting for infrastructure to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=postgres-auth -n nexus-data-engine --timeout=120s
kubectl wait --for=condition=ready pod -l app=postgres-core -n nexus-data-engine --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis -n nexus-data-engine --timeout=120s
kubectl wait --for=condition=ready pod -l app=kafka -n nexus-data-engine --timeout=180s

echo -e "${GREEN}✓ Infrastructure is ready${NC}"

# Deploy application services
echo -e "${YELLOW}Deploying application services...${NC}"

kubectl apply -f 05-discovery-service.yaml
echo -e "${GREEN}✓ Discovery Service deployed${NC}"

kubectl apply -f 06-auth-service.yaml
echo -e "${GREEN}✓ Auth Service deployed${NC}"

kubectl apply -f 07-core-service.yaml
echo -e "${GREEN}✓ Core Service deployed${NC}"

kubectl apply -f 08-data-processor-service.yaml
echo -e "${GREEN}✓ Data Processor Service deployed${NC}"

kubectl apply -f 09-sync-service.yaml
echo -e "${GREEN}✓ Sync Service deployed${NC}"

kubectl apply -f 10-gateway.yaml
echo -e "${GREEN}✓ Gateway deployed${NC}"

# Wait for all services
echo -e "${YELLOW}Waiting for all services to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=discovery-service -n nexus-data-engine --timeout=180s
kubectl wait --for=condition=ready pod -l app=auth-service -n nexus-data-engine --timeout=180s
kubectl wait --for=condition=ready pod -l app=core-service -n nexus-data-engine --timeout=180s
kubectl wait --for=condition=ready pod -l app=data-processor -n nexus-data-engine --timeout=180s
kubectl wait --for=condition=ready pod -l app=sync-service -n nexus-data-engine --timeout=180s
kubectl wait --for=condition=ready pod -l app=gateway -n nexus-data-engine --timeout=180s

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Nexus Data Engine deployed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${GREEN}Service URLs:${NC}"
echo -e "  API Gateway:    http://$(minikube ip):30800"
echo -e "  Eureka:         http://$(minikube ip):30761"
echo ""
echo -e "${YELLOW}To view pods:${NC}"
echo -e "  kubectl get pods -n nexus-data-engine"
echo ""
echo -e "${YELLOW}To view logs:${NC}"
echo -e "  kubectl logs -f <pod-name> -n nexus-data-engine"
echo ""
echo -e "${YELLOW}To delete deployment:${NC}"
echo -e "  kubectl delete namespace nexus-data-engine"
echo ""
