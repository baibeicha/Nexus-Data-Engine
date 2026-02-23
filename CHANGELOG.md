# Changelog

Все значимые изменения в проекте будут документироваться в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/),
и этот проект придерживается [Semantic Versioning](https://semver.org/lang/ru/).

## [Unreleased]

### Added
- Базовая архитектура микросервисов
- Сервис аутентификации (Nexus Auth Service)
- Сервис управления проектами и файлами (Nexus Core Service)
- Сервис обработки данных (Nexus Data Processor Service)
- WebSocket сервер для real-time коммуникаций (Nexus Sync Service)
- API Gateway (Nexus Gateway)
- Service Discovery (Nexus Discovery Service)
- Docker Compose конфигурация для запуска всех сервисов
- Шифрование данных с использованием AES-256-GCM
- SecureMemoryContainer для безопасного хранения данных в памяти
- Глобальная обработка исключений
- JWT аутентификация и авторизация
- Версионирование файлов
- Импорт данных из SQL баз
- WebSocket комнаты для совместной работы
- Блокировка файлов
- Тесты для CryptoService и SecureMemoryContainer
- API тесты
- Makefile для удобного управления
- Документация (README.md, CONTRIBUTING.md)

### Security
- AES-256-GCM шифрование для данных
- PBKDF2 для генерации ключей
- JWT токены с access и refresh
- BCrypt для хеширования паролей

## [0.1.0] - 2026-02-23

### Added
- Initial MVP release
- Basic project structure
- Core services implementation
- Docker support

[Unreleased]: https://github.com/baibeicha/Nexus-Data-Engine/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/baibeicha/Nexus-Data-Engine/releases/tag/v0.1.0
