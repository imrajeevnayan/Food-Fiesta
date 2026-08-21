# 🍕 Food Fiesta - Production-Grade Full-Stack Spring Boot Project

Welcome to **Food Fiesta**, a modern, high-performance dining management and ordering platform. Built on **Spring Boot 3.4.2** and **Java 21**, this application is designed for cloud-native deployment, supporting both quick-start in-memory databases (H2) and production-grade persistent databases (PostgreSQL).

This guide walks you through local development, architectural concepts, production configuration, and step-by-step cloud deployment (Render & Docker).

---

## 🏗️ Architecture & Technology Stack

The application adheres to clean **layered architecture** design principles:

- **Presentation Layer (Thymeleaf, CSS, JS)**: Server-side HTML rendering utilizing custom visual utility classes, glassmorphic UI elements, and dynamic loops.
- **Controller Layer (Spring Web)**: RESTful APIs and traditional Web MVC routing controls handling requests, user session binding, and OAuth flows.
- **Service Layer (Spring Component)**: Business logic, order calculations, and verification handlers.
- **Data Access Layer (Spring Data JPA / Hibernate)**: Object-Relational Mapping (ORM) translating Java objects directly to database tables.
- **Security Filter Chain (Spring Security)**: Built-in filters handling OAuth2, CORS policy setup, endpoint authorizations, and resource control.

### Layer Diagram
```text
[ Browser ] ──▶ [ Controller / MVC ] ──▶ [ Services ] ──▶ [ JPA Repositories ] ──▶ [ Database ]
                      │                                          │
                      ▼                                          ▼
            [ Thymeleaf Templates ]                      [ H2 / PostgreSQL ]
```

---

## 🖥️ Application Preview

### 🏠 Home Page
<p align="center">
  <img src="./screenshot/home.jpeg" width="800" alt="Home Page">
</p>

### 🍛 Interactive Menu Grid
<p align="center">
  <img src="./screenshot/products.png" width="800" alt="Menu Page">
</p>

### 📖 Story & About Page
<p align="center">
  <img src="./screenshot/about.png" width="800" alt="Story Page">
</p>

### 🔑 Authentication Systems
<p align="center">
  <img src="./screenshot/login.png" width="45%" alt="Sign In page">
  <img src="./screenshot/register.png" width="45%" alt="Register page">
</p>

### 👤 Customer & Admin Dashboards
<p align="center">
  <img src="./screenshot/userLogin.png" width="45%" alt="Customer Dashboard">
  <img src="./screenshot/admin-services.jpeg" width="45%" alt="Admin Control Console">
</p>

### 🛠️ Swagger API Documentation
<p align="center">
  <img src="./screenshot/swagger-ui-index-html.png" width="800" alt="Swagger API documentation UI">
</p>

---

## 📋 Prerequisites

Before setting up or deploying, verify you have the following installed locally:
- **Java Development Kit (JDK) 21**
- *Note: Maven is not required to be installed manually, as the Maven Wrapper (`mvnw` / `mvnw.cmd`) is preloaded in the project.*

---

## ⚙️ Environment Variables Registry

In production, avoid hardcoding values. Use this table to configure environment variables for deployment on Render, Docker, or your preferred cloud host:

| Variable Name | Purpose | Example Value / Default |
| :--- | :--- | :--- |
| `JAVA_HOME` | Points to Java installation directory | `C:\Program Files\Java\jdk-21` |
| `PORT` | Web port Spring Boot listens on | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC database connection string | `jdbc:postgresql://db:5432/foodfiesta` |
| `SPRING_DATASOURCE_USERNAME` | Database username credentials | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password credentials | `your_password` |
| `GOOGLE_CLIENT_ID` | Google OAuth Web App Client ID | `your_client_id.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Web App Client Secret | `GOCSPX-your_secret` |

---

## 🚀 Local Quick-Start (H2 Database)

### 1. Clone & Navigate
```bash
git clone https://github.com/imrajeevnayan/Food-Fiesta.git
cd Food-Fiesta
```

### 2. Configure Environment `.env`
Create a file named `.env` in the root directory (this is automatically ignored by Git) to store your local credentials:
```env
GOOGLE_CLIENT_ID=your_id_here.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_secret_here
```

### 3. Run Locally

*   **Windows (PowerShell)**:
    ```powershell
    Get-Content .env | ForEach-Object { $name, $value = $_.Split('=', 2); if ($name -and $value) { [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim()) } }; .\mvnw.cmd spring-boot:run
    ```
*   **macOS / Linux**:
    ```bash
    export $(cat .env | xargs) && ./mvnw spring-boot:run
    ```

### 4. Port Access
- **Frontend App**: [http://localhost:8080/](http://localhost:8080/)
- **Swagger Docs**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 DB Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:foodfiesta`, User: `sa`, Pass: *blank*)

---

## 🗄️ Database Configurations (H2 vs. PostgreSQL)

### Option A: Local Dev / Quick Demo (H2 In-Memory)
By default, the application runs on H2. It auto-seeds default administrators and catalog entries on startup:
*   **Admin Email**: `admin@foodfiesta.com`
*   **Admin Password**: `admin123`

*Note: In-memory data resets every time the application stops or sleeps.*

### Option B: Production Setup (PostgreSQL)
To run a persistent database locally or in production, configure the environment variables or update `src/main/resources/application.properties` with:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/foodfiesta
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

---

## ☁️ Deploying on Render (Cloud Deployment)

Render parses the project's `Dockerfile` to compile and containerize the Spring Boot application.

### Setup Guide A: H2 Database (Quick Demo)
1. Go to your **Render Dashboard**, click **New > Web Service**.
2. Link your `Food-Fiesta` GitHub repository.
3. Select **Docker** as the Runtime environment.
4. Go to **Advanced** settings, add the following environment variables:
   - **`PORT`**: `8080`
   - **`GOOGLE_CLIENT_ID`**: `[Your Client ID]`
   - **`GOOGLE_CLIENT_SECRET`**: `[Your Client Secret]`
5. Click **Create Web Service**.

### Setup Guide B: PostgreSQL (Production Persistence)
1. Go to Render, click **New > PostgreSQL** to create a persistent database. Copy the **Internal Database URL**.
2. Create a new **Web Service**, link your repository, and select **Docker** as the Runtime.
3. Under **Advanced**, add the environment variables:
   - **`PORT`**: `8080`
   - **`GOOGLE_CLIENT_ID`**: `[Your Client ID]`
   - **`GOOGLE_CLIENT_SECRET`**: `[Your Client Secret]`
   - **`SPRING_DATASOURCE_URL`**: `jdbc:postgresql://<HOST>:<PORT>/foodfiesta` *(parsed from your Internal Database URL)*
   - **`SPRING_DATASOURCE_USERNAME`**: `postgres`
   - **`SPRING_DATASOURCE_PASSWORD`**: `[Your DB Password]`
4. Click **Create Web Service**.

---

## 🐳 Docker Deployment (Local Containerized)

1. Build the production-ready Docker image:
   ```bash
   docker build -t food-fiesta .
   ```
2. Run the container locally:
   ```bash
   docker run -p 8080:8080 --env-file .env food-fiesta
   ```

---

## 📄 License
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

*Developed by **imrajeevnayan***
