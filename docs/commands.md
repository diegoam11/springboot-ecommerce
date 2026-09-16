# Comandos Maven

- `mvn clean` — borra target/, limpia el build anterior
- `mvn compile` — compila el código fuente
- `mvn test` — corre los tests
- `mvn package` — compila, testea y genera el .jar
- `mvn install` — genera el .jar y lo copia a ~/.m2/repository
- `mvn spring-boot:run` — levanta la app sin empaquetar
- `mvn clean package -DskipTests` — build rápido sin tests