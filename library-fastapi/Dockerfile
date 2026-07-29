# =============================================================================
# Dockerfile - Biblioteca API (FastAPI)
# =============================================================================
# Un Dockerfile es la "receta" que Docker sigue para construir una imagen:
# una plantilla inmutable con todo lo necesario para correr la app
# (sistema operativo base, Python, nuestras dependencias y nuestro código).
# Cada instrucción (FROM, RUN, COPY, etc.) crea una "capa" nueva encima
# de la anterior. Docker cachea esas capas, por eso el orden importa:
# lo que cambia menos (dependencias) va primero, lo que cambia más
# (nuestro código) va al final.
# =============================================================================

# FROM: elige la imagen base sobre la que construimos la nuestra.
# "python:3.11-slim" ya trae Python 3.11 instalado sobre una versión
# liviana de Debian (slim = menos peso que la imagen completa).
FROM python:3.11-slim

# WORKDIR: crea (si no existe) y se posiciona dentro de esa carpeta.
# Todas las instrucciones siguientes (COPY, RUN, CMD) se ejecutan
# relativas a este directorio dentro del contenedor.
WORKDIR /app

# ENV: define variables de entorno dentro del contenedor.
# PYTHONDONTWRITEBYTECODE evita que se generen archivos .pyc (basura).
# PYTHONUNBUFFERED hace que los print()/logs salgan en tiempo real
# en la consola de Docker en vez de quedar "bufferizados".
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# Copiamos SOLO el archivo de dependencias primero (no todo el código).
# ¿Por qué? Docker cachea cada capa: si el código cambia pero
# requirements.txt no, Docker reutiliza la capa de "pip install" ya
# construida en vez de reinstalar todo de nuevo. Esto hace los builds
# mucho más rápidos durante el desarrollo.
COPY requirements.txt .

# RUN: ejecuta un comando durante la construcción de la imagen (no en
# tiempo de ejecución del contenedor). Aquí instalamos las dependencias.
# --no-cache-dir evita que pip guarde una caché interna, lo que reduce
# el tamaño final de la imagen.
RUN pip install --no-cache-dir -r requirements.txt

# Ahora sí copiamos el resto del código de la aplicación al contenedor.
# Esta capa cambia seguido (cada vez que editamos código), por eso va
# después de instalar dependencias.
COPY . .

# EXPOSE: documenta qué puerto usa la aplicación dentro del contenedor.
# Es informativo; NO publica el puerto por sí solo (eso se hace con
# "-p" en docker run o con "ports:" en docker-compose.yml).
EXPOSE 8000

# CMD: comando por defecto que se ejecuta cuando arranca el contenedor
# (a diferencia de RUN, que se ejecuta solo al construir la imagen).
# Levantamos uvicorn escuchando en 0.0.0.0 para que sea accesible
# desde fuera del contenedor (127.0.0.1 solo sería visible por dentro).
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
