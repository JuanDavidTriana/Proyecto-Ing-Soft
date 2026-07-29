# Biblioteca API (Spring Boot)

CRUD de usuarios y libros con autenticación JWT y roles (ADMIN / USER).
Las contraseñas se guardan encriptadas (BCrypt) en la base de datos.

## Requisitos
- Java 17+
- Maven
- PostgreSQL corriendo localmente

## Configuración

Crear la base de datos:

```sql
CREATE DATABASE biblioteca;
```

Ajustar usuario/contraseña de Postgres y `jwt.secret` en
`src/main/resources/application.properties` si es necesario.

## Ejecutar

```bash
mvn spring-boot:run
```

La app corre en http://localhost:8080

## Ejecutar con Docker

```bash
docker compose up --build
```

Ver la guía completa (conceptos, comandos, troubleshooting) en
[`../DOCKER.md`](../DOCKER.md).

## Autenticación y roles

Todos los endpoints de `/usuarios` y `/libros` requieren un JWT en el
header `Authorization: Bearer <token>`, excepto `/auth/registro` y
`/auth/login`. Ver la explicación completa (cómo funciona el flujo, cómo
crear el primer ADMIN, ejemplos con curl) en [`../AUTH.md`](../AUTH.md).

## Endpoints

**Auth (públicos)**
- `POST   /auth/registro`   crear cuenta propia (siempre queda como USER)
- `POST   /auth/login`      login (JSON: username, password) -> devuelve JWT

**Usuarios** (requiere token; ADMIN salvo que se indique)
- `POST   /usuarios`             crear usuario (ADMIN)
- `GET    /usuarios?page=0&size=10`  listar (ADMIN o USER)
- `GET    /usuarios/{id}`        obtener uno (ADMIN o USER)
- `PUT    /usuarios/{id}`        actualizar (ADMIN)
- `PUT    /usuarios/{id}/rol`    cambiar rol (ADMIN)
- `DELETE /usuarios/{id}`        eliminar (ADMIN)

**Libros** (requiere token; ADMIN salvo que se indique)
- `POST   /libros`             crear (ADMIN)
- `GET    /libros?page=0&size=10`  listar (ADMIN o USER)
- `GET    /libros/{id}`        obtener uno (ADMIN o USER)
- `PUT    /libros/{id}`        actualizar (ADMIN)
- `DELETE /libros/{id}`        eliminar (ADMIN)
