# Nexus Data Engine

Гибридная аналитическая платформа, сочетающая гибкость Markdown-документации с мощностью профессиональных систем визуализации данных и бескомпромиссным подходом к безопасности.

## Архитектура

Проект построен на микросервисной архитектуре с использованием следующих технологий:

- **Java 21** + **Spring Boot 4.x**
- **Spring Cloud Gateway** - API Gateway
- **Netflix Eureka** - Service Discovery
- **Apache Kafka** - Message Broker
- **PostgreSQL** - База данных
- **WebSocket (STOMP)** - Real-time коммуникации

## Микросервисы

### 1. Nexus Discovery Service (Eureka Server)
- Порт: `8761`
- Service Discovery для всех микросервисов

### 2. Nexus Auth Service
- Порт: `9000`
- Аутентификация и авторизация
- JWT токены (Access + Refresh)
- Управление пользователями

### 3. Nexus Core Service
- Порт: `8081`
- Основная бизнес-логика
- Управление проектами и файлами
- Версионирование
- Интеграция с Kafka

### 4. Nexus Data Processor Service
- Порт: `8082`
- Импорт данных из SQL баз
- Конвертация в формат .nxdt (Parquet)
- Фоновая обработка задач

### 5. Nexus Sync Service
- Порт: `8085`
- WebSocket сервер для real-time коммуникаций
- Управление "комнатами" (rooms)
- Блокировка файлов
- Синхронизация курсоров

### 6. Nexus Gateway
- Порт: `8000`
- Единая точка входа
- Маршрутизация запросов
- JWT валидация

## Быстрый старт

### Требования

- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM

### Запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/baibeicha/Nexus-Data-Engine.git
cd Nexus-Data-Engine
```

2. Установите переменную окружения для JWT секрета:
```bash
export JWT_SECRET="your-256-bit-secret-key-here-must-be-at-least-32-characters-long"
```

3. Запустите все сервисы:
```bash
docker-compose up -d
```

4. Проверьте статус сервисов:
```bash
docker-compose ps
```

### Доступ к сервисам

- **API Gateway**: http://localhost:8000
- **Eureka Dashboard**: http://localhost:8761
- **Auth Service**: http://localhost:9000
- **Core Service**: http://localhost:8081
- **Sync Service (WebSocket)**: ws://localhost:8085/ws/sync

## API Endpoints

### Аутентификация

```bash
# Регистрация
POST http://localhost:8000/api/v1/auth/register
Content-Type: application/json

{
    "username": "user@example.com",
    "password": "password123"
}

# Получение токенов
POST http://localhost:8000/api/v1/auth/tokens
Content-Type: application/json

{
    "username": "user@example.com",
    "password": "password123"
}
```

### Проекты

```bash
# Создать проект
POST http://localhost:8000/api/v1/projects
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "ownerId": "user@example.com",
    "name": "My Project",
    "description": "Project description"
}

# Получить список проектов
GET http://localhost:8000/api/v1/projects
Authorization: Bearer <access_token>

# Получить проект по ID
GET http://localhost:8000/api/v1/projects/{projectId}
Authorization: Bearer <access_token>

# Удалить проект
DELETE http://localhost:8000/api/v1/projects/{projectId}
Authorization: Bearer <access_token>

# Поделиться проектом
POST http://localhost:8000/api/v1/projects/{projectId}/share
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "userId": "owner@example.com",
    "targetUserId": "user2@example.com",
    "accessLevel": "EDITOR"
}
```

### Файловая система

```bash
# Получить список файлов проекта
GET http://localhost:8000/api/v1/files/project/{projectId}
Authorization: Bearer <access_token>

# Создать папку/файл
POST http://localhost:8000/api/v1/files
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "projectId": "project-uuid",
    "parentId": "parent-uuid",
    "name": "New Folder",
    "type": "FOLDER",
    "userId": "user@example.com"
}

