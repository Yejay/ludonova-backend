# LudoNova Backend
Spring Boot backend for the LudoNova game backlog manager.

## Setup
```bash
./mvnw spring-boot:run
```
```
src/
├── main/                     # Main application code
│   ├── java/                # Java source files
│   │   └── com.bht.ludonova/  # Base package
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST endpoints
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── exception/   # Custom exceptions
│   │       ├── model/       # Data models/entities
│   │       ├── repository/  # Data access layer
│   │       ├── security/    # Security configurations
│   │       ├── service/     # Business logic
│   │       └── LudoNovaApplication.java  # Main class
│   │
│   └── resources/           # Non-Java resources
│       └── application.properties  # Application config
│
└── test/                    # Test code
    └── java/               # Test files
```