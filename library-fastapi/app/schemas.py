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
    # Nunca se expone el password_hash en las respuestas


class UsuarioPage(BaseModel):
    total: int
    skip: int
    limit: int
    items: List[UsuarioOut]


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

    id: int


class LibroPage(BaseModel):
    total: int
    skip: int
    limit: int
    items: List[LibroOut]
