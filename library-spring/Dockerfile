# =============================================================================
# Dockerfile - Biblioteca API (Spring Boot)
# =============================================================================
# Este Dockerfile usa "multi-stage build" (construcción en varias etapas).
# La idea: usamos UNA imagen pesada (con Maven + JDK) solo para compilar
# el proyecto, y luego copiamos ÚNICAMENTE el .jar resultante a una imagen
# mucho más liviana que solo tiene el Java Runtime (JRE), no todo el kit
# de desarrollo. Así la imagen final pesa mucho menos y es más segura
# (no viaja con Maven, código fuente, ni herramientas de build).
# =============================================================================


# =============================================================================
# ETAPA 1: "build" -> compila el proyecto y genera el .jar
# =============================================================================
# "AS build" le pone nombre a esta etapa para poder referenciarla después
# con --from=build en la segunda etapa.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos primero SOLO el pom.xml (las dependencias), no el código.
# Igual que en el proyecto de FastAPI: si el pom.xml no cambia, Docker
# reutiliza la capa donde ya se descargaron las dependencias de Maven,
# en vez de volver a bajarlas todas de internet en cada build.
COPY pom.xml .

# Descarga las dependencias declaradas en el pom.xml y las deja en la
# caché local de Maven dentro de esta capa de la imagen.
# "-B" (batch mode) evita mensajes interactivos innecesarios en el log.
RUN mvn -B dependency:go-offline

# Ahora sí copiamos el código fuente completo.
COPY src ./src

# Compilamos y empaquetamos el proyecto en un .jar ejecutable.
# "-DskipTests" evita correr los tests durante el build de la imagen
# (los tests se corren aparte, no en cada build de Docker).
RUN mvn -B clean package -DskipTests


# =============================================================================
# ETAPA 2: "runtime" -> imagen final, liviana, solo para EJECUTAR el .jar
# =============================================================================
# "jre" en vez de "jdk": no necesitamos compilador ni herramientas de
# desarrollo para correr la app, solo la máquina virtual de Java.
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copiamos ÚNICAMENTE el .jar generado en la etapa "build".
# --from=build le dice a Docker: "trae este archivo de la otra etapa,
# no de tu máquina local". El resto de la etapa "build" (Maven, código
# fuente, dependencias descargadas) se descarta y no queda en la imagen
# final, lo que reduce mucho su tamaño.
COPY --from=build /app/target/*.jar app.jar

# Puerto en el que escucha Spring Boot por defecto (ver application.properties).
EXPOSE 8080

# Comando que arranca el contenedor: corre el jar con el JRE.
ENTRYPOINT ["java", "-jar", "app.jar"]
