# Flujo de una request con JWT: 3 escenarios de `POST /api/v1/products`

Este documento traza, capa por capa, qué pasa con una request real según el estado del token y el rol del usuario. Todos los casos fueron verificados contra el servidor corriendo en `localhost:8080`.

## Las dos capas

```
Cliente
  │
  ▼
┌─────────────────────────────────────────────────────┐
│  CAPA 1: Servlet Filter Chain (Spring Security)      │
│                                                       │
│  ... → SecurityContextHolderFilter                   │
│         HeaderWriterFilter                            │
│         CsrfFilter (deshabilitado)                    │
│         LogoutFilter                                  │
│         JwtAuthenticationFilter  ← filtro custom       │
│         AnonymousAuthenticationFilter                 │
│         SessionManagementFilter                       │
│         ExceptionTranslationFilter                    │
│         AuthorizationFilter                            │
└─────────────────────────────────────────────────────┘
  │ (solo si AuthorizationFilter concede acceso)
  ▼
┌─────────────────────────────────────────────────────┐
│  CAPA 2: Spring MVC (DispatcherServlet)              │
│  → ProductController → ProductService → Repository   │
│  → @RestControllerAdvice (GlobalExceptionHandler)     │
└─────────────────────────────────────────────────────┘
```

`GlobalExceptionHandler` (`@RestControllerAdvice`) solo puede atrapar excepciones que ocurren **dentro** de la Capa 2 (dentro de la ejecución de un controller). Todo lo que pasa en la Capa 1 se resuelve con los hooks propios de Spring Security: `AuthenticationEntryPoint` y `AccessDeniedHandler`, conectados vía `ExceptionTranslationFilter`.

---

## Caso 1 — Token válido + rol ADMIN (happy path)

**Request:**
```
POST /api/v1/products
Authorization: Bearer <JWT válido, rol ADMIN>
```

1. `JwtAuthenticationFilter` (`JwtAuthenticationFilter.java:43-64`): `jwtService.extractUsername(token)` verifica la firma HMAC y el `exp` sin problema → `UserDetailsServiceImpl.loadUserByUsername` trae al usuario de la BD → `isTokenValid` confirma → se arma un `UsernamePasswordAuthenticationToken` con authority `ROLE_ADMIN` y se setea en el `SecurityContext`.
2. `AnonymousAuthenticationFilter`: no hace nada, ya hay una `Authentication` real.
3. `AuthorizationFilter`: evalúa `.requestMatchers(POST, "/api/v1/products").hasRole("ADMIN")` → la authority coincide → **acceso concedido** → continúa la cadena.
4. La request entra a **Capa 2**: `DispatcherServlet` resuelve `ProductController.create(...)`, deserializa el body a `ProductRequest`, llama a `ProductService.create(...)`:
   - `categoryRepository.findById(categoryId)` trae la categoría.
   - `productRepository.save(...)` inserta el producto (Hibernate genera el `INSERT`, Postgres asigna el `id`).
   - se arma el `ProductResponse` y Spring MVC lo serializa a JSON.
5. La respuesta sale por la misma cadena de filtros (sin cambios), `HeaderWriterFilter` escribe los headers de seguridad encolados.

**Resultado:**
```
200 OK
{"id":5,"name":"Lavadora Samsung","price":1599.99,"stock":12,"categoryId":11,"categoryName":"Electrodomésticos"}
```

---

## Caso 2 — Token corrupto (firma inválida)

**Request:** mismo endpoint, con un JWT cuya firma no coincide con el secreto del servidor.

