import uuid

from passlib.context import CryptContext
from sqlalchemy.orm import Session

from . import models, schemas

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


# ---------- Usuario ----------

def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def get_usuario(db: Session, usuario_id: int) -> models.Usuario | None:
    return db.query(models.Usuario).filter(models.Usuario.id == usuario_id).first()


def get_usuario_by_username(db: Session, username: str) -> models.Usuario | None:
    return db.query(models.Usuario).filter(models.Usuario.username == username).first()


def get_usuarios(db: Session, skip: int = 0, limit: int = 10):
    total = db.query(models.Usuario).count()
    items = (
        db.query(models.Usuario)
        .order_by(models.Usuario.id)
        .offset(skip)
        .limit(limit)
        .all()
    )
    return total, items


def create_usuario(
    db: Session, usuario: schemas.UsuarioCreate, rol: str = models.ROL_USER
) -> models.Usuario:
    db_usuario = models.Usuario(
        username=usuario.username,
        password_hash=hash_password(usuario.password),
        rol=rol,
    )
    db.add(db_usuario)
    db.commit()
    db.refresh(db_usuario)
    return db_usuario


def verificar_password(password_plano: str, password_hash: str) -> bool:
    return pwd_context.verify(password_plano, password_hash)


def update_usuario(
    db: Session, db_usuario: models.Usuario, cambios: schemas.UsuarioUpdate
) -> models.Usuario:
    if cambios.username is not None:
        db_usuario.username = cambios.username
    if cambios.password is not None:
        db_usuario.password_hash = hash_password(cambios.password)
    db.commit()
    db.refresh(db_usuario)
    return db_usuario


def delete_usuario(db: Session, db_usuario: models.Usuario) -> None:
    db.delete(db_usuario)
    db.commit()


# ---------- Libro ----------

def get_libro(db: Session, libro_id: uuid.UUID) -> models.Libro | None:
    return db.query(models.Libro).filter(models.Libro.id == libro_id).first()


def get_libro_by_isbn(db: Session, isbn: str) -> models.Libro | None:
    return db.query(models.Libro).filter(models.Libro.isbn == isbn).first()


def get_libros(db: Session, skip: int = 0, limit: int = 10):
    total = db.query(models.Libro).count()
    items = (
        db.query(models.Libro)
        .order_by(models.Libro.id)
        .offset(skip)
        .limit(limit)
        .all()
    )
    return total, items


def create_libro(db: Session, libro: schemas.LibroCreate) -> models.Libro:
    db_libro = models.Libro(**libro.model_dump())
    db.add(db_libro)
    db.commit()
    db.refresh(db_libro)
    return db_libro


def update_libro(
    db: Session, db_libro: models.Libro, cambios: schemas.LibroUpdate
) -> models.Libro:
    for campo, valor in cambios.model_dump(exclude_unset=True).items():
        setattr(db_libro, campo, valor)
    db.commit()
    db.refresh(db_libro)
    return db_libro


def delete_libro(db: Session, db_libro: models.Libro) -> None:
    db.delete(db_libro)
    db.commit()
