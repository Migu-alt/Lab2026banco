# Lab12026p — Banco

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
- Password: `root`

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
