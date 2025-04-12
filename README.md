# Tennis Tournament Web Application

A **client-server** web application that helps organize tennis tournaments. The system has **three user roles** – **Administrator**, **Referee**, and **Player** – each with distinct abilities to manage and participate in the tournaments.

This repository contains the **Spring Boot** backend (Java 17) and a **React (MUI)** frontend. The database used is **MySQL**. The application demonstrates **Builder**, **Strategy**, and **Singleton** patterns (the latter courtesy of Spring’s `@Service`/`@Bean` scopes), **JWT-based authentication**, a **token refresh** mechanism, scheduling tasks via **Spring Scheduler**, and advanced integrations like **Prometheus**, **Sentry**, and **Swagger/OpenAPI**.

---

## Table of Contents
- [Features Overview](#features-overview)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Database Schema & Seeding](#database-schema--seeding)
- [Running the Application](#running-the-application)
- [Detailed Features](#detailed-features)
- [Detailed Explanation of Components](#detailed-explanation-of-components)
- [Using the Frontend](#using-the-frontend)
- [REST API (Swagger UI)](#rest-api-swagger-ui)
- [Testing](#testing)
- [Troubleshooting & Tips](#troubleshooting--tips)
- [License](#license)

---

## Features Overview
- **Register & Login** with encoded passwords (`BCrypt`).
- **JWT Access & Refresh tokens** for secure endpoints.
- **Role-based access control** (ADMIN, REFEREE, PLAYER).
- **Tournament Management**: create tournaments, set start/end dates, registration deadlines, min/max players.
- **User Management**: Admin can list, update, and delete users.
- **Match Management**: create matches (assign players/referee, schedule times).
- **Score Management**: only assigned referee (or admin) can update scores.
- **Export**: Admin can export match data in `.csv` or `.txt` formats (Strategy pattern).
- **Scheduler**: Cancels tournaments with insufficient players after registration deadlines.
- **Real-time Monitoring**: Prometheus and Spring Actuator integration.
- **Error Tracking**: Sentry integration for exception monitoring.
- **React Frontend**: MUI components, dynamic theming (light/dark), and role-specific dashboards.

---

## Architecture & Design Patterns
- **Layered Architecture**: Controller → Service → Repository → Model → Security.
- **Builder Pattern**: 
  - `TennisMatchBuilder`
  - `TournamentBuilder`
  - `UserBuilder`
  - `TokenBuilder`
- **Strategy Pattern**: 
  - `ExportService` runtime selection (`CSVExportStrategy` or `TXTExportStrategy`).
- **Singleton Pattern**: 
  - Spring-managed beans/services using `@Service` or `@Bean` annotations.

---

## Database Schema & Seeding
- Create MySQL database: `tennis_db`.
- Update `application.properties` with your MySQL credentials.
- Schema is auto-generated (`spring.jpa.hibernate.ddl-auto=create-drop` or `update`).

---

## Running the Application

### Backend (Spring Boot)
```bash
mvn clean install
mvn spring-boot:run
```

### Frontend (React)
Navigate to `frontend/`:
```bash
npm install
npm start
```

Frontend: [http://localhost:3000](http://localhost:3000)  
Backend: [http://localhost:8081](http://localhost:8081)

---

## Detailed Features

### Authentication & Authorization
- **JWT Tokens**: Short-lived access tokens (60 min) and long-lived refresh tokens.
- **Token Refresh**: Auto-refresh via frontend when access tokens expire.
- **Secure Logout**: Tokens explicitly invalidated on logout.

### Admin Endpoints
- User management.
- Tournament and match creation.
- Data export in `.csv` or `.txt`.

### Referee Endpoints
- View and update scores for assigned matches.

### Player Endpoints
- Register for tournaments.
- View matches and schedules.

### Builder Pattern Example
```java
TennisMatch match = TennisMatchBuilder.builder()
    .tournament(tournament)
    .player1(p1)
    .player2(p2)
    .referee(ref)
    .startTime(startTime)
    .endTime(endTime)
    .score("")
    .build();
```

### Strategy Pattern (Export)
- Select between `csv` and `txt` exports dynamically.

### Scheduled Tasks
- Auto-cancellation of tournaments with insufficient players after deadlines.

---

## Detailed Explanation of Components

### Spring Boot Backend Services

**Spring Boot Actuator & Prometheus**
- **Actuator**: Exposes metrics and health endpoints (`/actuator/*`).
- **Prometheus**: Scrapes metrics for real-time monitoring and observability.

**Sentry Integration**
- Captures and reports backend exceptions and errors automatically.

**OpenAPI & Swagger**
- Auto-generates REST API documentation.
- Interactive API testing via Swagger UI (`/swagger-ui/index.html`).

---

### JWT-Based Authentication

**Workflow**:
- **Registration/Login** returns access and refresh tokens.
- **Refresh Token** endpoint allows seamless access renewal.
- **Token Storage**: Tokens are stored in the database for revocation checks.
- **Logout** explicitly invalidates tokens.

**Security Details**:
- Passwords hashed with `BCrypt`.
- JWTs securely signed with secret keys.

---

### Frontend (React & Material UI)

- **Material UI**: Responsive, elegant design.
- **Dynamic Themes**: Light/dark mode toggling.
- **Axios**: Auto-attach tokens to API requests.
- **Automatic Token Refresh**: Axios interceptors handle token expiry and refreshing.

**Role-Based Dashboards**:
- **Admin**: Manage users, tournaments, matches, export data.
- **Referee**: Manage assigned matches and scores.
- **Player**: Register and view tournaments/matches.

---

### Detailed Testing Strategy

- **JUnit Integration Tests** cover:
  - Authentication (login, register, logout, refresh).
  - Security (invalid/expired tokens).
  - Business logic (deadlines, overlapping tournaments).
  - CRUD for tournaments, matches, users.
  - Scheduled tasks (automatic cancellations).
  - Strategy pattern (exports).

**Testing Environment**:
- Isolated test database (`tennis_db_test`) with `create-drop` schema.

---

### Application Configuration

- **application.properties**: Production/Dev settings.
- **application-test.properties**: Isolated settings for testing.

---

### Additional Important Clarifications

- **Scheduled Tasks**:
  - Validates and cancels tournaments based on player counts post-deadline.

- **Design Patterns**:
  - **Builder** for object creation.
  - **Strategy** for export services.
  - **Singleton** via Spring context.

---

## Using the Frontend
- Dynamic routing and dashboards based on user roles.
- Smooth login, automatic token refresh, logout handling.
- Tournament registration, match viewing, score updating.

---

## REST API (Swagger UI)
- Interactive API documentation available at:
  - [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

---

## Testing
- Run backend tests:
```bash
mvn test
```
- Frontend testing (optional) with React testing libraries.

---

## Troubleshooting & Tips
- **Ports**: Backend (`8081`), Frontend (`3000`).
- **Token expiration** adjustable via `application.properties`.
- **CORS** settings configured in `WebConfig.java`.

---

## License
This project is for educational and demonstration purposes. Feel free to adapt or integrate the code into your own projects, referencing this repository.
