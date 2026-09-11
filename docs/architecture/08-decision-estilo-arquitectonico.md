# 08 — Decisión de estilo arquitectónico — S7

- Fecha: 2026-09-11
- Estado: **decisión documentada; pendiente de ratificación humana en mini-comité**
- Sistema: UTrabajo
- Alcance: arquitectura actual y evolución inmediata de mensajería

## Propósito

Consolidar en un único artefacto la comparación de estilos, los drivers que condicionan la decisión, el mapa modular objetivo y la evidencia que respalda la selección. El documento no reemplaza los ADR: los resume y enlaza.

## Drivers priorizados

1. **Seguridad y autorización:** la lectura/escritura de chats debe conservar un punto claro de autorización (`requireChatParticipant`) antes del acceso a datos.
2. **Mantenibilidad y modularidad:** evitar que controladores HTTP accedan directamente a JDBC/SQL y conservar una dirección de dependencias verificable.
3. **Costo y operación:** el proyecto se ejecuta con una API Spring Boot y PostgreSQL; infraestructura adicional debe justificarse con evidencia.
4. **Rendimiento:** la página reciente cumple el escenario S4; S7 demuestra que el costo aparece al profundizar con `OFFSET`.
5. **Reversibilidad:** las decisiones deben permitir evolucionar contratos y límites sin una migración distribuida prematura.

## Evidencia disponible

- S4: mediana de p95 de **9,109 ms** para los últimos 50 mensajes de una conversación de 100.000, con 10 VU y 40 solicitudes por corrida válida.
- S7: autorización 0,081 ms; `OFFSET 0` 0,120 ms; `OFFSET 50000` 102,050 ms; cursor equivalente 0,123 ms.
- C3 real: `controller → service/auth → JdbcClient → PostgreSQL`.
- CI: `scripts/check_architecture.py` impide referencias JDBC/SQL directas desde controladores.

## Alternativas de estilo

| Criterio | Monolito sin límites | Monolito modular + regla | Microservicio + broker |
| --- | --- | --- | --- |
| Complejidad inicial | Baja | Baja/media | Alta |
| Seguridad | Riesgo de dispersión | Autorización y acceso localizables | Autorización distribuida y más superficie |
| Operación | Un despliegue | Un despliegue + regla CI | Servicio, broker, contratos, observabilidad |
| Costo | Mínimo al inicio | Bajo | Alto |
| Escalado independiente | No | No | Sí |
| Reversibilidad | Media | Alta | Media/baja tras separar datos/contratos |
| Evidencia actual | No justifica omitir límites | C4, código, S4 y S7 | No existe presión medida que pague la complejidad |
| Decisión | Descartar | **Seleccionar** | Diferir |

## Decisión

Mantener UTrabajo como **monolito modular** para el backend. Mensajería permanece dentro de la API Spring Boot y se protege la dirección de dependencias:

```text
Android / HTTP
    ↓
controllers
    ↓
services / auth
    ↓
JdbcClient / persistencia
    ↓
PostgreSQL 16
```

La decisión de estilo no implica crear hoy un microservicio, Redis, broker o WebSocket. Esas opciones solo se reconsideran ante una presión medible de disponibilidad, escalado independiente, asincronía o equipos/despliegues separados.

## Mapa modular objetivo

El objetivo inmediato mantiene el mismo despliegue físico, pero hace explícitas las fronteras internas:

```text
Android
├── presentation  → pantallas y navegación
└── data          → sesión, Repository y Retrofit

Backend
├── controller    → HTTP, validación de entrada y delegación
├── auth          → autenticación, sesión y principal
├── service       → casos de uso y autorización de dominio
├── persistence   → JDBC / PostgreSQL
└── file storage  → archivos configurados
```

Dependencias clave:

| Origen | Permitido | Prohibido directamente |
| --- | --- | --- |
| Android `presentation` | Android `data` | PostgreSQL, JDBC, clases internas backend |
| Android `data` | HTTP/Retrofit, modelos, SharedPreferences | SQL o controladores Spring |
| Backend `controller` | services, modelos, seguridad web | `JdbcClient`, `java.sql`, `javax.sql` |
| Backend `service` / `auth` | persistencia, modelos y almacenamiento requerido | presentación Android |
| Persistencia | PostgreSQL | HTTP/UI |

La división futura de `UTrabajoService` en servicios internos es una mejora posible, no se presenta como implementada actualmente.

## Implicaciones de seguridad

- Mantener el flujo de autorización antes de leer/escribir mensajes facilita auditar acceso a chats.
- Prohibir JDBC en controladores reduce el riesgo de saltarse reglas de dominio por conveniencia.
- Un microservicio prematuro agregaría autenticación entre servicios, secretos, red y nuevos puntos de falla sin evidencia que lo justifique.
- La regla automática es mínima: no sustituye pruebas funcionales ni revisión de autorización.

## Relación con la paginación

El estilo arquitectónico y la estrategia de paginación son decisiones separadas. S7 demuestra que `OFFSET` profundo puede ser costoso, pero eso no obliga a distribuir mensajería. La evolución de `LIMIT/OFFSET` hacia cursor se conserva como decisión independiente y reversible.

## Crítica de la propuesta de IA

La propuesta de IA fue tratada como hipótesis, no como evidencia. Se aceptaron la observabilidad de mensajería, el límite de página, el índice y la comparación de cursor; se rechazaron por ahora microservicio, Redis, broker y Kubernetes; WebSocket queda fuera del alcance actual. El detalle está en [`propuesta-ia-critica.md`](propuesta-ia-critica.md).

## Consecuencias

### Positivas

- Conserva un despliegue sencillo y coherente con la arquitectura As-Is.
- Protege límites con una regla ejecutable en CI.
- Reduce costo operativo frente a una distribución prematura.
- Mantiene alta reversibilidad.

### Negativas

- La API continúa compartiendo proceso y despliegue con otros dominios.
- `UTrabajoService` sigue concentrando varios casos de uso.
- La regla actual cubre solo una familia de dependencias indebidas.

## Trazabilidad

- [`alternativas-s7.md`](alternativas-s7.md) — comparación detallada.
- [`modulos-y-limites.md`](modulos-y-limites.md) — mapa y dependencias.
- [`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md) — decisión de estilo: monolito modular.
- [`ADR-002`](../adr/ADR-002-limites-modulos-dependencias.md) — límites y dependencias permitidas/prohibidas.
- [`ADR-003`](../adr/ADR-003-paginacion-historial-mensajes.md) — evolución de paginación.
- [`Experimento S7`](../../experimentos/localizacion-s7/README.md) — localización del costo SQL.
- [`C3 backend`](../../dossier/07-c4-componentes-backend.md) — recorrido As-Is.
- [`Regla CI`](../../scripts/check_architecture.py) — restricción ejecutable.

## Ratificación pendiente

El contenido anterior consolida evidencia versionada. El estado de los ADR solo debe cambiar después del mini-comité real y del registro del veredicto del equipo.