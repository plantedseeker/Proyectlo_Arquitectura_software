# Localización S7 — consulta del historial de mensajería

## Objetivo

Localizar el costo dentro de la frontera `UTrabajoService/JDBC → PostgreSQL`
para la lectura de mensajes. El instrumento no sustituye k6: S4 midió el tiempo
HTTP completo y S7 observa planes SQL internos.

## Pregunta

¿Cómo cambia el trabajo de PostgreSQL al obtener 50 mensajes de la conversación
de 100.000 elementos mediante:

1. la página reciente usada en S4 (`OFFSET 0`);
2. una página profunda (`OFFSET 50000`);
3. una página equivalente mediante cursor compuesto `(sent_at, id)`?

## Instrumento

PostgreSQL 16 ejecuta `EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)` para la
comprobación de participación y las tres formas de lectura. El script captura el
plan completo, tiempo de planificación, tiempo de ejecución, filas, buffers,
índices usados, revisión Git, máquina y topología local.

Desde la raíz, con Docker Desktop abierto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-s7-localization.ps1
```

Si la semilla ya está cargada puede evitarse su recarga:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-s7-localization.ps1 -SkipSeed
```

Resultado generado:

```text
experimentos/localizacion-s7/resultados/localizacion.json
```

## Condiciones y controles

- Se usa la misma conversación determinista de S4:
  `3fb4ce25-b840-4685-d2ea-53f9a4bdedc6`.
- Debe contener exactamente 100.000 mensajes.
- Las tres consultas de mensajes piden 50 elementos y usan el mismo orden
  `(sent_at DESC, id DESC)`.
- El cursor se obtiene en la posición 49.999 y la consulta devuelve los 50
  elementos siguientes; esa consulta auxiliar no se incluye en su tiempo.
- El generador del instrumento y PostgreSQL comparten el equipo físico. La API
  queda fuera de este instrumento SQL; su costo completo ya fue medido por k6.
- Se capturan versión de PostgreSQL, revisión Git, host, CPU, memoria y estado de
  energía cuando Windows lo expone.

## Invalidaciones

El resultado no se acepta si la semilla no tiene 100.000 mensajes en la
conversación extrema, alguna consulta no devuelve un plan JSON, los contenedores
se reinician durante la observación, cambia el SQL entre alternativas, o se
compara una cantidad distinta de filas.

## Cómo leer el resultado

### Datos medidos

Solo son datos medidos los campos del JSON: `execution_time_ms`,
`planning_time_ms`, filas, bloques/buffers, nodos e índices del plan. El plan
`participant_authorization` corresponde a la primera consulta real del servicio;
los otros tres corresponden a estrategias de lectura. La comparación debe citar
el archivo y el commit capturado.

### Interpretación permitida

Si el plan profundo aumenta filas recorridas, buffers o tiempo respecto de
`OFFSET 0`, existe evidencia de que el costo crece con la profundidad. Si el
cursor reduce ese trabajo bajo las mismas condiciones, respalda considerarlo
como alternativa para navegación profunda.

### Supuestos y límites

- Una ejecución local no representa Internet ni producción.
- Un plan SQL no reparte el tiempo de autenticación, lógica, transporte y
  serialización de la API.
- Una sola observación es evidencia de localización, no una distribución de
  latencia. Para una decisión de rendimiento definitiva deben repetirse planes
  y medir el endpoint con la alternativa implementada.
- El resultado no justifica por sí solo microservicios, Redis o WebSocket.

## Resultado observado — 2026-09-04

| Escenario | Ejecución | Evidencia del plan |
| --- | ---: | --- |
| Autorizar participante | 0,070 ms | `chat_pkey`, 3 buffers hit |
| Página reciente `OFFSET 0` | 0,112 ms | `idx_message_chat_recent`, 13 buffers hit |
| Página profunda `OFFSET 50000` | 41,017 ms | `Seq Scan`, 24.140 buffers hit/read y trabajo temporal |
| Cursor equivalente | 0,130 ms | `idx_message_chat_recent`, 12 buffers hit |

El offset profundo tardó **315,515×** el cursor en esta observación. Los datos y
planes completos están en [`resultados/localizacion.json`](resultados/localizacion.json);
la interpretación auditada está en [`resultados/README.md`](resultados/README.md).

La primera captura precede al commit que incorporará el instrumento. Debe
repetirse después de ese commit para que el `git_revision` final lo incluya.

## Relación con decisiones

- [`ADR-001`](../../docs/adr/ADR-001-limites-modulo-mensajeria.md): límites del
  módulo y permanencia en el monolito modular.
- [`ADR-002`](../../docs/adr/ADR-002-paginacion-historial-mensajes.md): estrategia
  de paginación e índice.
- [`Trazabilidad C4`](../../dossier/09-c4-trazabilidad-localizacion.md): frontera
  exacta en C2/C3.
