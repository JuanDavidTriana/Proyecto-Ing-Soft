# Biblioteca API (Spring Boot)

CRUD básico de usuarios y libros. Sin roles ni JWT: las contraseñas se guardan
encriptadas (BCrypt) en la base de datos.

## Requisitos
- Java 17+
- Maven
- PostgreSQL corriendo localmente

## Configuración

Crear la base de datos:

```sql
CREATE DATABASE biblioteca;
```

Ajustar usuario/contraseña de Postgres en `src/main/resources/application.properties`
si no son `postgres` / `postgres`.

## Ejecutar

```bash
mvn spring-boot:run
```

La app corre en http://localhost:8080

## Endpoints

- `POST   /usuarios`             crear usuario
- `GET    /usuarios?page=0&size=10`  listar (paginado)
- `GET    /usuarios/{id}`        obtener uno
- `PUT    /usuarios/{id}`        actualizar
- `DELETE /usuarios/{id}`        eliminar

- `POST   /libros`
- `GET    /libros?page=0&size=10`
- `GET    /libros/{id}`
- `PUT    /libros/{id}`
- `DELETE /libros/{id}`
