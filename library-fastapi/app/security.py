# =============================================================================
# security.py - Autenticación con JWT y autorización por roles
# =============================================================================
# Este módulo concentra TODO lo relacionado a "quién eres" (autenticación)
# y "qué puedes hacer" (autorización/roles).
#
# Flujo general de JWT (JSON Web Token):
#   1. El usuario manda username + password a /auth/login.
#   2. Si son correctos, el servidor genera un TOKEN firmado con una clave
#      secreta (JWT_SECRET_KEY) que contiene el username y una fecha de
#      expiración. El servidor NO guarda sesiones en memoria ni en BD.
#   3. El cliente guarda ese token y lo manda en cada request futura en el
#      header: "Authorization: Bearer <token>".
#   4. El servidor, en cada request protegida, verifica la FIRMA del token
#      (no necesita ir a la base de datos para eso) y así sabe que el
#      token es válido y no fue alterado, y extrae el username de adentro.
#   5. Con el username, el servidor sí consulta la BD para saber el rol
#      actual del usuario y decidir si puede o no hacer la acción pedida.
# =============================================================================

import os
from datetime import datetime, timedelta, timezone

from dotenv import load_dotenv
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from sqlalchemy.orm import Session

from . import crud, models
from .database import get_db

load_dotenv()

# Configuración leída de variables de entorno (ver .env.example).
JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "clave-de-desarrollo-no-usar-en-produccion")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
JWT_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "60"))

# OAuth2PasswordBearer le dice a FastAPI/Swagger dónde está el endpoint de
# login (tokenUrl). Además, se encarga de leer automáticamente el header
# "Authorization: Bearer <token>" de cada request y extraer el token.
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")


def crear_access_token(username: str) -> str:
    """Genera un JWT firmado que contiene el username ('sub') y una
    fecha de expiración ('exp'). 'sub' (subject) es el campo estándar
    de JWT para identificar de quién es el token."""
    expira = datetime.now(timezone.utc) + timedelta(minutes=JWT_EXPIRE_MINUTES)
    payload = {"sub": username, "exp": expira}
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


def obtener_usuario_actual(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> models.Usuario:
    """Dependency que se usa en cada endpoint protegido. FastAPI la ejecuta
    ANTES de entrar a la función del endpoint. Si el token es inválido o el
    usuario no existe, corta la petición con 401 antes de llegar al código
    de negocio."""
    credenciales_invalidas = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="No se pudo validar el token",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # jwt.decode verifica la firma (con JWT_SECRET_KEY) y la expiración.
        # Si el token fue manipulado o expiró, lanza JWTError.
        payload = jwt.decode(token, JWT_SECRET_KEY, algorithms=[JWT_ALGORITHM])
        username: str | None = payload.get("sub")
        if username is None:
            raise credenciales_invalidas
    except JWTError:
        raise credenciales_invalidas

    usuario = crud.get_usuario_by_username(db, username)
    if usuario is None:
        raise credenciales_invalidas
    return usuario


def requerir_rol(*roles_permitidos: str):
    """Fábrica de dependencies para autorización por rol.

    Uso: Depends(requerir_rol("ADMIN")) sobre un endpoint. Primero exige
    un token válido (reutiliza obtener_usuario_actual) y luego verifica
    que el rol del usuario esté en la lista de roles permitidos. Si no,
    responde 403 (autenticado, pero sin permiso)."""

    def dependency(usuario: models.Usuario = Depends(obtener_usuario_actual)) -> models.Usuario:
        if usuario.rol not in roles_permitidos:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="No tienes permisos para realizar esta acción",
            )
        return usuario

    return dependency
