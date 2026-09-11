# Diseño modular — base As-Is y mapa objetivo S7–S8

Este documento separa explícitamente lo que **existe hoy** de lo que se propone
como **objetivo inmediato** para proteger límites y responsabilidades sin cambiar
el despliegue físico del sistema.

## 1. Base As-Is verificada

### Regla general observada

UTrabajo mantiene un despliegue backend único y el código actual muestra este
flujo:

```text
Cliente Android
  → contrato HTTP / controladores
    → servicios de negocio y autenticación
      → persistencia JDBC
        → PostgreSQL 16
```

### Módulos y responsabilidades actuales

| Límite | Responsabilidad | Puede depender de | No debe depender directamente de |
| --- | --- | --- | --- |
| Android `presentation` | Estado y pantallas Compose | `data.UTrabajoRepository`, modelos de UI | PostgreSQL, JDBC, clases internas del backend |
| Android `data` | Sesión local y contrato Retrofit | HTTP, modelos de datos, SharedPreferences | Esquema SQL o controladores Spring |
| Backend `controller` | Traducir HTTP, validar entrada y delegar | Servicios, modelos, seguridad web | `JdbcClient`, `java.sql`, `javax.sql` |
| Backend `auth` | Autenticación, sesiones y principal | JDBC y configuración de seguridad | Pantallas Android |
| Backend `service` | Casos de uso y autorización del dominio | JDBC, almacenamiento y modelos | Presentación Android |
| Persistencia Flyway | Esquema, restricciones e índices | PostgreSQL | HTTP o UI |
| FileStorageService | Validación y persistencia de archivos | Sistema de archivos configurado | Control de navegación Android |

## 2. Mapa modular objetivo S7–S8

El objetivo inmediato **no es describir solamente el As-Is**. Es conservar el
mismo despliegue, pero convertir las fronteras observadas en dependencias
explícitas y protegibles:

```text
Android presentation
  → Android data / Retrofit
    → Backend controller
      → Backend service / auth
        → JDBC / PostgreSQL
```

### Límites objetivo

- `presentation` depende de `data`, no de infraestructura backend.
- Android consume HTTP/Retrofit y no conoce PostgreSQL/JDBC.
- `controller` delega en `service`/`auth` y no accede directamente a JDBC/SQL.
- `service`/`auth` concentran reglas de dominio, autorización y acceso a
  persistencia.
- Flyway mantiene esquema, restricciones e índices sin depender de HTTP/UI.
- El almacenamiento de archivos permanece aislado tras `FileStorageService`.

La primera parte automatizada de este objetivo es la restricción
`controller !→ JDBC/SQL directo`, asociada a ADR-002 y ejecutada en CI.

No se presenta como objetivo inmediato extraer microservicios, introducir broker,
Redis o WebSocket. Esas alternativas quedan sujetas a presión medible y a un ADR
posterior.

## 3. Límite crítico de mensajería

Lectura:

```text
ChatController.messages
  → UTrabajoService.requireChatParticipant
  → UTrabajoService.messages
  → JdbcClient
  → message / idx_message_chat_recent
```

Escritura:

```text
ChatController.send
  → UTrabajoService.requireChatParticipant
  → INSERT message + UPDATE chat
```

La comprobación de participación debe ocurrir antes de leer o escribir. La
restricción automática de S8 cubre el salto más peligroso y fácil de verificar:
un controlador no puede importar APIs JDBC/SQL.

## 4. Deuda consciente y evolución

`UTrabajoService` contiene varios dominios y puede dividirse en servicios
internos (`MessagingService`, `JobService`, `ApplicationService`) sin crear
microservicios. Esa división futura no cambia el despliegue y debe conservar
autorización, transacciones y contratos. No se presenta como implementada hoy.

## Relación con decisiones

- [`Decisión de estilo S7`](../../dossier/08-decision-estilo-arquitectonico.md)
  consolida drivers, alternativas y el mapa modular objetivo.
- [`Drivers priorizados`](../../dossier/02-stakeholders-drivers.md) son la base
  para comparar las alternativas.
- [`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md) selecciona monolito
  modular.
- [`ADR-002`](../adr/ADR-002-limites-modulos-dependencias.md) formaliza las
  dependencias permitidas/prohibidas y la restricción `controller → JDBC`.
- [`ADR-003`](../adr/ADR-003-paginacion-historial-mensajes.md) documenta la
  evolución del contrato de historial sin mezclarla con la decisión de módulos.
- [`C3 backend`](../../dossier/07-c4-componentes-backend.md) representa el diseño
  As-Is contrastado con código.
