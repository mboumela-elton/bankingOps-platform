# Banking Operations Platform

Spring Boot 4.0 API for managing banking transactions, deployed locally with Docker, Kubernetes (kind), Helm and GitOps (ArgoCD).

## Project Structure

```
bankingops-platform/
├── app/                    # Spring Boot application
│   ├── src/                # Source code
│   ├── tests/              # Test scripts
│   ├── Dockerfile          # Multi-stage build
│   ├── docker-compose.yml  # Local stack (API + PostgreSQL)
│   └── .env.example        # Environment variables template
├── k8s/                    # Raw Kubernetes manifests
├── helm/bankingops/        # Helm chart
│   ├── values.yaml         # Default values
│   ├── values-dev.yaml     # Dev overrides
│   └── values-prod-like.yaml
└── gitops/                 # ArgoCD applications
```

---

## Prerequisites

- Java 17, Maven
- Docker & Docker Compose
- kubectl, kind, helm
- PostgreSQL (local dev only)

---

## 1. Local Development (Maven + local PostgreSQL)

```bash
# Initialize database
./app/init-db.sh

# Start application
./app/run-app.sh

# Or manually
cd app && mvn spring-boot:run -DskipTests
```

---

## 2. Docker Compose

```bash
cd app

# Create env file
cp .env.example .env

# Start API + PostgreSQL
docker compose up -d --build

# Stop
docker compose down

# Logs
docker compose logs -f api
```

---

## 3. Kubernetes (kind)

### Create cluster

```bash
kind create cluster --name bankingops
```

### Build and load image

```bash
docker build -t bankingops-api:latest app/
kind load docker-image bankingops-api:latest --name bankingops
```

### Deploy manifests

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/postgres-service.yaml
kubectl apply -f k8s/postgres-statefulset.yaml
kubectl apply -f k8s/api-service.yaml
kubectl apply -f k8s/api-deployment.yaml
kubectl apply -f k8s/hpa.yaml
```

### Or use the deploy script

```bash
./k8s/deploy.sh
```

### Verify

```bash
kubectl get pods -n bankingops-dev
kubectl get svc -n bankingops-dev
kubectl logs deploy/transaction-api -n bankingops-dev
```

### Access the API

```bash
kubectl port-forward svc/transaction-api 8000:80 -n bankingops-dev
curl http://localhost:8000/health
```

---

## 4. Helm

### Lint and preview

```bash
helm lint helm/bankingops
helm template bankingops helm/bankingops -f helm/bankingops/values-dev.yaml
```

### Deploy

```bash
# Dev
helm upgrade --install bankingops helm/bankingops \
  -f helm/bankingops/values-dev.yaml \
  -n bankingops-dev --create-namespace

# Prod-like
helm upgrade --install bankingops helm/bankingops \
  -f helm/bankingops/values-prod-like.yaml \
  -n bankingops-prod --create-namespace
```

### Manage releases

```bash
helm list -n bankingops-dev
helm history bankingops -n bankingops-dev
helm rollback bankingops 1 -n bankingops-dev
helm uninstall bankingops -n bankingops-dev
```

---

## 5. GitOps (ArgoCD + Gitea local)

ArgoCD surveille un repo **Gitea local** dans le cluster — aucune dépendance réseau externe.

### Architecture

```
git push → Gitea (in-cluster) ← ArgoCD surveille → kubectl apply automatique
```

### Déployer ArgoCD + Gitea

```bash
# Créer les namespaces
kubectl create namespace argocd
kubectl create namespace gitea

# Déployer Gitea
kubectl apply -f gitops/gitea.yaml

# Installer ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/application-crd.yaml

# Supprimer les NetworkPolicies (lab local)
kubectl delete networkpolicies -n argocd --all

# Attendre que tout soit prêt
kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=180s
kubectl wait --for=condition=available deployment/gitea -n gitea --timeout=120s
```

### Configurer Gitea et pousser le code

```bash
# Port-forward Gitea
kubectl port-forward svc/gitea 3000:3000 -n gitea &

# Créer l'utilisateur admin
kubectl exec -n gitea deployment/gitea -- gitea admin user create \
  --username msel --password msel123 --email msel@local.dev \
  --admin --must-change-password=false

# Créer le repo
curl -s -X POST http://localhost:3000/api/v1/user/repos \
  -H "Content-Type: application/json" -u "msel:msel123" \
  -d '{"name":"bankingops-platform","private":false,"auto_init":false,"default_branch":"develop"}'

# Pousser le code
git remote add gitea http://msel:msel123@localhost:3000/msel/bankingops-platform.git
git push gitea develop --force
```

### Configurer ArgoCD

```bash
# Secret repo Gitea
kubectl create secret generic repo-gitea-bankingops \
  --namespace=argocd \
  --from-literal=type=git \
  --from-literal=url=http://gitea.gitea.svc.cluster.local:3000/msel/bankingops-platform.git \
  --from-literal=username=msel \
  --from-literal=password=msel123 \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl label secret repo-gitea-bankingops \
  argocd.argoproj.io/secret-type=repository -n argocd

# Déployer l'application ArgoCD
kubectl apply -f gitops/argocd-application-dev.yaml
```

### Accéder à ArgoCD UI

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
# https://localhost:8080  |  login: admin
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d
```

### Vérifier la synchronisation

```bash
kubectl get applications -n argocd
kubectl get pods -n bankingops-dev
```

### Workflow GitOps

```bash
# 1. Modifier une valeur (ex: replicas dans values-dev.yaml)
# 2. Pousser vers Gitea
git add helm/bankingops/values-dev.yaml
git commit -m "feat: scale to 3 replicas"
git push gitea develop
# 3. ArgoCD détecte et synchronise automatiquement (< 3 min)
kubectl get pods -n bankingops-dev -w
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/users` | Create user |
| GET | `/users/{id}` | Get user |
| GET | `/users/{id}/transactions` | Get user transactions |
| POST | `/transactions` | Create transaction |
| GET | `/transactions/{id}` | Get transaction |
| POST | `/transactions/{id}/validate` | Validate transaction |
| POST | `/transactions/{id}/fail` | Fail transaction |

## Monitoring (Actuator)

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## Tests

```bash
# API tests
./app/tests/test-api.sh

# Actuator tests
./app/tests/test-actuator.sh

# Docker integration tests
./app/tests/docker-test.sh
```

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.0 |
| Database | PostgreSQL 14 |
| Build | Maven |
| Container | Docker |
| Orchestration | Kubernetes (kind) |
| Packaging | Helm |
| GitOps | ArgoCD |
| Monitoring | Spring Boot Actuator |
