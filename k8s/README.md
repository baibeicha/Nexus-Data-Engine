# Nexus Data Engine - Kubernetes Deployment

Эта директория содержит Kubernetes манифесты для развёртывания Nexus Data Engine в Minikube.

## Требования

- [Minikube](https://minikube.sigs.k8s.io/docs/start/) v1.30+
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Docker](https://docs.docker.com/get-docker/)
- 8GB+ RAM
- 4+ CPU cores

## Быстрый старт

### 1. Запустить Minikube

```bash
minikube start --driver=docker --memory=8192 --cpus=4
```

### 2. Запустить деплой

```bash
cd k8s
chmod +x deploy-minikube.sh
./deploy-minikube.sh
```

### 3. Получить URL сервисов

```bash
# API Gateway
minikube service gateway -n nexus-data-engine --url

# Eureka Dashboard
minikube service discovery-service -n nexus-data-engine --url
```

## Ручной деплой

Если вы хотите развернуть сервисы вручную:

```bash
# 1. Build images
eval $(minikube docker-env)
cd "Nexus Discovery Service" && docker build -t nexus-discovery-service:latest .
cd "Nexus Auth Service" && docker build -t nexus-auth-service:latest .
cd "Nexus Core Service" && docker build -t nexus-core-service:latest .
cd "Nexus Data Processor Service" && docker build -t nexus-data-processor-service:latest .
cd "Nexus Sync Service" && docker build -t nexus-sync-service:latest .
cd "Nexus Gateway" && docker build -t nexus-gateway:latest .

# 2. Apply manifests
cd k8s
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-configmap.yaml
kubectl apply -f 02-postgres.yaml
kubectl apply -f 03-redis.yaml
kubectl apply -f 04-kafka.yaml
kubectl apply -f 05-discovery-service.yaml
kubectl apply -f 06-auth-service.yaml
kubectl apply -f 07-core-service.yaml
kubectl apply -f 08-data-processor-service.yaml
kubectl apply -f 09-sync-service.yaml
kubectl apply -f 10-gateway.yaml
```

## Проверка статуса

```bash
# Просмотр всех pods
kubectl get pods -n nexus-data-engine

# Просмотр сервисов
kubectl get services -n nexus-data-engine

# Логи конкретного сервиса
kubectl logs -f <pod-name> -n nexus-data-engine

# Описание pod
kubectl describe pod <pod-name> -n nexus-data-engine
```

## Удаление деплоя

```bash
kubectl delete namespace nexus-data-engine
```

## Структура манифестов

| Файл | Описание |
|------|----------|
| `00-namespace.yaml` | Создание namespace |
| `01-configmap.yaml` | ConfigMap и Secrets |
| `02-postgres.yaml` | PostgreSQL StatefulSets |
| `03-redis.yaml` | Redis Deployment |
| `04-kafka.yaml` | Kafka и Zookeeper |
| `05-discovery-service.yaml` | Eureka Server |
| `06-auth-service.yaml` | Auth Service |
| `07-core-service.yaml` | Core Service |
| `08-data-processor-service.yaml` | Data Processor Service |
| `09-sync-service.yaml` | Sync Service |
| `10-gateway.yaml` | API Gateway |

## Ресурсы

| Сервис | CPU Request | CPU Limit | Memory Request | Memory Limit |
|--------|-------------|-----------|----------------|--------------|
| PostgreSQL | 250m | 500m | 256Mi | 512Mi |
| Redis | 100m | 250m | 64Mi | 256Mi |
| Kafka | 500m | 1000m | 512Mi | 1Gi |
| Discovery | 250m | 500m | 256Mi | 512Mi |
| Auth | 250m | 500m | 256Mi | 512Mi |
| Core | 500m | 1000m | 512Mi | 1Gi |
| Data Processor | 500m | 1000m | 512Mi | 1Gi |
| Sync | 250m | 500m | 256Mi | 512Mi |
| Gateway | 250m | 500m | 256Mi | 512Mi |

## Порты

| Сервис | Порт | NodePort |
|--------|------|----------|
| Gateway | 8000 | 30800 |
| Discovery | 8761 | 30761 |
| Auth | 9000 | - |
| Core | 8081 | - |
| Data Processor | 8082 | - |
| Sync | 8085 | - |
