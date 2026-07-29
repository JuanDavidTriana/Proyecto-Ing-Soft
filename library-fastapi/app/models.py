from sqlalchemy import Column, Integer, String

from .database import Base


class Usuario(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)


class Libro(Base):
    __tablename__ = "libros"

    id = Column(Integer, primary_key=True, index=True)
    titulo = Column(String(200), nullable=False)
    autor = Column(String(150), nullable=False)
    isbn = Column(String(20), unique=True, index=True, nullable=False)
    cantidad = Column(Integer, nullable=False, default=0)
