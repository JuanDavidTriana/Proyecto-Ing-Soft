from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from .. import crud, models, schemas
from ..database import get_db
from ..security import crear_access_token

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/registro", response_model=schemas.UsuarioOut, status_code=201)
def registro(datos: schemas.RegistroRequest, db: Session = Depends(get_db)):
    """Endpoint PÚBLICO (no requiere token). Cualquiera puede crear su
    cuenta, pero siempre queda con rol USER: el rol NO viene del cliente,
    se fija en el servidor con models.ROL_USER. Así evitamos que alguien
    mande {"rol": "ADMIN"} y se autoasigne permisos."""
    if crud.get_usuario_by_username(db, datos.username):
        raise HTTPException(status_code=400, detail="El username ya existe")

    usuario_create = schemas.UsuarioCreate(username=datos.username, password=datos.password)
    return crud.create_usuario(db, usuario_create, rol=models.ROL_USER)


@router.post("/login", response_model=schemas.TokenOut)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """Endpoint PÚBLICO. Recibe username/password como formulario
    (estándar OAuth2, lo que permite probarlo directo desde /docs con el
    botón "Authorize"). Si son correctos, devuelve un JWT firmado.

    OJO: comparamos la contraseña con verificar_password (bcrypt), NUNCA
    comparando strings directamente, porque en la BD solo existe el hash."""
    usuario = crud.get_usuario_by_username(db, form_data.username)
    credenciales_invalidas = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Username o password incorrectos",
        headers={"WWW-Authenticate": "Bearer"},
    )
    if not usuario or not crud.verificar_password(form_data.password, usuario.password_hash):
        raise credenciales_invalidas

    token = crear_access_token(usuario.username)
    return schemas.TokenOut(access_token=token)
