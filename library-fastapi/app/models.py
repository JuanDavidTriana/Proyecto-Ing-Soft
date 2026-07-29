import uuid

from sqlalchemy import Column, Integer, String
from sqlalchemy.dialects.postgresql import UUID

from .database import Base

# Roles disponibles. Se guardan como texto simple en la base de datos
# ("ADMIN" / "USER"); no usamos un ENUM nativo de Postgres para mantenerlo
# simple y fácil de migrar/entender en clase.
ROL_ADMIN = "ADMIN"
ROL_USER = "USER"


class Usuario(Base):
    __tablename__ = "usuarios"

    # Usuario mantiene id entero autoincremental a propósito: es un id
    # "interno", que casi nunca viaja en URLs públicas ni se comparte
    # fuera del sistema. Ver PAGINACION_UUID.md para la discusión de
    # cuándo conviene UUID y cuándo no hace falta.
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)

    # Nuevo: rol del usuario. Por defecto todo usuario nuevo es "USER".
    # El primer ADMIN se crea manualmente en la base de datos (ver AUTH.md).
    rol = Column(String(20), nullable=False, default=ROL_USER, server_default=ROL_USER)


class Libro(Base):
    __tablename__ = "libros"

    # Nuevo: id de tipo UUID en vez de entero autoincremental.
    # - as_uuid=True: SQLAlchemy nos entrega/recibe objetos uuid.UUID de
    #   Python (no strings), y Postgres lo guarda en su tipo nativo UUID
    #   (16 bytes, no como texto).
    # - default=uuid.uuid4: si no se manda un id explícito, se genera
    #   uno nuevo en el momento del INSERT, en la aplicación (no en la BD).
    # Ver PAGINACION_UUID.md para la explicación completa de por qué el
    # catálogo de libros usa UUID (ids expuestos en la URL pública,
    # posible sincronización entre sistemas) y por qué Usuario no.
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    titulo = Column(String(200), nullable=False)
    autor = Column(String(150), nullable=False)
    isbn = Column(String(20), unique=True, index=True, nullable=False)
    cantidad = Column(Integer, nullable=False, default=0)
