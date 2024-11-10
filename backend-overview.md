# LudoNova Backend Overview

The LudoNova backend is a robust and scalable **Spring Boot** application that powers the game backlog management system. It is designed with a focus on security, maintainability, and performance, ensuring a seamless experience for both developers and users.

## Understanding Spring and Spring Boot

Before diving into the architecture and components of the LudoNova backend, it's essential to understand the foundational frameworks it leverages: **Spring** and **Spring Boot**.

### What is Spring?

**Spring** is a comprehensive framework for building Java applications. It provides a wide range of features, including:

- **Dependency Injection (DI):** Allows for loose coupling of components by managing their dependencies.
- **Aspect-Oriented Programming (AOP):** Enables separation of cross-cutting concerns like logging and security.
- **Data Access:** Simplifies interactions with databases through modules like Spring JDBC and Spring Data.
- **Transaction Management:** Provides declarative transaction management for consistent data operations.
- **Model-View-Controller (MVC) Framework:** Facilitates the creation of web applications with clear separation between the model, view, and controller layers.

However, setting up a Spring application often involves extensive configuration, which can be time-consuming and error-prone.

### What is Spring Boot?

**Spring Boot** builds upon the Spring framework to simplify the development process. It offers:

- **Auto-Configuration:** Automatically configures Spring components based on the dependencies present in the project.
- **Starter POMs:** Pre-configured Maven or Gradle dependencies that simplify dependency management.
- **Embedded Servers:** Allows running applications with embedded servers like Tomcat or Jetty without the need for external deployment.
- **Production-Ready Features:** Includes metrics, health checks, and externalized configuration to prepare applications for production environments.

In essence, Spring Boot reduces the boilerplate configuration, enabling developers to focus more on business logic rather than setup.

## Architecture

The backend follows a **layered architecture**, a common design pattern in Spring Boot applications that separates concerns to enhance modularity and ease of maintenance. This structure aligns with Spring's philosophy of separation of concerns and facilitates scalability and testability. The primary layers include:

- **Controller Layer:** Handles incoming HTTP requests and maps them to appropriate services.
  
  - **Why It's Separate:** Isolating controllers ensures that request handling logic is decoupled from business logic, making the system more modular and easier to manage.

- **Service Layer:** Contains the business logic and processes data between the controller and repository layers.
  
  - **Role in Spring Boot:** Services are annotated with `@Service`, a specialization of `@Component`, which allows Spring to detect and manage them as beans. This layer orchestrates operations, enforces business rules, and acts as an intermediary between controllers and repositories.

- **Repository Layer:** Manages data persistence and interacts with the PostgreSQL database using JPA/Hibernate.
  
  - **Integration with Spring Data:** Repositories are typically interfaces annotated with `@Repository`. Spring Data JPA provides implementations at runtime, abstracting the data access layer and enabling CRUD operations without boilerplate code.

### How These Layers Work Together

1. **Request Flow:**
   - A client sends an HTTP request to the application.
   - The **Controller** receives the request and delegates processing to the **Service** layer.
   - The **Service** contains the business logic and interacts with the **Repository** to perform CRUD operations.
   - The **Repository** communicates with the PostgreSQL database to fetch or persist data.
   - The **Service** processes the data and returns the result to the **Controller**.
   - The **Controller** sends the appropriate HTTP response back to the client.

2. **Benefits of Layered Architecture:**
   - **Modularity:** Each layer has a distinct responsibility, making the codebase easier to navigate and maintain.
   - **Testability:** Layers can be tested independently, facilitating unit and integration testing.
   - **Scalability:** Allows for individual layers to be scaled or modified without affecting others.
   - **Maintainability:** Enhances code readability and simplifies the addition of new features.

## Authentication and Security

Security is a paramount concern in the LudoNova backend. Leveraging Spring Boot's robust security features, the application employs a JWT (JSON Web Token) based authentication system to ensure secure and stateless user sessions.

### Understanding Spring Security

**Spring Security** is a powerful and highly customizable authentication and access-control framework. It integrates seamlessly with Spring Boot applications, providing:

- **Authentication:** Verifies the identity of users.
- **Authorization:** Controls access to resources based on user roles and permissions.
- **Protection Against Common Vulnerabilities:** Guards against threats like CSRF, XSS, and session fixation.

### Key Components:

- **JwtTokenProvider:** Generates, validates, and parses JWT tokens. It uses HMAC-SHA for token signing with a configurable secret key and expiration time.
  
  ```java
  public String generateToken(String username) { /* ... */ }
  public String getUsernameFromJWT(String token) { /* ... */ }
  public boolean validateToken(String authToken) { /* ... */ }
  ```
  
  - **Role in Spring Security:** Acts as a utility for managing JWT operations, ensuring tokens are securely created and validated.

- **JwtAuthenticationFilter:** Intercepts incoming requests to extract and validate JWT tokens. If valid, it sets the authentication in the Spring Security context.
  
  - **Integration with Spring Boot:** Extends `OncePerRequestFilter` to ensure that each request is processed once. It leverages the `JwtTokenProvider` to manage token validation and sets the security context accordingly.

