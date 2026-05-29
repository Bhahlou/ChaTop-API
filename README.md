# ChaTop API

REST API backend for the ChaTop rental platform, built with Spring Boot 4 and secured with JWT.

## Prerequisites

- Java 21
- Maven 3.x
- MySQL 8+

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/Bhahlou/ChaTop-API.git
cd ChaTop-API
```

### 2. Set up environment variables

Create an `env.properties` file at the root of the project:

```properties
DB_DATABASE=chatop
DB_USER=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_base64_256bit_jwt_secret
```

> The JWT secret must be at least 256 bits (32 characters). Example to generate one:
>
> ```bash
> openssl rand -base64 32
> ```

### 3. Configure the database

Create the database in MySQL:

```sql
CREATE DATABASE chatop;
```

The schema is managed automatically by Hibernate (`ddl-auto=update`) on startup.

## Running the application

```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

### Running the tests

```bash
mvn test
```

## Advanced configuration

| Property                                 | Default value           | Description                      |
| ---------------------------------------- | ----------------------- | -------------------------------- |
| `app.upload.dir`                         | `uploads`               | Image storage directory          |
| `app.base-url`                           | `http://localhost:8080` | Base URL for image links         |
| `jwt.expiration`                         | `86400000` (24h)        | JWT token validity duration (ms) |
| `spring.servlet.multipart.max-file-size` | `10MB`                  | Maximum uploaded file size       |

These properties can be overridden in `env.properties`.

## API Documentation (Swagger)

Once the server is running, the interactive documentation is available at:

**http://localhost:8080/swagger-ui/index.html**

Bearer token authentication is built directly into the Swagger interface.
