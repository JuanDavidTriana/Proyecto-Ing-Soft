# Biblioteca API (FastAPI)

CRUD básico de usuarios y libros. Sin roles ni JWT: las contraseñas se guardan
encriptadas (bcrypt) en la base de datos.

## Requisitos
- Python 3.11+
- PostgreSQL corriendo localmente

## Configuración

```bash
python -m venv venv
source venv/bin/activate   # en Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env       # ajustar DATABASE_URL si es necesario
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

## Endpoints

- `POST   /usuarios/`        crear usuario
- `GET    /usuarios/?skip=0&limit=10`  listar (paginado)
- `GET    /usuarios/{id}`    obtener uno
- `PUT    /usuarios/{id}`    actualizar
- `DELETE /usuarios/{id}`    eliminar

- `POST   /libros/`
- `GET    /libros/?skip=0&limit=10`
- `GET    /libros/{id}`
- `PUT    /libros/{id}`
- `DELETE /libros/{id}`
