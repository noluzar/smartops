# SmartOps – Backend API

A secure, enterprise-grade **Project & Task Management REST API** built with Java Spring Boot.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.3 |
| Security | Spring Security + JWT |
| Database | H2 (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Documentation | Swagger / OpenAPI 3 |
| Build Tool | Maven |

---

## Architecture
```
smartops/
├── controller/       # REST API endpoints
├── service/          # Business logic
├── repository/       # Data access layer
├── entity/           # JPA entities
├── dto/              # Request/Response objects
├── security/         # JWT filter, config, utilities
├── exception/        # Global exception handler
├── enums/            # Role, TaskStatus
└── config/           # CORS, OpenAPI config
```

---

## Features

- **JWT Authentication** — Stateless token-based auth with BCrypt password hashing
- **Role-Based Access Control** — ADMIN, MANAGER, MEMBER roles
- **Project Management** — Create projects, assign members, track tasks
- **Task Management** — Full CRUD with status tracking (TODO, IN_PROGRESS, DONE)
- **Global Exception Handling** — Consistent error responses across all endpoints
- **API Documentation** — Swagger UI with JWT authorization support
- **Validation** — DTO-level input validation on all endpoints

---

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login and get JWT token | Public |

### Projects
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/projects` | Create a project | Required |
| GET | `/api/projects` | Get my projects | Required |
| GET | `/api/projects/{id}` | Get project by ID | Required |
| POST | `/api/projects/{id}/members/{userId}` | Add member to project | Required |
| DELETE | `/api/projects/{id}` | Delete a project | Required |

### Tasks
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/tasks` | Create a task | Required |
| GET | `/api/tasks/project/{projectId}` | Get tasks by project | Required |
| PUT | `/api/tasks/{id}` | Update task | Required |
| DELETE | `/api/tasks/{id}` | Delete task | Required |

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/smartops.git
cd smartops
```

**2. Create `src/main/resources/application-local.yml`**
```yaml
jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000
```

**3. Run the application**
```bash
mvn spring-boot:run
```

**4. Access the API**
- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

---

## Authentication

All protected endpoints require a JWT token in the request header:
```
Authorization: Bearer <your_token>
```

To get a token:
1. Call `POST /api/auth/register` or `POST /api/auth/login`
2. Copy the `token` from the response
3. Include it in the `Authorization` header of subsequent requests

In Swagger UI, click **Authorize** and paste your token.

---

##  Database

The application uses **H2 in-memory database** for development. All data resets on restart.

H2 Console connection details:
- URL: `jdbc:h2:mem:smartops`
- Username: `sa`
- Password: `password123`

---

## User Roles

| Role | Description |
|---|---|
| ADMIN | Full system access |
| MANAGER | Manage projects and tasks |
| MEMBER | View and update assigned tasks |

New users are assigned the **MEMBER** role by default on registration.

---

## License

MIT License — feel free to use this project as a reference or template.