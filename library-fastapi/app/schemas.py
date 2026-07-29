import uuid
from typing import List

from pydantic import BaseModel, Field, ConfigDict


# ---------- Usuario ----------

class UsuarioCreate(BaseModel):
    username: str = Field(min_length=3, max_length=50)
    password: str = Field(min_length=4, max_length=100)


class UsuarioUpdate(BaseModel):
    username: str | None = Field(default=None, min_length=3, max_length=50)
    password: str | None = Field(default=None, min_length=4, max_length=100)


class UsuarioOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    username: str
    rol: str
    # Nunca se expone el password_hash en las respuestas


class UsuarioPage(BaseModel):
    total: int
    skip: int
    limit: int
    # Metadata calculada por app/pagination.py: le ahorra al cliente
    # tener que hacer estas cuentas él mismo (total//limit, etc.).
    total_paginas: int
    pagina_actual: int
    hay_siguiente: bool
    hay_anterior: bool
    items: List[UsuarioOut]


# ---------- Auth (JWT) ----------

class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"


class RegistroRequest(BaseModel):
    """Registro público: cualquiera puede crear su cuenta, pero SIEMPRE
    queda con rol USER. El rol no se recibe del cliente para evitar que
    alguien se auto-asigne ADMIN."""

    username: str = Field(min_length=3, max_length=50)
    password: str = Field(min_length=4, max_length=100)


class RolUpdate(BaseModel):
    """Solo un ADMIN puede usar esto (ver /usuarios/{id}/rol) para
    ascender o degradar a otro usuario."""

    rol: str = Field(pattern="^(ADMIN|USER)$")


# ---------- Libro ----------

class LibroBase(BaseModel):
    titulo: str = Field(min_length=1, max_length=200)
    autor: str = Field(min_length=1, max_length=150)
    isbn: str = Field(min_length=1, max_length=20)
    cantidad: int = Field(ge=0, default=0)


class LibroCreate(LibroBase):
    pass


class LibroUpdate(BaseModel):
    titulo: str | None = Field(default=None, min_length=1, max_length=200)
    autor: str | None = Field(default=None, min_length=1, max_length=150)
    isbn: str | None = Field(default=None, min_length=1, max_length=20)
    cantidad: int | None = Field(default=None, ge=0)


class LibroOut(LibroBase):
    model_config = ConfigDict(from_attributes=True)

    # Nuevo: id UUID en vez de int. Pydantic serializa uuid.UUID como el
    # string estándar con guiones, ej: "3fa85f64-5717-4562-b3fc-2c963f66afa6".
    id: uuid.UUID


class LibroPage(BaseModel):
    total: int
    skip: int
    limit: int
    total_paginas: int
    pagina_actual: int
    hay_siguiente: bool
    hay_anterior: bool
    items: List[LibroOut]
