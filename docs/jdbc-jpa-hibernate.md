# JDBC vs JPA vs Hibernate

Son tres capas distintas que se apilan una sobre otra, no alternativas entre sí.

## JDBC (Java Database Connectivity)

La API de más bajo nivel — viene con Java mismo. Define cómo Java se conecta y habla con cualquier base de datos relacional: abrir conexión, mandar SQL crudo, leer resultados fila por fila.

```java
Connection conn = DriverManager.getConnection(url, user, pass);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM categories");
```

Es verboso y manual: tú escribes el SQL, tú mapeas cada columna a un objeto Java a mano.

## JPA (Jakarta Persistence API)

Es una **especificación**, no una implementación. Define un conjunto de interfaces y anotaciones (`@Entity`, `@Id`, `EntityManager`, etc.) que describen cómo debería verse el mapeo objeto-relacional en Java, pero no trae código funcional por sí sola — necesita que alguien la implemente.

## Hibernate

Es la **implementación** de JPA más usada (existen otras, como EclipseLink). Es quien realmente hace el trabajo: toma las entidades anotadas con `@Entity`, genera el SQL por debajo, y usa **JDBC internamente** para ejecutar ese SQL contra la base de datos.

## Cómo se relacionan en un proyecto Spring Boot

```
Tu código (CategoryRepository, @Entity Category)
        │
        ▼
   JPA — la especificación (@Id, @GeneratedValue, JpaRepository)
        │
        ▼
Hibernate — la implementación que Spring Boot usa por defecto
        │
        ▼
   JDBC — lo que Hibernate usa por debajo para hablar con la BD
        │
        ▼
   PostgreSQL
```

## En la práctica

- Programas contra **JPA** (anotaciones, `JpaRepository`).
- **Hibernate** traduce eso a SQL real.
- **JDBC** es el canal final que lleva ese SQL a la base de datos.

Detalle importante: comportamientos específicos (como el batching de inserts con `GenerationType.IDENTITY` vs `SEQUENCE`) dependen de **Hibernate**, no de JPA como especificación — otra implementación de JPA podría comportarse distinto en ese aspecto.
