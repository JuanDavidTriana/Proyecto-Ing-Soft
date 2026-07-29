# Guía de Docker (primeros pasos) - Biblioteca API

Esta guía es una introducción a Docker pensada para quien lo usa por
primera vez, aplicada a los dos proyectos de este repo: `library-fastapi`
y `library-spring`. Ambos ya tienen su propio `Dockerfile` y
`docker-compose.yml`, comentados línea por línea.

## 1. Conceptos básicos

- **Imagen**: una plantilla de solo lectura con todo lo necesario para
  correr algo (sistema operativo mínimo, Python o Java, dependencias,
  nuestro código). Se construye una vez a partir de un `Dockerfile`.
- **Contenedor**: una instancia en ejecución de una imagen. Es como el
  "proceso vivo" que corre a partir de la plantilla. Puedes tener varios
  contenedores corriendo de la misma imagen.
- **Dockerfile**: el archivo con las instrucciones paso a paso para
  construir una imagen (qué base usar, qué instalar, qué copiar, qué
  comando correr al arrancar).
- **Volumen**: espacio en disco que vive fuera del contenedor y persiste
  aunque el contenedor se borre. Lo usamos para no perder los datos de
  Postgres cada vez que reconstruimos.
- **Red (network)**: Docker crea una red interna entre los contenedores
  de un mismo `docker-compose.yml`, para que puedan hablarse entre sí
  usando el nombre del servicio como si fuera un hostname (por ejemplo,
  la API se conecta a la base de datos usando `db`, no `localhost`).
- **Docker Compose**: herramienta para levantar varios contenedores
  relacionados (en nuestro caso: API + base de datos) con un solo
  comando, en vez de crear cada uno a mano.

## 2. Requisitos

Instalar [Docker Desktop](https://www.docker.com/products/docker-desktop/)
(incluye Docker Engine y Docker Compose). Verificar que funciona:

```bash
docker --version
docker compose version
```

## 3. Comandos básicos (los vas a usar todo el tiempo)

```bash
docker ps                # contenedores corriendo ahora mismo
docker ps -a              # todos los contenedores (incluidos los detenidos)
docker images             # imágenes que ya tienes descargadas/construidas
docker logs <nombre>       # ver los logs de un contenedor
docker logs -f <nombre>    # ver los logs "en vivo" (follow)
docker exec -it <nombre> sh   # abrir una terminal dentro de un contenedor
docker stop <nombre>       # detener un contenedor
docker rm <nombre>         # eliminar un contenedor detenido
docker rmi <imagen>        # eliminar una imagen
```

## 4. Levantar el proyecto FastAPI con Docker

Desde la carpeta `library-fastapi/`:

```bash
cd library-fastapi

# Construye las imágenes y levanta API + Postgres juntos, en primer plano
docker compose up --build

# Igual, pero en segundo plano (no bloquea la terminal)
docker compose up --build -d
```

Qué pasa al correr esto:
1. Compose construye la imagen de la API a partir del `Dockerfile`.
2. Descarga la imagen oficial de Postgres si no la tienes.
3. Crea una red interna y un volumen para los datos de Postgres.
4. Levanta primero `db`, espera a que el healthcheck confirme que ya
   acepta conexiones, y luego levanta `api`.

Probar que funciona: abrir `http://localhost:8000/docs` en el navegador
(documentación interactiva de FastAPI).

Detener todo:

```bash
docker compose down          # detiene y borra los contenedores (los datos persisten en el volumen)
docker compose down -v       # además borra el volumen (pierdes los datos de la BD)
```

## 5. Levantar el proyecto Spring Boot con Docker

Desde la carpeta `library-spring/`:

```bash
cd library-spring

docker compose up --build
```

Qué pasa al correr esto (un poco más lento la primera vez):
1. Compose construye la imagen usando un **build de dos etapas**: una
   etapa temporal con Maven compila el proyecto y genera el `.jar`;
   la imagen final solo contiene el `.jar` y el Java Runtime (más
   liviana, sin código fuente ni Maven).
2. Levanta Postgres, espera el healthcheck, y luego levanta la API.

Probar que funciona: hacer un `GET` a `http://localhost:8080/libros`
(por ejemplo con curl, Postman o el navegador).

Detener todo:

```bash
docker compose down
docker compose down -v   # borra también el volumen con los datos
```

## 6. Comandos útiles para el día a día en clase

```bash
# Reconstruir solo un servicio después de cambiar código
docker compose up --build api

# Ver logs de un solo servicio
docker compose logs -f api
docker compose logs -f db

# Entrar a la base de datos desde dentro del contenedor
docker exec -it biblioteca_db psql -U postgres -d biblioteca

# Ver qué contenedores están corriendo de este proyecto
docker compose ps
```

## 7. Errores comunes al empezar

- **"port is already allocated"**: ya tienes algo corriendo en el puerto
  5432 u 8000/8080 (por ejemplo Postgres instalado localmente). Solución:
  detener ese proceso local o cambiar el puerto publicado en
  `docker-compose.yml` (ej. `"5433:5432"`).
- **La API no conecta a la base de datos al arrancar**: normalmente es
  un problema de orden de arranque. Por eso usamos `depends_on` con
  `condition: service_healthy`, para que la API espere a que Postgres
  esté realmente lista, no solo "iniciada".
- **Cambié el código y no se refleja**: hay que reconstruir la imagen
  (`docker compose up --build`), a menos que estés usando el volumen de
  desarrollo que monta el código local (ya incluido en el compose de
  FastAPI).