1. `JwtAuthenticationFilter`: `jwtService.extractUsername(token)` lanza `io.jsonwebtoken.security.SignatureException` (subclase de `JwtException`) al intentar verificar la firma. El `catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex)` la atrapa y solo loguea en debug — el `SecurityContext` **queda vacío**.
2. `AnonymousAuthenticationFilter`: como el contexto sigue vacío, rellena con un `AnonymousAuthenticationToken` (principal `"anonymousUser"`, authority `ROLE_ANONYMOUS`).
3. `AuthorizationFilter`: evalúa `hasRole("ADMIN")` contra `[ROLE_ANONYMOUS]` → no coincide → lanza `AccessDeniedException`.
4. `ExceptionTranslationFilter` atrapa la excepción y se pregunta: *¿el `Authentication` actual es anónimo?* → **sí** → la reinterpreta como "nunca hubo autenticación real", la envuelve en `InsufficientAuthenticationException` y llama al **`AuthenticationEntryPoint`** (no al `AccessDeniedHandler`).
5. `JwtAuthenticationEntryPoint.commence(...)` escribe la respuesta directamente y corta la cadena ahí. **La Capa 2 nunca se ejecuta** — `ProductController`/`ProductService` no se enteran de la request.

**Resultado (verificado):**
```
401 Unauthorized
{"timestamp":"2026-10-01T06:35:06.371307Z","status":401,"errorCode":"UNAUTHORIZED","message":"Authentication required or token invalid","path":"/api/v1/products"}
```

---

## Caso 3 — Token válido, pero rol USER (no ADMIN)

**Request:** JWT real y válido (firma correcta, no expirado), de un usuario con `role = USER`.

1. `JwtAuthenticationFilter`: todo el parseo sale bien, `userDetails.getAuthorities()` devuelve `[ROLE_USER]`. Se setea una `Authentication` **real y autenticada** en el `SecurityContext` (a diferencia del Caso 2, aquí no queda vacío).
2. `AnonymousAuthenticationFilter`: no hace nada, ya hay una `Authentication` real.
3. `AuthorizationFilter`: evalúa `hasRole("ADMIN")` contra `[ROLE_USER]` → no coincide → lanza `AccessDeniedException`.
4. `ExceptionTranslationFilter` se hace la misma pregunta: *¿es anónimo?* → **no**, es un usuario real autenticado → **no** reinterpreta nada, llama directo al **`AccessDeniedHandler`**.
5. `JwtAccessDeniedHandler.handle(...)` escribe la respuesta y corta la cadena. Capa 2 tampoco se ejecuta, pero esta vez sí se sabía exactamente quién hizo la request (solo que sin el rol necesario).

**Resultado (verificado):**
```
403 Forbidden
{"timestamp":"2026-10-01T06:45:57.015901Z","status":403,"errorCode":"FORBIDDEN","message":"You do not have permission to perform this action","path":"/api/v1/products"}
```

---

## Tabla comparativa

| Caso | `SecurityContext` al llegar a `AuthorizationFilter` | Excepción lanzada | ¿Es anónimo? | Handler invocado | Status |
|---|---|---|---|---|---|
| Token válido + ADMIN | `Authentication` real, `ROLE_ADMIN` | ninguna | — | — (pasa a Capa 2) | 200 |
| Token corrupto | `AnonymousAuthenticationToken` (relleno) | `AccessDeniedException` | sí | `AuthenticationEntryPoint` (reinterpretada) | 401 |
| Token válido + USER | `Authentication` real, `ROLE_USER` | `AccessDeniedException` | no | `AccessDeniedHandler` | 403 |

**La conclusión clave:** lo que decide 401 vs 403 no es "hubo o no un problema de rol" — es si el `SecurityContext` tenía o no una identidad real en el momento exacto en que `AuthorizationFilter` negó el acceso. Un token roto nunca llega a tener una identidad real, así que siempre termina en 401 pese a que la regla que lo bloquea es técnicamente una de rol (`hasRole("ADMIN")`).

## Archivos relevantes

- `src/main/java/org/diego/ecommerce/demo/shared/security/JwtAuthenticationFilter.java`
- `src/main/java/org/diego/ecommerce/demo/shared/security/JwtService.java`
- `src/main/java/org/diego/ecommerce/demo/shared/security/JwtAuthenticationEntryPoint.java`
- `src/main/java/org/diego/ecommerce/demo/shared/security/JwtAccessDeniedHandler.java`
- `src/main/java/org/diego/ecommerce/demo/shared/security/SecurityConfig.java`
