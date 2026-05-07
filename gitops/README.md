# GitOps avec ArgoCD

## Qu'est-ce que GitOps ?

GitOps est une pratique où **Git est la seule source de vérité** pour l'état du cluster.
Tu ne déploies plus avec `kubectl apply` ou `helm upgrade` à la main.
Tu pousses un commit, ArgoCD détecte le changement et synchronise le cluster automatiquement.

```
Developer → git push → GitHub → ArgoCD détecte → kubectl apply automatique
```

## Déploiement manuel vs GitOps

| | Déploiement manuel | GitOps (ArgoCD) |
|---|---|---|
| Comment déployer | `helm upgrade` sur ta machine | `git push` |
| Traçabilité | Aucune (qui a déployé quoi ?) | Historique Git complet |
| Rollback | `helm rollback` manuel | `git revert` + sync auto |
| Dérive de config | Invisible | ArgoCD détecte et corrige |
| Audit | Difficile | Chaque déploiement = un commit |
| Collaboration | Risque de conflits | PR/MR comme workflow |

## Installation ArgoCD

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=120s
```

## Accéder à l'UI ArgoCD

```bash
# Port-forward vers l'UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Récupérer le mot de passe admin
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Ouvrir dans le navigateur
# https://localhost:8080
# Login: admin / <mot de passe ci-dessus>
```

## Déployer les applications ArgoCD

```bash
# Dev
kubectl apply -f gitops/argocd-application-dev.yaml

# Prod-like
kubectl apply -f gitops/argocd-application-prod-like.yaml
```

## Vérifier le statut

```bash
kubectl get applications -n argocd
```

Résultat attendu :
```
NAME                   SYNC STATUS   HEALTH STATUS
bankingops-dev         Synced        Healthy
bankingops-prod-like   Synced        Healthy
```

## Tester le GitOps : changer le nombre de replicas

C'est ici que la magie opère. Pour passer de 1 à 3 replicas en dev :

### 1. Modifier values-dev.yaml dans Git

```yaml
# helm/bankingops/values-dev.yaml
api:
  replicas: 3   # était 1
```

### 2. Pousser le commit

```bash
git add helm/bankingops/values-dev.yaml
git commit -m "feat: scale dev to 3 replicas"
git push
```

### 3. Observer la synchronisation

ArgoCD vérifie le repo toutes les **3 minutes** par défaut.
Tu peux forcer la sync immédiatement depuis l'UI ou avec :

```bash
argocd app sync bankingops-dev
```

### 4. Vérifier que les pods ont scalé

```bash
kubectl get pods -n bankingops-dev -w
```

Tu verras les nouveaux pods apparaître automatiquement, sans avoir touché `kubectl` ou `helm`.

## Concepts clés des fichiers ArgoCD

### `targetRevision: HEAD`
ArgoCD suit toujours le dernier commit de la branche principale.
En prod réelle, on pointe vers un tag (`v1.2.3`) pour contrôler exactement ce qui est déployé.

### `automated.selfHeal: true`
Si quelqu'un fait un `kubectl edit` directement sur le cluster, ArgoCD détecte la dérive
et remet le cluster dans l'état décrit dans Git. Git gagne toujours.

### `automated.prune: true`
Si tu supprimes une ressource de Git (ex: tu supprimes le HPA), ArgoCD la supprime
aussi dans le cluster. Sans ça, les ressources orphelines s'accumulent.

### `finalizers`
Quand tu supprimes l'Application ArgoCD, il supprime aussi toutes les ressources
Kubernetes qu'il gérait. Évite les ressources fantômes.

## Structure des fichiers

```
gitops/
├── argocd-application-dev.yaml        # App ArgoCD pour l'env dev
├── argocd-application-prod-like.yaml  # App ArgoCD pour l'env prod-like
└── README.md                          # Ce fichier
```

## Workflow complet GitOps

```
1. Tu modifies un fichier dans helm/ ou k8s/
2. git commit + git push
3. ArgoCD détecte le changement (polling toutes les 3 min ou webhook)
4. ArgoCD compare l'état Git vs l'état du cluster
5. Si différent → ArgoCD applique les changements
6. Le cluster est synchronisé avec Git
```

## Commandes utiles

```bash
# Voir toutes les applications
kubectl get applications -n argocd

# Détail d'une application
kubectl describe application bankingops-dev -n argocd

# Forcer une synchronisation
kubectl patch application bankingops-dev -n argocd \
  --type merge \
  -p '{"operation":{"initiatedBy":{"username":"admin"},"sync":{"revision":"HEAD"}}}'

# Voir l'historique des déploiements
kubectl get application bankingops-dev -n argocd -o jsonpath='{.status.history}' | python3 -m json.tool
```
