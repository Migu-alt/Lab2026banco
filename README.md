# Lab12026p — Banco

[![CI/CD Pipeline](https://github.com/Migu-alt/lab12026p/actions/workflows/build.yml/badge.svg)](https://github.com/Migu-alt/lab12026p/actions/workflows/build.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Migu-alt_Lab2026banco&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Migu-alt_Lab2026banco)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Migu-alt_Lab2026banco&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Migu-alt_Lab2026banco)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Migu-alt_Lab2026banco&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Migu-alt_Lab2026banco)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Migu-alt_Lab2026banco&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Migu-alt_Lab2026banco)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Migu-alt_Lab2026banco&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Migu-alt_Lab2026banco)
[![Known Vulnerabilities](https://snyk.io/test/github/Migu-alt/lab12026p/badge.svg)](https://snyk.io/test/github/Migu-alt/lab12026p)

Aplicación bancaria sencilla compuesta por:

- **Backend**: Spring Boot 3.5.11 + Java 17 + Spring Data JPA + MySQL (paquete `com.udea.lab12026p`).
- **Frontend**: React + Vite, ubicado en `frontend/`.

## 1. Requisitos

- Java 17
- Maven (o usa el wrapper `mvnw` / `mvnw.cmd` incluido, no necesitas tener Maven instalado)
- MySQL 8
- Node.js 18+ y npm

## 2. Configuración de la base de datos

El backend espera una base de datos MySQL local ya creada:

- Host: `localhost`
- Puerto: `3306`
- Base de datos: `lab12026p`
- Usuario: `root`
- Password: se proporciona mediante la variable de entorno `DB_PASSWORD`; no se guarda en el repositorio.

Define `DB_PASSWORD` con la contraseña de tu MySQL local antes de iniciar el backend. En PowerShell:

```powershell
$env:DB_PASSWORD = "<tu-clave-local>"
```

Crea la base de datos si no existe (no se incluyen scripts destructivos):

```sql
CREATE DATABASE IF NOT EXISTS lab12026p;
```

Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea/actualiza las tablas automáticamente al arrancar.

## 3. Cómo iniciar el backend

Desde la raíz del proyecto:

**Windows**
```
mvnw.cmd spring-boot:run
```

**Linux / macOS**
```
./mvnw spring-boot:run
```

El backend queda disponible en `http://localhost:8080`.

## 4. Cómo iniciar el frontend

```
cd frontend
npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173` y consume el backend en `http://localhost:8080` (habilitado vía CORS, ver `config/CorsConfig.java`).

Para generar el build de producción:

```
npm run build
```

## 5. Vistas del frontend

1. **Clientes** — consulta `GET /api/customers` y muestra el listado con ID, nombre, apellido, número de cuenta y saldo.
2. **Transferencia** — formulario que envía `POST /api/transactions` con cuenta origen, cuenta destino y monto; valida campos vacíos y montos positivos, y muestra mensajes de éxito o error.
3. **Histórico** — permite elegir un cliente (o escribir un número de cuenta) y consulta `GET /api/transactions/{accountNumber}` para listar sus transacciones.

## 6. Endpoints del backend

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/customers` | Lista todos los clientes |
| GET | `/api/customers/{id}` | Obtiene un cliente por ID |
| POST | `/api/customers` | Crea un cliente |
| POST | `/api/transactions` | Realiza una transferencia entre dos cuentas |
| GET | `/api/transactions/{accountNumber}` | Lista las transacciones donde la cuenta aparece como remitente o receptora |
| PUT | `/api/transactions/{id}` | Actualiza una transacción existente, revirtiendo el efecto anterior sobre los saldos y aplicando el nuevo |
| DELETE | `/api/transactions/{id}` | Elimina una transacción, revirtiendo su efecto sobre los saldos |

## 7. Notas sobre PUT y DELETE

Una transacción representa dinero que ya se movió entre dos cuentas. Por eso:

- **PUT** primero revierte el efecto de la transacción original (suma de vuelta al remitente, resta al receptor) y luego aplica el nuevo efecto con los valores recibidos, todo dentro de una transacción atómica (`@Transactional`). Si alguna cuenta no existe o el saldo resultante sería inválido, no se modifica nada y se responde `400`. Si el `id` no existe, responde `404`.
- **DELETE** revierte el efecto de la transacción antes de eliminarla, también de forma atómica. `404` si no existe.

## 8. Datos de prueba

El proyecto no inserta datos ficticios automáticamente para no alterar tu base de datos existente. Si necesitas clientes de prueba, créalos con `POST /api/customers`, por ejemplo:

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ana","lastName":"Gómez","accountNumber":"111111111","balance":1000.0}'
```

## 9. Pruebas y cobertura

**Windows**
```
mvnw.cmd clean verify
```

**Linux / macOS**
```
./mvnw clean verify
```

Las pruebas usan H2 en memoria (`src/test/resources/application.properties`), por lo que **no necesitan MySQL**. JaCoCo genera el reporte en `target/site/jacoco/index.html` y `target/site/jacoco/jacoco.xml` (este último lo consume SonarCloud).

## 10. Pipeline CI/CD (GitHub Actions)

Archivo: `.github/workflows/build.yml`

| Job | Qué hace | Cuándo corre |
|---|---|---|
| `tests` | `mvn clean verify` (JUnit 5 + JaCoCo) | push a `main`/`feature/**`/`fix/**`/`mi-rama`, PR a `main` o ejecución manual |
| `sonar` | Análisis de calidad en SonarCloud (con cobertura) | después de `tests` |
| `build` | Genera `lab12026p.jar` y lo sube como artefacto | después de `sonar` |
| `docker` | Construye la imagen y la publica en Docker Hub (`latest` y SHA del commit) | solo push a `main` |
| `deploy` | Dispara el despliegue en Render y hace un smoke test a `/api/customers` | solo push a `main` |

**Estrategia trunk-based:** `main` es el tronco y siempre debe estar desplegable. Los cambios entran con ramas cortas (`feature/...`) y Pull Request hacia `main`; el PR ejecuta tests + Sonar + build, y al hacer merge se construye la imagen y se despliega.

### Secrets requeridos (Settings → Secrets and variables → Actions)

| Secret | Descripción |
|---|---|
| `SONAR_TOKEN` | Token de SonarCloud |
| `DOCKER_USERNAME` / `DOCKER_PASSWORD` | Credenciales (o access token) de Docker Hub |
| `RENDER_DEPLOY_HOOK_URL` | Deploy Hook del servicio en Render |
| `RENDER_APP_URL` | URL pública del servicio, ej. `https://lab12026p.onrender.com` |

### Variables de entorno del backend (en la nube)

| Variable | Ejemplo |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED&serverTimezone=UTC` |
| `DB_USERNAME` | usuario MySQL |
| `DB_PASSWORD` | contraseña MySQL |
| `PORT` | la asigna Render automáticamente |

### Ejecutar la imagen localmente

```
docker pull <DOCKER_USERNAME>/lab12026p:latest
docker run -p 8080:8080 -e DB_URL="jdbc:mysql://host.docker.internal:3306/lab12026p?useSSL=false&allowPublicKeyRetrieval=true" -e DB_USERNAME=root -e DB_PASSWORD <DOCKER_USERNAME>/lab12026p:latest
```

Define `DB_PASSWORD` en el entorno local antes de ejecutar `docker run`; la forma `-e DB_PASSWORD` reenvía esa variable al contenedor sin escribir la contraseña en el comando ni en el repositorio.
