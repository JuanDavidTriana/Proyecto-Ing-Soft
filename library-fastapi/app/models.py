from sqlalchemy import Column, Integer, String

from .database import Base

# Roles disponibles. Se guardan como texto simple en la base de datos
# ("ADMIN" / "USER"); no usamos un ENUM nativo de Postgres para mantenerlo
# simple y fácil de migrar/entender en clase.
ROL_ADMIN = "ADMIN"
ROL_USER = "USER"


class Usuario(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)

    # Nuevo: rol del usuario. Por defecto todo usuario nuevo es "USER".
    # El primer ADMIN se crea manualmente en la base de datos (ver AUTH.md).
    rol = Column(String(20), nullable=False, default=ROL_USER, server_default=ROL_USER)


class Libro(Base):
    __tablename__ = "libros"

    id = Column(Integer, primary_key=True, index=True)
    titulo = Column(String(200), nullable=False)
    autor = Column(String(150), nullable=False)
    isbn = Column(String(20), unique=True, index=True, nullable=False)
    cantidad = Column(Integer, nullable=False, default=0)
