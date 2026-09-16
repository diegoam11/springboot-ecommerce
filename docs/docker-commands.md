# Comandos Docker para este proyecto

## Levantar Postgres con docker-compose (recomendado)

`.env` en la raíz (nunca se sube a git — agrégalo a `.gitignore`):

```env
DB_USER=postgres
DB_PASSWORD=postgres
DB_NAME=ecommerce
```

`docker-compose.yml`:

```yaml
services:
  db:
    image: postgres:16
    container_name: ecommerce-db
    environment:
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: ${DB_NAME}
    ports:
      - "5432:5432"
    volumes:
      - db-data:/var/lib/postgresql/data

volumes:
  db-data:
```

Docker Compose lee `.env` automáticamente. El `volume` mantiene los datos aunque borres el contenedor. Sube un `.env.example` (sin valores reales) como plantilla para el equipo.

```bash
docker compose up -d      # levantar en background
docker compose down       # detener y eliminar contenedor (datos persisten)
docker compose down -v    # eliminar también el volume (borra los datos)
docker compose logs -f db # seguir logs en vivo
```

## application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Mismo principio: nunca credenciales en texto plano en un archivo versionado.

## Contenedores (día a día)

```bash
docker ps                 # contenedores corriendo
docker ps -a               # incluye detenidos
docker stop/start/restart ecommerce-db
docker rm ecommerce-db     # eliminar (debe estar detenido)
docker logs -f ecommerce-db
docker exec -it ecommerce-db psql -U postgres -d ecommerce   # conectarte a psql
```

Dentro de `psql`: `\dt` (listar tablas), `\d tabla` (describirla), `\q` (salir).

## Imágenes

```bash
docker images              # imágenes descargadas
docker pull postgres:16    # descargar sin crear contenedor
docker rmi postgres:16     # eliminar imagen (falla si un contenedor la usa)
docker image prune -a      # eliminar imágenes no usadas
```

Una **imagen** es la plantilla inmutable; un **contenedor** es una instancia corriendo de esa imagen.

## Problemas comunes

| Problema | Causa |
|---|---|
| `port is already allocated` | Ya hay algo usando el puerto 5432 |
| La app no conecta aunque el contenedor está `up` | Postgres tarda unos segundos en aceptar conexiones al arrancar |
| Cambios en variables de entorno no se aplican | Solo se leen al crear el volume por primera vez — usa `docker compose down -v` para reiniciar |
