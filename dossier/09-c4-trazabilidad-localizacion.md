# Trazabilidad C4 y localización del fenómeno medido

Fecha de integración: 2026-09-04 (America/Bogota).

## Procedencia del C4 aprobado

Las vistas `05` a `08` provienen del paquete **Taller practico Utrabajo.zip**,
entregado por el equipo e identificado por el usuario como aprobado por el
profesor. Se conservó su arquitectura as-is y se añadieron propósito, audiencia
y precisiones verificables contra el código.

- Autores registrados en el documento aprobado: Camilo Andrés Romero Palencia,
  Santiago Jaramillo Sánchez y Juan Carlos Barragán Arévalo.
- SHA-256 del ZIP: `50AA20BEB86D3A41D1A4654B693385B1C03147B722DC603DED454AE50E1D8F64`.
- SHA-256 del DOCX interno: `53A51EE7E32FDF1CD4F6062C842B36ED654FC0201BDE5F014C7244180AAC9463`.
- Alcance aprobado: C1 contexto, C2 contenedores, C3 backend y C3 Android. No
  se dibuja nivel 4 porque no lo exige el corte.

## Audiencia y propósito por vista

| Vista | Audiencia principal | Pregunta que responde |
| --- | --- | --- |
| [C1 Contexto](05-c4-contexto.md) | Profesor, auditor y stakeholders | ¿Quién usa o evalúa UTrabajo y con qué objetivo? |
| [C2 Contenedores](06-c4-contenedores.md) | Equipo técnico y auditor | ¿Qué unidades se ejecutan y cómo se comunican? |
| [C3 Backend](07-c4-componentes-backend.md) | Desarrolladores backend y auditor | ¿Qué componentes procesan una solicitud hasta PostgreSQL? |
| [C3 Android](08-c4-componentes-android.md) | Desarrolladores móviles | ¿Cómo llegan las pantallas a la API sin acceder directamente a la base de datos? |

## Trazabilidad de cajas C4 a implementación real

