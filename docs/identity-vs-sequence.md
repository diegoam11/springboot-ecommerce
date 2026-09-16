# IDENTITY vs SEQUENCE

Ambas son estrategias de `@GeneratedValue` para generar el valor del `@Id` en JPA. La diferencia está en **cuándo** se genera ese id y qué tan bien puede optimizar Hibernate los inserts.

## IDENTITY

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

- Postgres genera el id **como parte del propio `INSERT`** (equivalente a una columna `bigserial`), usando `RETURNING id` internamente.
- Hibernate no calcula nada: solo recibe el id real que la base de datos generó en esa misma operación.
- El problema no es la generación en sí, sino que Hibernate necesita ese id **inmediatamente después** de cada insert — por ejemplo, para usarlo en relaciones (`@ManyToOne`, `@OneToMany`) de otras entidades en memoria.
- Como cada insert requiere esperar la respuesta antes de continuar, **Hibernate no puede agrupar varios inserts en un solo batch**. El batching de JDBC funciona mandando N inserts de una vez sin esperar nada en el medio, y eso es imposible si necesitas el resultado de cada uno antes de seguir.

## SEQUENCE

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
@SequenceGenerator(name = "products_seq", sequenceName = "products_id_seq", allocationSize = 1)
private Long id;
```

- Hibernate le pide el próximo id a una secuencia de Postgres **antes** de insertar — una operación simple y separada del insert en sí.
- Para cuando arma los `INSERT`, Hibernate ya tiene todos los ids en mano, así que puede mandarlos todos juntos en un solo batch, sin pausas intermedias.
- Esto habilita batch inserts reales, que mejoran el rendimiento en cargas con muchos registros.

## Comparación rápida

| | IDENTITY | SEQUENCE |
|---|---|---|
| Quién genera el id | La base de datos, durante el insert | Una secuencia de la BD, consultada antes del insert |
| Cuándo lo sabe Hibernate | Después de insertar | Antes de insertar |
| Permite batch inserts | No | Sí |
| Simplicidad | Más simple, menos configuración | Requiere definir la secuencia |
| Cuándo usarla | Proyectos pequeños, práctica, bajo volumen de escritura | Sistemas con mucho volumen de inserts, donde importa el rendimiento |

## Para tener en cuenta

Para proyectos de práctica o bajo volumen, `IDENTITY` es perfectamente válido y el más simple de configurar. En sistemas empresariales con mucho volumen de escritura, `SEQUENCE` suele preferirse justamente por permitir batching.