# Переместить файл/папку
PUT http://localhost:8000/api/v1/files/{id}/move
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "targetParentId": "new-parent-uuid",
    "userId": "user@example.com"
}

# Переименовать файл/папку
PUT http://localhost:8000/api/v1/files/{id}/rename
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "newName": "New Name",
    "userId": "user@example.com"
}

# Удалить файл/папку
DELETE http://localhost:8000/api/v1/files/{id}?userId=user@example.com
Authorization: Bearer <access_token>
```

### Импорт данных

```bash
# Импорт из SQL базы
POST http://localhost:8000/api/v1/import/sql
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "userId": "user@example.com",
    "projectId": "project-uuid",
    "targetFolderId": "folder-uuid",
    "connection": {
        "url": "jdbc:postgresql://host:5432/db",
        "user": "admin",
        "password": "secret"
    },
    "query": "SELECT * FROM sales WHERE year = 2024"
}
```

### Контент

```bash
# Скачать файл
GET http://localhost:8000/api/v1/content/{fileNodeId}?userId=user@example.com
Authorization: Bearer <access_token>

# Скачать конкретную версию файла
GET http://localhost:8000/api/v1/content/{fileNodeId}/version/{version}
Authorization: Bearer <access_token>

# Загрузить файл
POST http://localhost:8000/api/v1/content/upload
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

file: <file>
projectId: project-uuid
parentId: parent-uuid
userId: user@example.com
```

### Версии

```bash
# Получить историю версий файла
GET http://localhost:8000/api/v1/versions/file/{fileNodeId}
Authorization: Bearer <access_token>

# Откатиться к версии
POST http://localhost:8000/api/v1/versions/{fileNodeId}/rollback
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "targetVersion": 2
}
```

## WebSocket API

### Подключение

```javascript
const socket = new SockJS('http://localhost:8085/ws/sync');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
});
```

### Подписка на проект

```javascript
stompClient.subscribe('/topic/project/{projectId}', function(message) {
    console.log('Project update: ' + message.body);
});

stompClient.subscribe('/topic/project/{projectId}/notifications', function(message) {
    console.log('Notification: ' + message.body);
});
```

### Подписка на файл

```javascript
stompClient.subscribe('/topic/file/{fileId}', function(message) {
    console.log('File update: ' + message.body);
});

stompClient.subscribe('/topic/file/{fileId}/locks', function(message) {
    console.log('Lock update: ' + message.body);
});

stompClient.subscribe('/topic/file/{fileId}/cursors', function(message) {
    console.log('Cursor update: ' + message.body);
});
```

### Блокировка файла

```javascript
// Заблокировать файл
stompClient.send('/app/file/{fileId}/lock', {}, JSON.stringify({}));

// Разблокировать файл
stompClient.send('/app/file/{fileId}/unlock', {}, JSON.stringify({}));
```

## Разработка

### Структура проекта

```
Nexus-Data-Engine/
├── Nexus Auth Service/          # Сервис аутентификации
├── Nexus Core Service/          # Основной сервис
├── Nexus Data Processor Service/ # Обработка данных
├── Nexus Discovery Service/     # Service Discovery
├── Nexus Gateway/               # API Gateway
├── Nexus Sync Service/          # WebSocket сервер
├── Nexus Desktop/               # JavaFX Desktop приложение
├── docker-compose.yml           # Docker Compose конфигурация
└── README.md                    # Документация
```

### Сборка отдельного сервиса

```bash
cd "Nexus Core Service"
./gradlew clean bootJar
```

### Запуск отдельного сервиса

```bash
cd "Nexus Core Service"
./gradlew bootRun
```

### Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Запуск тестов конкретного сервиса
cd "Nexus Core Service"
./gradlew test
```

## Безопасность

- Все endpoints защищены JWT аутентификацией (кроме /api/v1/auth/**)
- Пароли хешируются с использованием BCrypt
- TLS 1.3 для передачи данных
- Проверка прав доступа на уровне проектов

## Лицензия

MIT License