| Elemento C4 | Archivo o módulo real | Evidencia verificable |
| --- | --- | --- |
| UTrabajo | [`README.md`](../README.md) | Describe alcance, actores y arquitectura local |
| Aplicación Android | [`app/`](../app) | Módulo Gradle Android |
| Pantallas Compose | [`presentation/screens/`](../app/src/main/java/com/tab/utrabajo/presentation/screens) | Pantallas de login, ofertas, postulaciones, perfil y chat |
| UTrabajoRepository | [`UTrabajoRepository.kt`](../app/src/main/java/com/tab/utrabajo/data/UTrabajoRepository.kt) | Coordina sesión y llamadas HTTP |
| ApiService/Retrofit | [`ApiService.kt`](../app/src/main/java/com/tab/utrabajo/data/ApiService.kt) | Declara `GET api/chats/{chatId}/messages` |
| API REST | [`backend/`](../backend) | Módulo Spring Boot desplegable |
| Cadena de seguridad | [`SecurityConfig.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/config/SecurityConfig.kt) y [`TokenAuthenticationFilter.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/auth/TokenAuthenticationFilter.kt) | Configura rutas y autentica Bearer token |
| Controladores | [`controller/`](../backend/src/main/kotlin/com/tab/utrabajo/api/controller) | Adaptadores HTTP reales |
| ChatController | [`ChatController.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt) | Expone chats y mensajes y delega al servicio |
| UTrabajoService | [`UTrabajoService.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt) | Autoriza al participante, pagina y consulta mensajes |
| JdbcClient/JDBC | [`UTrabajoService.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt) | Dependencia interna para ejecutar SQL; no es un contenedor |
| PostgreSQL 16 | [`docker-compose.yml`](../docker-compose.yml) | Contenedor desplegable de base de datos |
| Esquema de mensajes | [`V1__relational_schema.sql`](../backend/src/main/resources/db/migration/V1__relational_schema.sql) | Tablas, claves e índices iniciales |
| Índice reciente | [`V2__message_recent_index.sql`](../backend/src/main/resources/db/migration/V2__message_recent_index.sql) | Índice `(chat_id, sent_at DESC, id DESC)` |
| Flyway | [`db/migration/`](../backend/src/main/resources/db/migration) | Tecnología ejecutada por la API para versionar esquema; no es un contenedor |
| Almacenamiento local | [`FileStorageService.kt`](../backend/src/main/kotlin/com/tab/utrabajo/api/service/FileStorageService.kt) | Valida y escribe archivos en el volumen configurado |

## Correcciones al contrastar el dibujo con el código

| Hallazgo | Ajuste realizado | Motivo |
| --- | --- | --- |
| JDBC y Flyway podían parecer contenedores C2 | Se etiquetaron como tecnologías/relaciones y se listaron solo cuatro contenedores | No se despliegan ni ejecutan independientemente de la API |
| Las flechas podían sugerir acceso directo de Android a controladores | C3 ahora muestra primero la cadena de seguridad | Las rutas protegidas pasan por el filtro y `SecurityConfig` |
| El experimento no aparecía en C3 | Se añadió k6 como actor temporal externo | Localiza la entrada medida sin convertir el instrumento en componente del producto |
| El valor S4 podía confundirse con latencia móvil | Se declaró Android fuera del alcance de S4 | k6 llamó a la API directamente; no hubo emulador ni red de Internet |
| Podían inferirse broker, caché, WebSocket o microservicio | No se añadieron esas cajas | No existen en el código as-is; solo son alternativas de S7 |

No se eliminó ningún contenedor real del modelo aprobado. Las precisiones
anteriores corrigen semántica y flujo, no cambian el sistema implementado.

## Frontera del fenómeno medido

La categoría confirmada por el profesor es **Mensajería y mesa de ayuda**. La
operación S4 fue:

```http
GET /api/chats/3fb4ce25-b840-4685-d2ea-53f9a4bdedc6/messages?limit=50&offset=0
Authorization: Bearer <token de un participante>
```

En C2, la frontera medida empieza en la entrada HTTP de la **API REST** y llega
hasta **PostgreSQL**. En C3 backend, el trayecto es:

```text
k6 → seguridad → ChatController.messages
   → UTrabajoService.requireChatParticipant
   → UTrabajoService.messages → JdbcClient
   → PostgreSQL/índice de mensajes → JSON
```

La línea base mide tiempo HTTP de extremo a extremo entre k6 y la API, incluida
la espera por PostgreSQL y la serialización. No mide Android, pantalla,
dispositivo, Internet ni tiempo de percepción humana.

## Dato disponible y localización aún necesaria

### Dato medido en S4

- p95 válidos: 9,451 ms; 8,265 ms; 9,109 ms.
- Mediana de p95 válidos: **9,109 ms**.
- Resultado: 120 solicitudes válidas, 100 % de comprobaciones y 0 % de fallos.
- Topología: k6, API y PostgreSQL 16.14 compartieron Docker Desktop y el mismo
  Acer Predator PH16-71, conectado a corriente.

### Límite de ese dato

k6 entrega el total HTTP, pero por sí solo no reparte el tiempo entre seguridad,
servicio, SQL y serialización. Por eso no sería válido afirmar, con S4 solamente,
que PostgreSQL es el cuello de botella.

### Instrumento y dato de S7

`EXPLAIN (ANALYZE, BUFFERS)` registró autorización en 0,070 ms, página reciente
en 0,112 ms, `OFFSET 50000` en 41,017 ms y cursor equivalente en 0,130 ms. El
offset profundo costó 315,515× el cursor, eligió `Seq Scan` y utilizó buffers y
archivos temporales; reciente y cursor usaron `idx_message_chat_recent`.

Esto localiza costo dentro de **UTrabajoService/JDBC → PostgreSQL**. El tiempo
restante entre el total HTTP y el plan SQL sigue siendo costo combinado de
seguridad, lógica, transporte y serialización; no se reparte sin instrumentación
adicional. No se restan tiempos de S4 y S7 porque provienen de ejecuciones
diferentes.

## Evidencia primaria relacionada

- [Protocolo S4](../experimentos/medicion-escenario-01/README.md)
- [Resultado agregado S4](../experimentos/medicion-escenario-01/resultados/resultado.json)
- [Contexto de máquina, energía y topología](../experimentos/medicion-escenario-01/resultados/contexto.json)
- [Verificación de semilla](../experimentos/medicion-escenario-01/resultados/verificacion-semilla.json)
- [Experimento de localización S7](../experimentos/localizacion-s7/README.md)
- [Planes y resumen S7](../experimentos/localizacion-s7/resultados/README.md)
