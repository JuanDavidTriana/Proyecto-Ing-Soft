# Biblioteca API (FastAPI)

CRUD de usuarios y libros con autenticación JWT y roles (ADMIN / USER).
Las contraseñas se guardan encriptadas (bcrypt) en la base de datos.

## Requisitos
- Python 3.11+
- PostgreSQL corriendo localmente

## Configuración

```bash
python -m venv venv
source venv/bin/activate   # en Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env       # ajustar DATABASE_URL y JWT_SECRET_KEY si es necesario
```

Crear la base de datos en Postgres:

```sql
CREATE DATABASE biblioteca;
```

## Ejecutar

```bash
uvicorn app.main:app --reload
```

Docs interactivas: http://localhost:8000/docs

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
- `POST   /auth/login`      login (form: username, password) -> devuelve JWT

**Usuarios** (requiere token; ADMIN salvo que se indique)
- `POST   /usuarios/`             crear usuario (ADMIN)
- `GET    /usuarios/?skip=0&limit=10`  listar (ADMIN o USER)
- `GET    /usuarios/{id}`         obtener uno (ADMIN o USER)
- `PUT    /usuarios/{id}`         actualizar (ADMIN)
- `PUT    /usuarios/{id}/rol`     cambiar rol (ADMIN)
- `DELETE /usuarios/{id}`         eliminar (ADMIN)

**Libros** (requiere token; ADMIN salvo que se indique)
- `POST   /libros/`             crear (ADMIN)
- `GET    /libros/?skip=0&limit=10`  listar (ADMIN o USER)
- `GET    /libros/{id}`         obtener uno (ADMIN o USER)
- `PUT    /libros/{id}`         actualizar (ADMIN)
- `DELETE /libros/{id}`         eliminar (ADMIN)
