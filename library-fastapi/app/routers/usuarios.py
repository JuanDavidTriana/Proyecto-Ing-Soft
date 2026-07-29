from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from .. import crud, schemas
from ..database import get_db

router = APIRouter(prefix="/usuarios", tags=["usuarios"])


@router.post("/", response_model=schemas.UsuarioOut, status_code=201)
def crear_usuario(usuario: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    if crud.get_usuario_by_username(db, usuario.username):
        raise HTTPException(status_code=400, detail="El username ya existe")
    return crud.create_usuario(db, usuario)


@router.get("/", response_model=schemas.UsuarioPage)
def listar_usuarios(
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    total, items = crud.get_usuarios(db, skip=skip, limit=limit)
    return schemas.UsuarioPage(total=total, skip=skip, limit=limit, items=items)


@router.get("/{usuario_id}", response_model=schemas.UsuarioOut)
def obtener_usuario(usuario_id: int, db: Session = Depends(get_db)):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return db_usuario


@router.put("/{usuario_id}", response_model=schemas.UsuarioOut)
def actualizar_usuario(
    usuario_id: int, cambios: schemas.UsuarioUpdate, db: Session = Depends(get_db)
):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return crud.update_usuario(db, db_usuario, cambios)


@router.delete("/{usuario_id}", status_code=204)
def eliminar_usuario(usuario_id: int, db: Session = Depends(get_db)):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    crud.delete_usuario(db, db_usuario)
