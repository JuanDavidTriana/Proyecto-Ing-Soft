from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from .. import crud, models, schemas
from ..database import get_db
from ..security import requerir_rol

router = APIRouter(prefix="/libros", tags=["libros"])

# Regla de negocio para este router:
# - Leer catálogo (GET): cualquier usuario autenticado (ADMIN o USER).
# - Modificar catálogo (POST/PUT/DELETE): solo ADMIN.


@router.post(
    "/",
    response_model=schemas.LibroOut,
    status_code=201,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def crear_libro(libro: schemas.LibroCreate, db: Session = Depends(get_db)):
    if crud.get_libro_by_isbn(db, libro.isbn):
        raise HTTPException(status_code=400, detail="El ISBN ya existe")
    return crud.create_libro(db, libro)


@router.get(
    "/",
    response_model=schemas.LibroPage,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN, models.ROL_USER))],
)
def listar_libros(
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    total, items = crud.get_libros(db, skip=skip, limit=limit)
    return schemas.LibroPage(total=total, skip=skip, limit=limit, items=items)


@router.get(
    "/{libro_id}",
    response_model=schemas.LibroOut,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN, models.ROL_USER))],
)
def obtener_libro(libro_id: int, db: Session = Depends(get_db)):
    db_libro = crud.get_libro(db, libro_id)
    if not db_libro:
        raise HTTPException(status_code=404, detail="Libro no encontrado")
    return db_libro


@router.put(
    "/{libro_id}",
    response_model=schemas.LibroOut,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def actualizar_libro(
    libro_id: int, cambios: schemas.LibroUpdate, db: Session = Depends(get_db)
):
    db_libro = crud.get_libro(db, libro_id)
    if not db_libro:
        raise HTTPException(status_code=404, detail="Libro no encontrado")
    return crud.update_libro(db, db_libro, cambios)


@router.delete(
    "/{libro_id}",
    status_code=204,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def eliminar_libro(libro_id: int, db: Session = Depends(get_db)):
    db_libro = crud.get_libro(db, libro_id)
    if not db_libro:
        raise HTTPException(status_code=404, detail="Libro no encontrado")
    crud.delete_libro(db, db_libro)
