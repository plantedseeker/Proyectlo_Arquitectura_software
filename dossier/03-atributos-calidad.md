# 03 — Atributos de calidad

## Matriz priorizada

Escala: impacto y urgencia de 1 (bajo) a 5 (alto). La prioridad es el producto
`impacto × urgencia`; los empates se resuelven por riesgo para el usuario.

| Orden | Atributo | Impacto | Urgencia | Puntaje | Justificación |
| ---: | --- | ---: | ---: | ---: | --- |
| 1 | Rendimiento | 5 | 5 | 25 | Fenómeno central de conversaciones con 10–100.000 mensajes |
| 2 | Seguridad/confidencialidad | 5 | 4 | 20 | Los chats contienen información entre candidato y empresa |
| 3 | Confiabilidad/integridad | 4 | 4 | 16 | No deben perderse, duplicarse ni desordenarse mensajes |
| 4 | Modificabilidad | 4 | 3 | 12 | La migración futura a Internet no debe rehacer Android |
| 5 | Disponibilidad | 3 | 3 | 9 | Importante a futuro, pero el checkpoint actual es local |

## RNF convertidos en escenarios verificables

### MSG-PERF-01 — Abrir conversación extrema

- Fuente: estudiante autenticado.
- Estímulo: solicita los últimos 50 mensajes de una conversación con 100.000.
- Artefacto: API de mensajes, índice y PostgreSQL 16.
- Entorno: ejecución local; k6, API y DB en el mismo equipo; 10 VU.
- Respuesta: la API autoriza, consulta una página y devuelve 50 mensajes válidos.
- Medida: 40 solicitudes por corrida, `p95 <= 500 ms`, cero fallos; cuatro
  corridas, primera descartada y mediana de las tres válidas.
- Prioridad: principal y bloqueante para semana 4.

### MSG-SEC-01 — Aislamiento de conversación

- Fuente: usuario autenticado que no pertenece al chat.
- Estímulo: intenta consultar o enviar mensajes usando un `chatId` ajeno.
- Artefacto: API de chats y PostgreSQL.
- Entorno: operación normal.
- Respuesta: la API no entrega ni modifica mensajes.
- Medida: HTTP 403 en el 100 % de los intentos y cero filas expuestas.

### MSG-REL-01 — Orden estable

- Fuente: participantes de una conversación.
- Estímulo: escriben mensajes cercanos en el tiempo y solicitan la última página.
- Artefacto: tabla `message` y endpoint paginado.
- Entorno: operación normal.
- Respuesta: se devuelven hasta 50 mensajes en orden cronológico visible.
- Medida: sin identificadores duplicados; orden no decreciente por fecha e ID.

### MSG-MOD-01 — Evolución sin romper Android

- Fuente: equipo de desarrollo.
- Estímulo: introduce límite y desplazamiento en la lectura de mensajes.
- Artefacto: API, cliente Retrofit y pruebas.
- Entorno: integración continua.
- Respuesta: el cliente existente usa valores predeterminados y compila.
- Medida: pruebas backend, pruebas Android, lint y APK en verde.

## Registro de escenarios sugeridos por IA y corregidos por el equipo

| Propuesta de IA | Problema detectado | Reformulación adoptada |
| --- | --- | --- |
| “El chat debe ser rápido” | No define carga, operación ni umbral | MSG-PERF-01: 100.000 mensajes, últimos 50, 10 VU, p95 ≤ 500 ms |
| “Los mensajes deben ser seguros” | No define atacante ni resultado | MSG-SEC-01: no participante obtiene 403 y cero datos |
| “Los mensajes deben llegar ordenados” | No define orden ni comprobación | MSG-REL-01: página sin duplicados y orden por fecha/ID |
| “La solución debe escalar” | Mezcla capacidad, costo y disponibilidad | Se limita al fenómeno medible local; Internet reabre la decisión |

## Mapa atributo–decisión

| Atributo | Decisión arquitectónica | Evidencia |
| --- | --- | --- |
| Rendimiento | Página máxima 100, escenario de 50 e índice `(chat_id, sent_at DESC, id DESC)` | k6 y `V2__message_recent_index.sql` |
| Seguridad | Autorización por participación antes de la consulta | `requireChatParticipant` y prueba de integración |
| Confiabilidad | PK/FK, transacción de envío y orden estable | Migraciones y pruebas backend |
| Modificabilidad | Parámetros HTTP opcionales y API entre Android/DB | Compilación Android y CI |
| Reproducibilidad | Imagen k6 fijada, Docker Compose y semilla determinista | `/experimentos/medicion-escenario-01` |
