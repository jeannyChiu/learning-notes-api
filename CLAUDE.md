# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

**Build and Run:**
```bash
./mvnw spring-boot:run     # Start the application on localhost:8080
./mvnw clean compile       # Clean and compile
./mvnw clean package      # Build JAR file
```

**Testing:**
```bash
./mvnw test               # Run all tests
./mvnw test -Dtest=ClassName#methodName  # Run specific test
```

**Database Setup:**
- Create MySQL database: `learning_notes`
- Default connection: `jdbc:mysql://localhost:3306/learning_notes`
- Auto DDL enabled via `spring.jpa.hibernate.ddl-auto=update`

## Architecture Overview

This is a Spring Boot 3.x REST API for a learning notes platform with the following key architectural components:

**Security Architecture:**
- JWT-based authentication with configurable secret from environment variables
- Custom security filters: `JwtAuthenticationFilter`, `TestEndpointFilter`
- Spring Security configuration with custom access denied handler and authentication entry point
- Password validation with custom `@StrongPassword` annotation

**Data Flow:**
```
Controller → Service → Repository → Database (MySQL)
     ↕
   DTO ↔ Entity mapping
```

**AOP Logging:**
- `LogAspect` automatically logs all API requests/responses to `api_log` table
- Captures HTTP method, URL, request body, response body, status code, and execution time

**Key Entities:**
- `User`: Authentication and user management
- `Note`: Core note entity with title/content
- `ApiLog`: Request/response logging

**Custom Exception Handling:**
- `GlobalExceptionHandler` with standardized `ErrorResponse`
- Custom exceptions: `InvalidCredentialsException`, `UserAlreadyExistsException`, `NoteNotFoundException`, `InvalidTokenException`, `InvalidPasswordException`

**Configuration Features:**
- JWT secret validation on startup via `JwtSecretConfig`
- Test endpoint toggles with IP filtering
- CORS configuration for frontend integration
- Environment-based configuration for production deployments

**Package Structure:**
```
com.jeannychiu.learningnotesapi/
├── aspect/         # AOP logging (LogAspect)
├── config/         # Security, JWT, CORS configuration
├── controller/     # REST endpoints (AuthController, NoteController)
├── dto/           # Data transfer objects (LoginRequest, RegisterRequest, UserResponse)
├── exception/     # Custom exceptions and global handler
├── filter/        # Custom filters (TestEndpointFilter)
├── model/         # JPA entities (User, Note, ApiLog)
├── repository/    # Data access layer (JPA repositories)
├── security/      # JWT utilities, auth handlers
├── service/       # Business logic (AuthService, NoteService)
├── validator/     # Custom validation (StrongPassword)
└── LearningNotesApiApplication.java
```

**Authentication Flow:**
1. Register: POST `/register` with email/password
2. Login: POST `/login` returns JWT token
3. Protected endpoints require `Authorization: Bearer {token}` header

**Notes API Endpoints:**
- GET `/notes` - List all notes with pagination
- GET `/notes/{id}` - Get specific note
- POST `/notes` - Create new note
- PUT `/notes/{id}` - Update note
- DELETE `/notes/{id}` - Delete note

All endpoints (except auth) require valid JWT token and log requests via AOP.