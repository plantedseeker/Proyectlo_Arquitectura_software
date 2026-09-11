# 08 — Decisión de estilo arquitectónico — S7

- Fecha: 2026-09-11
- Estado: **decisión documentada; pendiente de ratificación humana en mini-comité**
- Sistema: UTrabajo
- Alcance: arquitectura actual y evolución inmediata de mensajería

## Propósito

Consolidar en el **dossier del curso** la comparación de estilos, los drivers priorizados que condicionan la decisión, el mapa modular objetivo y la evidencia que respalda la selección. Este archivo es el artefacto canónico solicitado por el checklist para la decisión de estilo arquitectónico.

## Fuente de los drivers priorizados

Los drivers usados para evaluar las alternativas no se redefinen aquí. Se toman de [`02-stakeholders-drivers.md`](02-stakeholders-drivers.md), donde el equipo priorizó:

1. **Rendimiento al abrir conversaciones extremas**.
2. **Confidencialidad entre conversaciones**.
3. **Orden e integridad de mensajes**.
4. **Modificabilidad del cliente**.
5. **Reproducibilidad**.

Costo operacional, complejidad y reversibilidad se usan como criterios adicionales de decisión arquitectónica, pero no reemplazan esos cinco drivers priorizados.

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
| Operación | Un despliegue | Un despliegue + regla CI | Servicio, broker, contratos y observabilidad |
| Costo | Mínimo al inicio | Bajo | Alto |
| Escalado independiente | No | No | Sí |
| Reversibilidad | Media | Alta | Media/baja tras separar datos/contratos |
| Evidencia actual | No justifica omitir límites | C4, código, S4 y S7 | No existe presión medida que pague la complejidad |
| Decisión | Descartar | **Seleccionar** | Diferir |

## Matriz explícita: alternativas ↔ drivers priorizados

Esta matriz responde literalmente a la pregunta del checklist: **qué driver satisface, compromete o deja sin mejora cada alternativa**.

| Driver priorizado de `02-stakeholders-drivers.md` | Monolito sin límites | Monolito modular + regla | Microservicio + broker |
| --- | --- | --- | --- |
| **1. Rendimiento al abrir conversaciones extremas** | No aporta una mejora específica; conserva el comportamiento actual pero permite degradación estructural | **Satisface el escenario medido** sin agregar infraestructura; mantiene el índice y permite evolucionar paginación de forma independiente | Puede aportar escalado independiente, pero **no hay evidencia actual** que demuestre que sea necesario para cumplir S4/S7 |
| **2. Confidencialidad entre conversaciones** | Puede dispersar autorización si controladores empiezan a consultar persistencia directamente | **Favorece el driver** al mantener autorización y reglas de negocio localizables antes de JDBC | Exige autorización entre servicios, más secretos y más superficie de ataque |
| **3. Orden e integridad de mensajes** | Depende de disciplina manual y de la BD; no agrega protección arquitectónica | **Conserva el flujo actual** de servicio, transacciones, PK/FK y orden estable por `(sent_at,id)` | Introduce consistencia distribuida, reintentos y contratos adicionales que aumentan complejidad |
| **4. Modificabilidad del cliente** | El acoplamiento interno puede crecer y volver más riesgoso cambiar backend sin afectar Android | **Favorece el driver**: mantiene HTTP/Retrofit estable y permite cambiar internamente servicios/persistencia | Añade contratos distribuidos y migraciones de integración que aumentan el costo de cambio |
| **5. Reproducibilidad** | Un despliegue es simple, pero los límites dependen de revisión manual | **Favorece el driver**: un despliegue + Docker + CI + regla verificable | Requiere reproducir más procesos, red, broker, secretos y observabilidad |

### Resultado contra los drivers

El **monolito modular con regla** es la alternativa que mejor satisface los drivers priorizados con la menor complejidad adicional demostrablemente necesaria. La decisión no afirma que un microservicio sea incorrecto en general; afirma que **la evidencia actual no justifica pagar su costo ahora**.

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

## Mapa modular objetivo S7–S8

El objetivo inmediato mantiene el mismo despliegue físico, pero hace explícitas y protegibles las fronteras internas:

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

El estilo arquitectónico y la estrategia de paginación son decisiones separadas. S7 demuestra que `OFFSET` profundo puede ser costoso, pero eso no obliga a distribuir mensajería. La evolución de `LIMIT/OFFSET` hacia cursor se conserva como decisión independiente y reversible en ADR-003.

## Crítica de la propuesta de IA

La propuesta de IA fue tratada como hipótesis, no como evidencia. Se aceptaron la observabilidad de mensajería, el límite de página, el índice y la comparación de cursor; se rechazaron por ahora microservicio, Redis, broker y Kubernetes; WebSocket queda fuera del alcance actual. El detalle está en [`../docs/architecture/propuesta-ia-critica.md`](../docs/architecture/propuesta-ia-critica.md).

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

- [`02-stakeholders-drivers.md`](02-stakeholders-drivers.md) — fuente de los drivers priorizados.
- [`../docs/architecture/alternativas-s7.md`](../docs/architecture/alternativas-s7.md) — comparación detallada.
- [`../docs/architecture/modulos-y-limites.md`](../docs/architecture/modulos-y-limites.md) — base As-Is y mapa objetivo.
- [`../docs/adr/ADR-001-limites-modulo-mensajeria.md`](../docs/adr/ADR-001-limites-modulo-mensajeria.md) — decisión de estilo: monolito modular.
- [`../docs/adr/ADR-002-limites-modulos-dependencias.md`](../docs/adr/ADR-002-limites-modulos-dependencias.md) — límites y dependencias permitidas/prohibidas.
- [`../docs/adr/ADR-003-paginacion-historial-mensajes.md`](../docs/adr/ADR-003-paginacion-historial-mensajes.md) — evolución de paginación.
- [`../experimentos/localizacion-s7/README.md`](../experimentos/localizacion-s7/README.md) — localización del costo SQL.
- [`07-c4-componentes-backend.md`](07-c4-componentes-backend.md) — recorrido As-Is.
- [`../scripts/check_architecture.py`](../scripts/check_architecture.py) — restricción ejecutable.

## Ratificación pendiente

El contenido anterior consolida evidencia versionada. El estado de los ADR solo debe cambiar después del mini-comité real y del registro del veredicto del equipo.