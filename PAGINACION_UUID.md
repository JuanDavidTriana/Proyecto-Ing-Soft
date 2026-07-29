# Guía de Paginación y UUID - Biblioteca API

Esta guía explica dos cambios agregados sobre la rama de JWT/roles:
paginación con metadata completa, y el uso de UUID como identificador
del catálogo de libros.

## 1. Paginación: qué es y por qué se usa

Paginar significa devolver los resultados "de a pedazos" en vez de toda
la tabla de una vez. El cliente pide una porción con dos parámetros:

- **skip/offset**: cuántos registros saltarse desde el principio.
- **limit/size**: cuántos registros como máximo devolver en esa porción.

### Por qué deberías aplicarla

- **Rendimiento**: sin paginar, `GET /libros` en una tabla con 500.000
  filas trae TODO a la memoria del servidor y lo manda por la red en
  una sola respuesta. Con paginación, cada request solo trae, por
  ejemplo, 10 o 20 filas.
- **Experiencia de uso**: una lista de 500.000 elementos no es usable
  en una UI de todos modos; siempre se muestra "de a páginas".
  Paginar en el backend evita que el frontend tenga que descargar todo
  y filtrar en el navegador.
- **Protección de la base de datos**: sin límite, un solo cliente (o un
  bug, o un ataque) podría forzar una consulta gigantesca y tumbar el
  rendimiento para todos los demás usuarios.
- **Estándar de la industria**: prácticamente toda API pública (GitHub,
  Stripe, etc.) pagina sus listas por defecto.

### Qué se agregó en esta rama

Antes, las listas devolvían `total`, `skip`/`page`, `limit`/`size` e
`items`. Eso ya era "paginación", pero el cliente tenía que calcular él
mismo cosas como "¿hay una página siguiente?" o "¿en qué página estoy?".
Ahora:

- **FastAPI** (`app/pagination.py`): se agregó
  `calcular_metadata_paginacion(total, skip, limit)`, que devuelve
  `total_paginas`, `pagina_actual`, `hay_siguiente` y `hay_anterior`.
  Se usa en `GET /usuarios/` y `GET /libros/`.
- **Spring Boot**: no hizo falta escribir nada nuevo. Al devolver
  directamente un `Page<T>` de Spring Data, Jackson ya lo serializa con
  `totalPages`, `totalElements`, `first`, `last`, `numberOfElements`,
  etc. Es una de las diferencias de estilo entre los dos frameworks:
  FastAPI/Pydantic es explícito (tú defines cada campo de la respuesta),
  Spring Data trae bastante "gratis" con `Page<T>`.

Ejemplo de respuesta (FastAPI, `GET /libros/?skip=0&limit=2` con 5 libros):

```json
{
  "total": 5,
  "skip": 0,
  "limit": 2,
  "total_paginas": 3,
  "pagina_actual": 1,
  "hay_siguiente": true,
  "hay_anterior": false,
  "items": [ ... ]
}
```

## 2. UUID: qué es y por qué se usa (solo en Libro)

Un **UUID** (Universally Unique Identifier) es un identificador de 128
bits, generado con muy alta probabilidad de no repetirse nunca (ni
entre tablas, ni entre servidores, ni entre bases de datos distintas).
Se ve así: `3fa85f64-5717-4562-b3fc-2c963f66afa6`.

Se aplicó **solo a `Libro`**, no a `Usuario`, a propósito.

### Por qué Libro sí usa UUID

- **El id de un libro se expone públicamente**: aparece en la URL
  (`GET /libros/{id}`), y potencialmente en catálogos compartidos con
  otros sistemas (una librería, otra sucursal, una API externa).
- **No revela información de negocio**: con un id autoincremental
  (1, 2, 3...), cualquiera puede *adivinar* IDs válidos y deducir
  cuántos libros hay en total, o iterar `GET /libros/1`, `/2`, `/3`...
  para "raspar" todo el catálogo. Con UUID, no hay forma de adivinar el
  siguiente id ni de inferir cuántos registros existen.
