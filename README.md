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

3. To stop and remove the container (but keep the data volume):
   ```bash
   docker-compose down
   ```

4. To remove everything including the volume:
   ```bash
   docker-compose down -v
   ```
   Remember that after a full reset, your database will be empty and you'll need to run your application to reinitialize it with any seed data (like the test user) defined in DataInitializer.java.

4. Alternatively, if you just want to restart the container without stopping and recreating it:
   ```bash
   docker restart ludonova_db
   ```

5. Run the application:
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

## Development Setup
### SSL Certificate
1. Generate a self-signed certificate:
```bash
keytool -genkeypair -alias ludonova -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ludonova.p12 -validity 365
```