File Converter Service

Микросервис конвертации файлов в PDF. Получает события из Apache Kafka, скачивает файл из MinIO, конвертирует в PDF, загружает результат обратно в MinIO и отправляет событие в выходной топик Kafka.

## Архитектура

Kafka (files.input) → Consumer → MinIO (скачать) → Конвертер → MinIO (загрузить) → Kafka (files.output)

Для идемпотентности реализован паттерн **Transactional Inbox** — каждое входящее сообщение сохраняется в PostgreSQL со статусом `RECEIVED`, после успешной обработки статус меняется на `PROCESSED`. Повторная доставка того же сообщения из Kafka игнорируется.

Выбор конвертера реализован через паттерны **Strategy + Factory** — без `if-else`, через список реализаций интерфейса `FileConverter`.

## Поддерживаемые форматы

- TXT
- PNG
- JPG
- ZIP (конвертируются все файлы внутри архива, результат объединяется в один PDF)

## Технологии

- Java 17
- Spring Boot 3.x
- Apache Kafka
- MinIO (S3-совместимое хранилище)
- PostgreSQL
- Hibernate / Spring Data JPA
- OpenPDF
- Docker / Docker Compose
- JUnit 5

## Запуск

### 1. Запустить инфраструктуру

```bash
docker compose up -d
```

Поднимаются контейнеры:
- Kafka + Zookeeper (порт 9092)
- MinIO (порт 9000, UI на 9001)
- PostgreSQL (порт 5432)

### 2. Создать бакеты в MinIO

Открыть http://localhost:9001 (логин: `minioadmin`, пароль: `minioadmin`) и создать два бакета:
- `source-files` — для исходных файлов
- `converted-files` — для готовых PDF

### 3. Загрузить файл в MinIO

Загрузить любой файл (например `test.txt`) в бакет `source-files` через UI.

### 4. Запустить приложение

```bash
mvn spring-boot:run
```

### 5. Отправить сообщение в Kafka

```bash
docker exec -it file-converter-kafka-1 kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic files.input
```

Вставить сообщение:

```json
{"messageId":"test-001","bucket":"source-files","filePath":"test.txt"}
```

### 6. Проверить результат

Открыть бакет `converted-files` в MinIO UI — там появится `test.pdf`.

Проверить выходной топик Kafka:

```bash
docker exec -it file-converter-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic files.output \
  --from-beginning
```

## Запуск тестов

```bash
mvn test
```

## Структура проекта
src/main/java/com/example/file_converter/
├── config/          # Конфигурация MinIO
├── converter/       # Интерфейс и реализации конвертеров
├── exception/       # Кастомные исключения
├── inbox/           # Transactional Inbox (entity + repository + service)
├── kafka/           # Consumer и Producer
├── model/           # DTO для Kafka сообщений
└── service/         # Основная бизнес-логика