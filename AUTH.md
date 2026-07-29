# Guía de Autenticación (JWT) y Roles - Biblioteca API

Esta guía explica cómo se agregó autenticación con JWT y roles
(`ADMIN` / `USER`) a los dos proyectos, y por qué cada pieza está donde
está. Antes de esto, ambos proyectos solo encriptaban la contraseña
(bcrypt) pero cualquiera podía llamar a cualquier endpoint sin login.

## 1. Conceptos básicos

- **Autenticación**: probar quién eres (login con username/password).
- **Autorización**: decidir qué puedes hacer una vez que se sabe quién
  eres (por ejemplo, "solo ADMIN puede borrar libros").
- **JWT (JSON Web Token)**: un token firmado digitalmente que contiene
  información (normalmente el username y una fecha de expiración). El
  servidor lo firma con una clave secreta al hacer login; en cada
  request futura, el servidor vuelve a verificar esa firma para
  confirmar que el token es válido y no fue alterado.
  Importante: el servidor **no guarda sesiones**. Toda la validez del
  login vive en el propio token, mientras no haya expirado y la firma
  sea correcta.
- **Rol**: una etiqueta (`ADMIN` o `USER`) guardada en la base de datos
  junto al usuario, que se usa para decidir permisos.

## 2. Flujo general (igual en ambos proyectos)

```
1. POST /auth/registro   (público)   -> crea el usuario, SIEMPRE como USER
2. POST /auth/login      (público)   -> valida username+password, devuelve un JWT
3. Cliente guarda el JWT y lo manda en cada request:
     Authorization: Bearer <token>
4. El servidor valida la firma/expiración del token, identifica al
   usuario, consulta su rol actual en la BD, y decide si puede o no
   ejecutar la acción pedida.
```

Reglas de negocio usadas en ambos proyectos:

| Acción                          | Rol requerido    |
|---------------------------------|-------------------|
| `POST /auth/registro`, `/login` | público            |
| Listar / obtener (`GET`)        | ADMIN o USER (cualquier autenticado) |
| Crear / actualizar / borrar     | solo ADMIN        |

## 3. Cómo crear el primer ADMIN

El registro público (`/auth/registro`) siempre crea usuarios `USER` a
propósito, para que nadie pueda auto-asignarse `ADMIN`. Por eso, el
**primer** ADMIN de cada proyecto se crea manualmente:

1. Regístrate normalmente por `/auth/registro` (quedas como USER).
2. Conéctate a la base de datos y sube tu propio rol:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE username = 'tu_usuario';
```

A partir de ahí, ya puedes loguearte como ADMIN y usar
`PUT /usuarios/{id}/rol` para ascender/degradar a otros usuarios sin
tocar la base de datos directamente.

## 4. Cómo está implementado en FastAPI

Archivos nuevos/clave:

- `app/security.py`: toda la lógica de JWT.
  - `crear_access_token(username)`: firma un token con `python-jose`
    usando `JWT_SECRET_KEY` (variable de entorno).
  - `obtener_usuario_actual(...)`: **dependency** de FastAPI que se
    engancha con `Depends(...)` en cada endpoint protegido. Lee el
    header `Authorization`, valida el token, y busca al usuario en la BD.
  - `requerir_rol(*roles)`: fábrica de dependencies. Genera una
    dependency que además verifica que `usuario.rol` esté en la lista
    de roles permitidos (si no, responde 403).
- `app/routers/auth.py`: expone `/auth/registro` y `/auth/login`
  (públicos, sin ninguna dependency de seguridad).
- `app/routers/usuarios.py` y `app/routers/libros.py`: cada ruta declara
  su propio `dependencies=[Depends(requerir_rol(...))]`, por ejemplo:

```python
@router.post("/", ..., dependencies=[Depends(requerir_rol(models.ROL_ADMIN))])
def crear_libro(...):
    ...
```

Ventaja de este estilo (dependency injection por ruta): queda muy
explícito, ruta por ruta, quién puede entrar — perfecto para ver en
clase endpoint por endpoint.

Probar con curl:

```bash
# Registro
curl -X POST http://localhost:8000/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"juan","password":"1234"}'

# Login (form-urlencoded, no JSON, porque usa OAuth2PasswordRequestForm)
curl -X POST http://localhost:8000/auth/login \
  -d "username=juan&password=1234"
# -> {"access_token": "...", "token_type": "bearer"}

# Usar el token
curl http://localhost:8000/libros/ \
  -H "Authorization: Bearer <PEGA_AQUI_EL_TOKEN>"
```

## 5. Cómo está implementado en Spring Boot

Archivos nuevos/clave (paquete `security/`):

- `JwtService`: crea (`generarToken`) y valida (`validarToken`,
  `extraerUsername`) tokens JWT usando la librería `jjwt`, firmando con
  `jwt.secret` (de `application.properties`).
- `CustomUserDetailsService`: implementa `UserDetailsService` de Spring
  Security. Traduce nuestra entidad `Usuario` al modelo `UserDetails`
  que Spring entiende, incluyendo el rol como `ROLE_ADMIN` / `ROLE_USER`
  (el prefijo `ROLE_` es obligatorio para que `hasRole("ADMIN")` funcione).
- `JwtAuthFilter`: un `OncePerRequestFilter` que se ejecuta en cada
  request, ANTES de llegar al controller. Lee el header `Authorization`,
  valida el token, y si es válido carga al usuario en el
  `SecurityContext` de Spring (así el resto del framework sabe "quién"
  está haciendo la petición).
- `SecurityConfig`: define, en un solo lugar (a diferencia de FastAPI,
  donde cada ruta declara su propia dependency), las reglas de acceso
  por método HTTP y path:

```java
.requestMatchers(HttpMethod.GET, "/usuarios/**", "/libros/**").hasAnyRole("ADMIN", "USER")
.requestMatchers(HttpMethod.POST, "/usuarios/**", "/libros/**").hasRole("ADMIN")
```

- `AuthController`: expone `/auth/registro` y `/auth/login` (marcados
  como públicos en `SecurityConfig` con `"/auth/**".permitAll()`).

Diferencia clave de estilo vs. FastAPI: en Spring, la autorización vive
centralizada en `SecurityConfig` (basada en el método HTTP + path); en
FastAPI vive repartida en cada endpoint vía `Depends(requerir_rol(...))`.
Ambos enfoques son válidos — vale la pena discutir en clase las
ventajas de cada uno (centralizado y declarativo vs. explícito por ruta).

Probar con curl:

```bash
# Registro
curl -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"juan","password":"1234"}'

# Login (JSON, no form, a diferencia de FastAPI)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"juan","password":"1234"}'
# -> {"accessToken": "...", "tokenType": "bearer"}

# Usar el token
curl http://localhost:8080/libros \
  -H "Authorization: Bearer <PEGA_AQUI_EL_TOKEN>"
```

## 6. Errores comunes al probar esto

- **401 Unauthorized**: no mandaste el header `Authorization`, el token
  expiró, o está mal formado (recuerda el prefijo `Bearer `).
- **403 Forbidden**: el token es válido, pero tu rol no alcanza para esa
  acción (por ejemplo, un USER intentando borrar un libro).
- **"El username ya existe"**: ya te registraste antes; usa `/auth/login`.
- Si cambiaste `JWT_SECRET_KEY` / `jwt.secret` después de emitir tokens,
  todos los tokens viejos dejan de ser válidos (la firma ya no coincide).
