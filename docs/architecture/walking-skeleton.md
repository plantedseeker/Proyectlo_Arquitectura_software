# Walking skeleton — UTrabajo

## Decisión y alcance

El walking skeleton elegido es **enviar y recuperar un mensaje asociado a una
oferta laboral**. Es la rebanada vertical mínima alineada con la categoría
confirmada, **Mensajería y mesa de ayuda**, y atraviesa aplicación Android, API
REST, autenticación, reglas de acceso, JDBC/Flyway y PostgreSQL 16.

No se agregan funciones nuevas de producto. Se hace observable y reproducible
el camino que ya existe para responder dos preguntas:

1. ¿Las partes desplegadas están realmente conectadas?
2. Cuando una parte se rompe, ¿podemos ubicar el límite y recuperar el sistema?

## Recorrido y límites

```text
[Persona en Android]
        |
        | HTTP/JSON + Bearer
        v
[Retrofit / UTrabajoRepository]
        |
        | /api/auth, /api/jobs, /api/chats
        v
[Spring Security -> Controllers -> UTrabajoService]
        |
        | JDBC; transacción al enviar mensaje
        v
[PostgreSQL 16: app_user, job_offer, chat, message]
```

La automatización cubre desde Docker hasta la respuesta y persistencia del
mensaje. La interacción de pantalla se conserva como checkpoint manual porque
un script HTTP no demuestra renderizado, navegación ni configuración de red del
emulador.

## Trazabilidad a implementación

| Paso | Elemento real | Evidencia |
| --- | --- | --- |
| Despliegue | `docker-compose.yml` | Salud y dependencia API -> DB |
| Esquema | `backend/src/main/resources/db/migration/` | Tabla `flyway_schema_history` sin fallos |
| Autenticación | `AuthController.kt`, `AuthService.kt` | Login y `/api/auth/me` |
| Oferta | `JobController.kt`, `UTrabajoService.activeJobs` | Una oferta demo activa |
| Conversación | `ChatController.kt`, `createOrGetChat` | Identificador de chat |
| Mensaje | `sendMessage`, tabla `message` | Mismo ID y marcador por API y SQL |
| Límite de confianza | `SecurityConfig.kt` | `/api/chats` rechaza solicitud sin token |
| Cliente móvil | `ApiService.kt`, `UTrabajoRepository.kt`, pantallas de chat | Marcador visible en emulador |

## Cómo demostrarlo al profesor

1. Mostrar este recorrido y explicar que una sola acción cruza todos los
   contenedores y componentes críticos.
2. Ejecutar `scripts/run-walking-skeleton.ps1` desde la raíz.
3. Abrir el JSON generado y señalar revisión Git, versiones y comprobaciones.
4. En Android, iniciar sesión con el estudiante demo y localizar el mismo
   `message_marker`.
5. Elegir un modo de fallo controlado de la matriz, mostrar la señal y recuperar
   el sistema sin borrar datos.

## Qué demuestra y qué no

Demuestra conectividad, migración del esquema, autenticación, autorización
básica, operación principal y persistencia. No demuestra todavía alta
disponibilidad, recuperación ante pérdida de volumen, seguridad para Internet,
entrega en tiempo real ni cumplimiento del umbral de rendimiento. Esos riesgos
permanecen explícitos en la matriz de fallos y en las decisiones S7–S8.
