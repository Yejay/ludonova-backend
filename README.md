# LudoNova Backend

Spring Boot backend for the LudoNova game backlog manager.

## Prerequisites

- Java 17+
- Maven
- Docker and Docker Compose

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ludonova-backend.git
   ```

2. Start the PostgreSQL database:
   ```bash
   docker-compose up -d
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation

The API documentation is available at http://localhost:8080/swagger-ui.html when running in development mode.

## Database

The application uses PostgreSQL. The database schema is managed by JPA/Hibernate and will be created automatically on startup.

## Testing

Run tests with:
```bash
./mvnw test
```
```