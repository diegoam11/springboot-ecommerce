# La carpeta `target/`

## Qué es

`target/` es la carpeta donde Maven coloca todo lo que **genera** durante el build. No es código fuente, es contenido derivado — Maven la reconstruye por completo a partir de `src/` y `pom.xml` cada vez que compilas.

Por eso siempre va en `.gitignore` y nunca se sube al repositorio.

## Qué contiene

| Contenido | Se genera con |
|---|---|
| `.class` (bytecode compilado) | `mvn compile` |
| Resources copiados (`application.properties`, etc.) | `mvn compile` |
| Resultados de tests | `mvn test` |
| `.jar` final empaquetado | `mvn package` |
| Metadata del build (`maven-archiver/`, `maven-status/`) | `mvn package` |

## Estructura típica (después de `mvn package`)

```
target/
├── classes/                → bytecode (.class) + resources copiados
│   ├── application.properties
│   └── org/diego/ecommerce/...*.class
├── generated-sources/      → código generado por anotaciones/plugins
├── maven-archiver/         → metadata del jar generado
├── maven-status/           → tracking interno de qué se compiló y cuándo
├── test-classes/           → bytecode de los tests
├── surefire-reports/       → resultados de mvn test
└── tu-proyecto-0.0.1-SNAPSHOT.jar   → jar final ejecutable
```

Con solo `mvn compile`, únicamente existen `classes/` y `generated-sources/` — el resto aparece a partir de `mvn test` y `mvn package`.

## Por qué es importante

- Es la fuente de verdad de "lo que se va a ejecutar" — el `.jar` que corres con `java -jar` sale de aquí, no de `src/` directamente.
- Si algo se comporta raro (cambios que no se reflejan, errores de compilación extraños), suele deberse a un `target/` desactualizado.

## Comandos que la afectan

| Comando | Efecto sobre `target/` |
|---|---|
| `mvn clean` | La borra por completo |
| `mvn compile` | Crea/actualiza `target/classes/` con el bytecode |
| `mvn test` | Compila y además genera reportes de tests |
| `mvn package` | Genera el `.jar` final, además de todo lo anterior |
| `mvn install` | Igual que `package`, y copia el `.jar` a `~/.m2/repository` |

**Regla práctica:** si algo no cuadra, `mvn clean package` (o `./mvnw clean package` con el wrapper) resuelve el 90% de los problemas raros de build, porque fuerza a Maven a regenerar todo desde cero.