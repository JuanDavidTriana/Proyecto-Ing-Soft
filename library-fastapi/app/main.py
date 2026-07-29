from fastapi import FastAPI

from . import models
from .database import engine
from .routers import auth, libros, usuarios

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Biblioteca API", version="1.0.0")

app.include_router(auth.router)
app.include_router(usuarios.router)
app.include_router(libros.router)


@app.get("/")
def root():
    return {"mensaje": "API de Biblioteca funcionando"}