- **Generación distribuida sin colisiones**: si en el futuro el
  catálogo se sincroniza entre varias sedes/sistemas que insertan
  libros de forma independiente (sin consultarse entre sí), un
  autoincremental normal chocaría (dos sedes generando el "id=45" cada
  una). Un UUID se genera localmente y prácticamente nunca colisiona,
  sin necesitar coordinación entre sistemas.

### Por qué Usuario NO usa UUID (a propósito)

- El id de `Usuario` es un dato **interno**: el cliente no navega la
  app por `/usuarios/{id}`, inicia sesión por *username*, y el id casi
  nunca se expone fuera del backend. Se identifica al usuario logueado
  por su username dentro del JWT, no por su id numérico.
- No hay necesidad de sincronizar usuarios entre sistemas externos en
  este proyecto, así que el argumento de "generación distribuida" no
  aplica aquí.
- Un entero autoincremental es más liviano en índices y más simple de
  leer/depurar en clase ("el usuario 3", "el usuario 7").

Esto es una decisión de diseño, no una regla absoluta: en otro proyecto,
si los ids de usuario también se expusieran en URLs públicas o se
sincronizaran entre sistemas, valdría la pena aplicar UUID ahí también.

### Costos de usar UUID (para que la decisión sea informada)

- Ocupa más espacio en disco/índices que un entero (16 bytes vs 4-8).
- No es secuencial: **no sirve para ordenar por fecha de creación**. Si
  se necesita saber qué se creó primero, hay que agregar una columna
  aparte (`fecha_creacion`), no confiar en el orden del UUID. Por eso
  en el `LibroController` de Spring se cambió el `Sort.by("id")` por
  `Sort.by("titulo")` al paginar.
- Es más largo de escribir/leer a mano que un número corto.

### Cómo está implementado en cada proyecto

**FastAPI** (`app/models.py`):

```python
from sqlalchemy.dialects.postgresql import UUID
import uuid

id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
```

- `as_uuid=True`: SQLAlchemy entrega/recibe objetos `uuid.UUID` de
  Python, no strings.
- `default=uuid.uuid4`: el UUID se genera en la aplicación (Python) al
  crear el objeto, antes del INSERT.
- Los endpoints de `libros.py` reciben `libro_id: uuid.UUID` como tipo
  del path param: FastAPI valida automáticamente el formato y responde
  422 si el texto no es un UUID válido, sin que el código del endpoint
  se ejecute.

**Spring Boot** (`model/Libro.java`):

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

- `GenerationType.UUID` (Hibernate 6+): genera el UUID en la aplicación
  antes del INSERT, igual que en FastAPI (a diferencia de `IDENTITY`,
  que dependía de que la base de datos asignara el siguiente número).
- `LibroRepository` pasó de `JpaRepository<Libro, Long>` a
  `JpaRepository<Libro, UUID>`.
- Los métodos del controller reciben `@PathVariable UUID id`: Spring
  convierte el texto de la URL a `UUID` automáticamente, respondiendo
  400 si no es válido.

### Probar con curl

```bash
# Crear un libro (FastAPI) y ver que el id que devuelve es un UUID
curl -X POST http://localhost:8000/libros/ \
  -H "Authorization: Bearer <TOKEN_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Cien años de soledad","autor":"GGM","isbn":"978-1","cantidad":3}'

# Usar ese UUID para consultarlo
curl http://localhost:8000/libros/3fa85f64-5717-4562-b3fc-2c963f66afa6 \
  -H "Authorization: Bearer <TOKEN>"

# Un id que NO es UUID válido -> 422 (FastAPI) / 400 (Spring)
curl http://localhost:8000/libros/no-es-un-uuid \
  -H "Authorization: Bearer <TOKEN>"
```
