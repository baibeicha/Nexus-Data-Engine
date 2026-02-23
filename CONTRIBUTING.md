# Contributing to Nexus Data Engine

Спасибо за интерес к проекту Nexus Data Engine! Этот документ содержит руководство по внесению изменений в проект.

## Как внести вклад

### 1. Сообщение об ошибках

Если вы нашли ошибку, пожалуйста, создайте issue с подробным описанием:

- Описание проблемы
- Шаги для воспроизведения
- Ожидаемое поведение
- Фактическое поведение
- Окружение (OS, Java версия, etc.)

### 2. Предложение новых функций

Для предложения новой функции создайте issue с:

- Описанием функции
- Обоснованием (почему это нужно)
- Возможной реализацией

### 3. Pull Requests

1. Форкните репозиторий
2. Создайте ветку для ваших изменений (`git checkout -b feature/amazing-feature`)
3. Внесите изменения
4. Запустите тесты
5. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
6. Запушьте в ветку (`git push origin feature/amazing-feature`)
7. Откройте Pull Request

## Стандарты кода

### Java

- Следуйте [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Максимальная длина строки: 120 символов
- Используйте 4 пробела для отступов
- Все публичные методы должны иметь JavaDoc

### Именование

- Классы: `PascalCase`
- Методы и переменные: `camelCase`
- Константы: `UPPER_SNAKE_CASE`
- Пакеты: `lowercase`

### Коммиты

Используйте conventional commits:

- `feat:` - новая функция
- `fix:` - исправление ошибки
- `docs:` - изменения документации
- `style:` - изменения форматирования
- `refactor:` - рефакторинг кода
- `test:` - добавление тестов
- `chore:` - обслуживание

Пример:
```
feat(core): add file encryption support

- Add CryptoService for AES-256-GCM encryption
- Add SecureMemoryContainer for secure data storage
- Add encryption configuration options
```

## Тестирование

### Запуск тестов

```bash
# Все тесты
make test-all

# Тесты конкретного сервиса
cd "Nexus Core Service" && ./gradlew test
```

### Покрытие кода

- Минимальное покрытие: 70%
- Критичные компоненты: 90%

## Архитектура

### Микросервисы

```
Nexus-Data-Engine/
├── Nexus Auth Service/          # Аутентификация
├── Nexus Core Service/          # Основная логика
├── Nexus Data Processor Service/ # Обработка данных
├── Nexus Discovery Service/     # Service Discovery
├── Nexus Gateway/               # API Gateway
├── Nexus Sync Service/          # WebSocket сервер
└── Nexus Desktop/               # Desktop приложение
```

### Взаимодействие сервисов

```
Client -> Gateway -> Auth Service (JWT validation)
                -> Core Service (business logic)
                -> Sync Service (WebSocket)
                -> Data Processor (background jobs)
```

## Безопасность

- Все endpoints должны быть защищены (кроме auth)
- Пароли хешируются с BCrypt
- Используйте prepared statements для SQL
- Валидируйте все входные данные
- Не логируйте чувствительные данные

## Производительность

- Используйте кэширование где возможно
- Минимизируйте количество запросов к БД
- Используйте пагинацию для больших списков
- Оптимизируйте Kafka consumers

## Документация

- Обновляйте README.md при изменении API
- Добавляйте JavaDoc для публичных методов
- Обновляйте CHANGELOG.md

## Лицензия

Внося изменения, вы соглашаетесь с тем, что ваш код будет распространяться под лицензией MIT.

## Контакты

- Email: your-email@example.com
- Issues: https://github.com/baibeicha/Nexus-Data-Engine/issues
