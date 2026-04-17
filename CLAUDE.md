# IndieLovers - Project Context

## About This Project
IndieLovers is a Spring Boot MVC web application for indie game enthusiasts. Features include game catalog, reviews, favorites, recommendations, notifications, and admin panel.

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.5.x, Spring Security
- **Frontend:** Thymeleaf, Bootstrap 5
- **Database:** H2 (file-based)
- **Email:** SMTP (Gmail)

## Key Files
- Main: `src/main/java/.../ProyectoFinalApplication.java`
- Config: `src/main/resources/application.properties`
- Security: `src/main/java/.../config/SecurityConfig.java`

## Running the Project
```bash
# Run main class
./mvnw spring-boot:run
# Or run ProyectoFinalApplication from IntelliJ

# Access: http://localhost:9001
# Admin: Admin / admin123
# User: RoseM / 1234
```

## Test Users
| Role | Username | Password |
|------|----------|----------|
| Admin | Admin | admin123 |
| User | RoseM | 1234 |

---

## Instructions for Claude
1. **First, always read and review `README.md`** to understand the project scope, features, and configuration before making any changes.
2. Respect the MVC architecture pattern used throughout the project.
3. When modifying security-related code, verify Spring Security configuration is not broken.
4. Check `application.properties` for email configuration requirements.