- **SecurityConfig:** Configures Spring Security, defining public and protected endpoints, setting up the authentication provider, and applying security filters.
  
  - **Configuration Highlights:**
    - **CORS:** Configures Cross-Origin Resource Sharing to allow or restrict requests from specific origins.
    - **CSRF:** Disables Cross-Site Request Forgery protection for stateless APIs.
    - **Session Management:** Configures the application to be stateless, relying solely on JWT tokens without maintaining server-side sessions.
    - **Authentication Provider:** Sets up how authentication is handled, typically using a `DaoAuthenticationProvider` that retrieves user details from a data source.

### Security Features:

- **Stateless Authentication:** No server-side sessions are maintained. All authentication is handled via JWT tokens sent with each request.
  
  - **Advantages:** Enhances scalability as the server doesn't need to store session information. It also simplifies horizontal scaling and load balancing.

- **Password Encryption:** User passwords are hashed using BCrypt to ensure they are not stored in plain text.
  
  - **Security Best Practice:** Hashing passwords protects user credentials even if the database is compromised.

- **CORS Configuration:** Restricts backend access to authorized frontend origins and specific HTTP methods.
  
  - **Purpose:** Prevents unauthorized domains from making requests to the backend, mitigating potential cross-origin attacks.

## API Endpoints

The backend exposes a set of RESTful API endpoints to manage user authentication, games, and user profiles. Leveraging Spring Boot's **Spring MVC** framework, these endpoints are defined using annotations that map HTTP requests to handler methods.

### Examples:

- **Authentication:**
  - `POST /api/auth/login`: Authenticates a user and returns a JWT token.
  - `POST /api/auth/register`: Registers a new user.

- **Games Management:**
  - `GET /api/games`: Retrieves a list of games.
  - `POST /api/games`: Adds a new game to the backlog.
  - `PUT /api/games/{id}`: Updates game details.
  - `DELETE /api/games/{id}`: Removes a game from the backlog.

- **User Profiles:**
  - `GET /api/user`: Retrieves the authenticated user's profile information.

### Leveraging Spring MVC:

- **Controllers:** Annotated with `@RestController`, these classes handle HTTP requests and return responses.
- **Request Mappings:** Methods within controllers use annotations like `@GetMapping`, `@PostMapping`, etc., to map specific HTTP methods and URLs to handler functions.
- **Response Entities:** Facilitates building HTTP responses with appropriate status codes and headers.

## Database Management

The application uses **PostgreSQL** as its primary database, with the schema managed by **JPA/Hibernate**. Spring Boot's integration with Spring Data JPA simplifies database interactions, allowing developers to work with databases using Java objects rather than writing SQL queries.

### Benefits of Using JPA/Hibernate:

- **Object-Relational Mapping (ORM):** Translates Java objects to database tables, enabling developers to interact with the database using familiar Java constructs.
- **Schema Generation:** Automatically creates and updates the database schema based on entity classes, reducing manual configuration.
- **Query Abstraction:** Provides a high-level API for querying the database, such as JPQL or the Criteria API, abstracting the underlying SQL.

### Configuration:

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ludonova
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

- **spring.datasource.url:** Specifies the JDBC URL for the PostgreSQL database.
- **spring.jpa.hibernate.ddl-auto:** Determines how Hibernate handles schema generation. The `update` value ensures that the schema is automatically updated based on entity changes.

## Testing and Deployment

### Testing:

Comprehensive tests are implemented to ensure the reliability and correctness of the backend functionalities. Leveraging **Spring Boot's testing framework**, developers can write unit and integration tests with ease.

- **Running Tests:**
  
  ```bash
  ./mvnw test
  ```
  
  - **Spring Boot Test Annotations:** Annotations like `@SpringBootTest` facilitate loading the application context and testing components in an integrated environment.
  
- **Benefits:**
  - **Continuous Integration:** Ensures that new changes do not break existing functionalities.
  - **Quality Assurance:** Maintains high code quality and reliability.

### Deployment:

The backend can be containerized using **Docker** and managed with **Docker Compose**. This facilitates consistent environments across development, testing, and production.

- **Containerization with Docker:**
  
  ```bash
  docker-compose up -d
  ```
  
  - **Advantages:**
    - **Isolation:** Ensures that the application runs in a consistent environment, eliminating "it works on my machine" issues.
    - **Scalability:** Simplifies scaling services horizontally.
    - **Portability:** Enables easy deployment across different environments and cloud platforms.

### Prerequisites:

- **Java 17+:** Required to run the Spring Boot application.
- **Maven:** For building and managing dependencies.
- **Docker and Docker Compose:** For containerization and deployment.

## Integration with Frontend

The backend seamlessly integrates with the **React/Next.js** frontend of the LudoNova application. Authentication tokens are managed on the frontend and included in API requests to secure endpoints.

### API Client:

A configured Axios instance is used on the frontend to handle API requests, automatically attaching JWT tokens and handling authentication errors.

```javascript
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

- **Interceptors:** Axios interceptors can be set up to inject JWT tokens into request headers and handle responses globally, such as redirecting to the login page on a `401 Unauthorized` response.

## Conclusion

The LudoNova backend is meticulously crafted using **Spring Boot** to provide a secure, efficient, and scalable foundation for the game backlog management system. Its thoughtful layered architecture and robust security measures ensure a reliable service for users, while Spring Boot's auto-configuration and integration capabilities facilitate ease of development and maintenance. Understanding how Spring Boot orchestrates its components and leverages frameworks like Spring MVC and Spring Data JPA empowers developers to effectively manage and extend the backend functionalities.

```
