# E-Bank

A banking application built with a Spring Boot REST API and a React frontend. The project includes authentication, customer and account management, a client dashboard, and bank transfers.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Backend | Java 17, Spring Boot 3.4.12, Spring Security, Spring Data JPA, JWT |
| Frontend | React 19, React Router, Axios, Tailwind CSS, Create React App |
| Database | MySQL |
| Build tools | Maven Wrapper, npm |

## Run locally

Install JDK 17, MySQL, Node.js, and npm. The repository does not currently pin a Node.js version; use one compatible with the dependencies in `ebank-frontend/package-lock.json`.

```sh
git clone https://github.com/Ihssaaaaaaane/E-bank.git
cd E-bank
```

### 1. Configure the database and backend

Create the database in MySQL:

```sql
CREATE DATABASE ebank_db;
```

Configure the backend through environment variables in your terminal or IDE:

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Database URL; the configured default is `jdbc:mysql://localhost:3306/ebank_db` |
| `SPRING_DATASOURCE_USERNAME` | Your local database user |
| `SPRING_DATASOURCE_PASSWORD` | Your local database password |
| `JWT_SECRET` | Your own Base64-encoded signing key; use at least 32 random bytes before encoding |
| `GMAIL_USERNAME` | Gmail account used for outgoing mail |
| `GMAIL_APP_PASSWORD` | App password for that mail account |
| `MAIL_FROM` | Optional sender address; defaults to `GMAIL_USERNAME` |

Database and JWT environment variables override the corresponding Spring properties. Mail variables are referenced directly by the committed configuration. Keep real credentials outside Git. The committed defaults and seeded accounts are for local development.

### 2. Start the backend

```sh
cd ebank-backend
sh mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The frontend expects the API at `http://localhost:8080`.

### 3. Start the frontend

In another terminal, from the repository root:

```sh
cd ebank-frontend
npm ci
npm start
```

Open `http://localhost:3000`. The backend's CORS configuration allows this origin. Mail configuration is required for flows that send email, including customer onboarding and password recovery.

## Features

- JWT authentication and role-based access for agents and clients.
- Customer registration and bank account creation.
- Account overview and operation history.
- Transfers between accounts.
- Password change and recovery flows.

## Repository layout

```text
ebank-backend/
  src/main/java/com/ebank/
    config/        # Security configuration
    dtos/          # Request and response models
    entities/      # Persistence models
    repositories/  # Database access
    security/      # JWT support
    services/      # Application logic
    web/           # REST controllers
  src/main/resources/application.properties
ebank-frontend/
  src/api/         # API calls
  src/auth/        # Authentication and protected routes
  src/components/  # Shared UI
  src/pages/       # Agent, client, and authentication screens
```

## Development checks

Backend, from `ebank-backend/`:

```sh
sh mvnw test
```

Frontend, from `ebank-frontend/`:

```sh
npm test -- --watchAll=false
npm run build
```

These commands and paths were checked against the committed project files. Builds, tests, and database/mail integration have not been run as part of this documentation update.
