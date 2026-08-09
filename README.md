# Star Recommendation Service

Сервис рекомендаций банковских продуктов для банка «Стар».

## 📌 О проекте
Сервис выдачи персонализированных рекомендаций банковских продуктов на основе транзакционной активности клиентов. Поддерживает как фиксированные, так и динамические правила рекомендаций, которыми могут управлять менеджеры через REST API.

## 🛠 Технологии
- **Java 17**
- **Spring Boot 3.1.5**
- **PostgreSQL 18**
- **Spring Data JPA (Hibernate)**
- **Liquibase**
- **Maven**
- **JUnit 5 / Mockito**

## 🚀 Быстрый старт

### Требования
- Java 17+
- PostgreSQL 18+
- Maven (или использовать встроенный Maven Wrapper)

### Переменные окружения
DB_URL=jdbc:postgresql://localhost:5432/star_rules
DB_USERNAME=postgres
DB_PASSWORD=your_password
# Сборка проекта
./mvnw clean package

# Запуск приложения
java -jar target/star-0.0.1-SNAPSHOT.jar
Проверка работы
curl http://localhost:8080/rule
Документация
Требования
Архитектура
REST API
Развертывание
GitHub Project
Доска задач
Команда
Разработчик: @snyiper34
