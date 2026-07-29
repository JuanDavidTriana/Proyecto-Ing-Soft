from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from .. import crud, models, schemas
from ..database import get_db
from ..security import requerir_rol

router = APIRouter(prefix="/usuarios", tags=["usuarios"])

# Todas las rutas de este router quedan protegidas por rol:
# - Depends(requerir_rol(models.ROL_ADMIN)) -> exige token válido Y rol ADMIN.
#   Si no hay token: 401. Si hay token pero el rol no es ADMIN: 403.
# Para crear tu propia cuenta sin ser ADMIN, usa /auth/registro (sin rol).


@router.post(
    "/",
    response_model=schemas.UsuarioOut,
    status_code=201,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def crear_usuario(usuario: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    """Solo un ADMIN puede crear usuarios por esta vía (por ejemplo, para
    dar de alta a otro empleado). El registro público está en /auth/registro
    y siempre asigna rol USER."""
    if crud.get_usuario_by_username(db, usuario.username):
        raise HTTPException(status_code=400, detail="El username ya existe")
    return crud.create_usuario(db, usuario)


@router.get(
    "/",
    response_model=schemas.UsuarioPage,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN, models.ROL_USER))],
)
def listar_usuarios(
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """Cualquier usuario autenticado (ADMIN o USER) puede listar."""
    total, items = crud.get_usuarios(db, skip=skip, limit=limit)
    return schemas.UsuarioPage(total=total, skip=skip, limit=limit, items=items)


@router.get(
    "/{usuario_id}",
    response_model=schemas.UsuarioOut,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN, models.ROL_USER))],
)
def obtener_usuario(usuario_id: int, db: Session = Depends(get_db)):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return db_usuario


@router.put(
    "/{usuario_id}",
    response_model=schemas.UsuarioOut,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def actualizar_usuario(
    usuario_id: int, cambios: schemas.UsuarioUpdate, db: Session = Depends(get_db)
):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return crud.update_usuario(db, db_usuario, cambios)


@router.put(
    "/{usuario_id}/rol",
    response_model=schemas.UsuarioOut,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def cambiar_rol(usuario_id: int, cambios: schemas.RolUpdate, db: Session = Depends(get_db)):
    """Endpoint dedicado a ascender/degradar usuarios. Separado de
    actualizar_usuario para que el cambio de rol sea explícito y fácil
    de auditar/loggear en un proyecto real."""
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    db_usuario.rol = cambios.rol
    db.commit()
    db.refresh(db_usuario)
    return db_usuario


@router.delete(
    "/{usuario_id}",
    status_code=204,
    dependencies=[Depends(requerir_rol(models.ROL_ADMIN))],
)
def eliminar_usuario(usuario_id: int, db: Session = Depends(get_db)):
    db_usuario = crud.get_usuario(db, usuario_id)
    if not db_usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    crud.delete_usuario(db, db_usuario)
