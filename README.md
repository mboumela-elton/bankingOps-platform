# Banking Operations Platform

Spring Boot 4.0 API for managing banking transactions, deployed with Docker, Kubernetes (kind), Helm and GitOps (ArgoCD).

## Project Structure

```
bankingops-platform/
├── .github/workflows/      # GitHub Actions CI/CD pipeline
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

## 1. Local Development

```bash
./app/init-db.sh
./app/run-app.sh
# or
cd app && mvn spring-boot:run -DskipTests
```

---

## 2. Docker Compose

```bash
cd app
cp .env.example .env
docker compose up -d --build
docker compose logs -f api
docker compose down
```

---

## 3. Kubernetes (kind)

```bash
# Create cluster
kind create cluster --name bankingops

# Build and load image
docker build -t bankingops-api:latest app/
kind load docker-image bankingops-api:latest --name bankingops

# Deploy
./k8s/deploy.sh

# Verify
kubectl get pods -n bankingops-dev
kubectl port-forward svc/transaction-api 8000:80 -n bankingops-dev
curl http://localhost:8000/health
```

---

## 4. Helm

```bash
helm lint helm/bankingops

# Dev
helm upgrade --install bankingops helm/bankingops \
  -f helm/bankingops/values-dev.yaml \
  -n bankingops-dev --create-namespace

# Prod-like
helm upgrade --install bankingops helm/bankingops \
  -f helm/bankingops/values-prod-like.yaml \
  -n bankingops-prod --create-namespace
```

---

## 5. GitOps (ArgoCD + GitHub)

ArgoCD surveille le repo GitHub et synchronise automatiquement le cluster.

### Installer ArgoCD

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/application-crd.yaml
kubectl delete networkpolicies -n argocd --all
kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=180s
```

### Accéder à l'UI

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
# https://localhost:8080  |  login: admin
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d
```

### Connecter le repo GitHub

Via l'UI ArgoCD : Settings → Repositories → Connect Repo (HTTPS + token GitHub)

### Déployer l'application

```bash
kubectl apply -f gitops/argocd-application-dev.yaml
kubectl get applications -n argocd
```

### Workflow GitOps

```bash
# Modifier une valeur, committer et pousser
git push origin develop
# ArgoCD détecte et synchronise automatiquement (< 3 min)
kubectl get pods -n bankingops-dev -w
```

---

## 6. CI/CD (GitHub Actions)

Le pipeline `.github/workflows/ci-cd.yaml` se déclenche sur chaque push sur `develop` ou `master` :

1. Tests unitaires Maven
2. Build Docker image
3. Scan Trivy (vulnérabilités)
4. Push vers Docker Hub
5. Mise à jour du tag dans `values-dev.yaml` → ArgoCD sync

### Secrets GitHub requis

| Secret | Description |
|--------|-------------|
| `DOCKERHUB_USERNAME` | Ton username Docker Hub |
| `DOCKERHUB_TOKEN` | Token Docker Hub (Settings → Security) |
| `GH_PAT` | GitHub Personal Access Token (scope: `repo`) |

---

## Ingress — Convention de nommage

| Environnement | Format | Exemple |
|---------------|--------|---------|
| Hors-prod | `<api>.bankingops.hors.prod.fr` | `transaction-api.bankingops.hors.prod.fr` |
| Prod | `<api>.msel.fr` | `transaction-api.msel.fr` |

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

## Monitoring

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
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
| CI/CD | GitHub Actions |
| GitOps | ArgoCD |
| Registry | Docker Hub |
| Monitoring | Spring Boot Actuator |
