# URL, URI, path, endpoint y otros conceptos

## Ejemplo base

```
http://localhost:8080/api/v1/products
└─┬─┘   └────┬────┘ └────┬─────┘
scheme    authority     path
```

Con query params:
```
http://localhost:8080/api/v1/products?stock=10&sort=price
                                        └──────┬──────┘
                                          query string
```

## Definiciones

| Término | Qué es | En el ejemplo |
|---|---|---|
| **URI** | Identificador genérico de un recurso. Es el concepto más amplio — toda URL es una URI, pero no toda URI es una URL. | `http://localhost:8080/api/v1/products` |
| **URL** | Una URI que además indica cómo localizar el recurso (incluye scheme + host). | `http://localhost:8080/api/v1/products` |
| **Path / Ruta** | Solo la parte jerárquica después del host, antes del `?`. "Path" y "ruta" son el mismo concepto en inglés/español. | `/api/v1/products` |
| **Endpoint** | Método HTTP + path juntos. El mismo path con distinto verbo es un endpoint distinto. | `GET /api/v1/products` |
| **Resource** | El concepto REST detrás del path — la "cosa" que se manipula. | "la colección de productos" |
| **Query string** | Parámetros después del `?`, para filtrar/ordenar/paginar. | `?stock=10&sort=price` |
| **Host / Authority** | Servidor + puerto. | `localhost:8080` |

## El detalle que más confunde: `{id}`

```
GET /api/v1/products/{id}       ← path variable
GET /api/v1/products?category=x ← query parameter
```

**Regla práctica:**
- Si el valor identifica **qué** recurso → va en el path → `/products/5`
- Si el valor **filtra o modifica** una colección → va en el query string → `/products?sort=price&page=2`

## Para recordar

- Un **endpoint** siempre es método + path juntos, nunca solo el path.
- **URI** es el término más general; **URL** es el subtipo más común y específico que usamos casi siempre en la práctica.
- **Path** = **ruta**, son el mismo concepto.
