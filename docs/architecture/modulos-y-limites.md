# Diseño modular y límites as-is

## Regla general

UTrabajo mantiene un despliegue backend único, pero sus dependencias deben fluir
hacia adentro y no saltar capas:

```text
Cliente Android
  → contrato HTTP / controladores
    → servicios de negocio y autenticación
      → persistencia JDBC
        → PostgreSQL 16
```

## Módulos y responsabilidades

| Límite | Responsabilidad | Puede depender de | No debe depender directamente de |
| --- | --- | --- | --- |
| Android `presentation` | Estado y pantallas Compose | `data.UTrabajoRepository`, modelos de UI | PostgreSQL, JDBC, clases internas del backend |
| Android `data` | Sesión local y contrato Retrofit | HTTP, modelos de datos, SharedPreferences | Esquema SQL o controladores Spring |
| Backend `controller` | Traducir HTTP, validar entrada y delegar | Servicios, modelos, seguridad web | `JdbcClient`, `java.sql`, `javax.sql` |
| Backend `auth` | Autenticación, sesiones y principal | JDBC y configuración de seguridad | Pantallas Android |
| Backend `service` | Casos de uso y autorización del dominio | JDBC, almacenamiento y modelos | Presentación Android |
| Persistencia Flyway | Esquema, restricciones e índices | PostgreSQL | HTTP o UI |
| FileStorageService | Validación y persistencia de archivos | Sistema de archivos configurado | Control de navegación Android |

## Mapa modular objetivo inmediato

El objetivo de S7–S8 no cambia el despliegue actual; hace explícitas y protegibles
las fronteras ya observadas en el sistema real. La evolución objetivo inmediata
es conservar el mismo flujo y evitar saltos de capa:

```text
Android presentation
  → Android data / Retrofit
    → Backend controller
      → Backend service / auth
        → JDBC / PostgreSQL
```

No se presenta como objetivo inmediato extraer microservicios, introducir broker,
Redis o WebSocket. Esas alternativas quedan sujetas a presión medible y a un ADR
posterior.

## Límite crítico de mensajería

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

## Deuda consciente

`UTrabajoService` contiene varios dominios y puede dividirse en servicios
internos (`MessagingService`, `JobService`, `ApplicationService`) sin crear
microservicios. Esa división futura no cambia el despliegue y debe conservar
autorización, transacciones y contratos. No se presenta como implementada hoy.

## Relación con decisiones

- [`Decisión de estilo S7`](08-decision-estilo-arquitectonico.md) consolida drivers,
  alternativas y el mapa modular objetivo inmediato.
- [`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md) selecciona monolito
  modular y da origen a la regla CI.
- [`ADR-002`](../adr/ADR-002-limites-modulos-dependencias.md) formaliza las
  dependencias permitidas/prohibidas y la restricción controller → JDBC.
- [`ADR-003`](../adr/ADR-003-paginacion-historial-mensajes.md) documenta la
  evolución del contrato de historial sin mezclarla con la decisión de módulos.
- [`C3 backend`](../../dossier/07-c4-componentes-backend.md) representa el diseño
  as-is contrastado con código.
