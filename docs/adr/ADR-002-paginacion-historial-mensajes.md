# ADR-002 — Estrategia de paginación del historial de mensajes

- Fecha: 2026-09-04
- Estado: **Propuesta sustentada por MSG-LOC-01; pendiente de ratificación humana**
- Categoría: Mensajería y mesa de ayuda
- Protege: rendimiento, consistencia de navegación y evolución del contrato

## Contexto

El endpoint actual acepta `limit` entre 1 y 100 y `offset` hasta 1.000.000. La
consulta ordena por `(sent_at DESC, id DESC)`, utiliza un índice compuesto y
devuelve la página en orden cronológico. El cliente Android solicita la página
por defecto de 50 mensajes y no expone hoy navegación profunda.

S4 midió exclusivamente `limit=50&offset=0` sobre una conversación de 100.000
mensajes. Su mediana de p95 fue 9,109 ms; no permite extrapolar páginas
profundas. MSG-LOC-01 observó 0,112 ms para la consulta reciente, 41,017 ms
para `OFFSET 50000` y 0,130 ms para el cursor equivalente. El offset profundo
costó 315,515× el cursor bajo las condiciones registradas.

## Decisión propuesta

Conservar `limit/offset` para compatibilidad y para la página reciente que usa
el producto. Mantener el índice `(chat_id, sent_at DESC, id DESC)` y el límite
máximo de página. No promocionar `offset` profundo como garantía de rendimiento.

Cuando se implemente una necesidad real de cargar historial profundo, añadir un
contrato de cursor opaco basado en `(sent_at, id)` y migrar Android de forma
compatible antes de retirar `offset`. MSG-LOC-01 ya demuestra costo material en
la profundidad observada; falta validar el nuevo contrato bajo carga HTTP.

## Alternativas consideradas

### A. Devolver el historial completo

- **Beneficio:** contrato muy simple.
- **Costo:** transferencia, memoria y serialización crecen con la conversación.
- **Seguridad/operación:** amplifica abuso y consumo por solicitud.
- **Reversibilidad:** alta técnicamente, pero rompe clientes al paginar después.
- **Decisión:** descartada.

### B. `LIMIT/OFFSET` con índice compuesto — seleccionada para el uso actual

- **Beneficio:** ya implementada, fácil de probar, compatible con Android y
  rápida para `offset=0` bajo S4.
- **Costo:** PostgreSQL puede recorrer y descartar filas al profundizar.
- **Seguridad:** deben mantenerse límite de página y autorización por chat.
- **Operación:** no incorpora estado ni infraestructura nueva.
- **Reversibilidad:** alta mediante adición de un endpoint/campo de cursor.

### C. Paginación por cursor `(sent_at, id)`

- **Beneficio:** trabajo acotado y navegación más estable mientras llegan
  mensajes nuevos.
- **Costo:** contrato y cliente más complejos; no permite saltar naturalmente a
  una página numérica.
- **Seguridad:** el cursor debe validarse y tratarse como dato no confiable; es
  preferible codificarlo de forma opaca.
- **Operación:** usa el mismo PostgreSQL e índice; no agrega servicio.
- **Reversibilidad:** alta mientras coexista con `offset`; menor si se retira el
  contrato anterior demasiado pronto.
- **Decisión:** alternativa de migración condicionada a evidencia y necesidad.

## Consecuencias

### Positivas

- El producto no cambia antes de tener resultado causal y caso de uso real.
- Se conserva compatibilidad y el buen comportamiento de la página reciente.
- Queda definida una ruta concreta y reversible hacia cursor.

### Negativas

- El endpoint continúa aceptando offsets que pueden ser costosos.
- Habrá un período con dos estrategias si se migra.
- La decisión debe reabrirse; no resuelve por adelantado navegación profunda.

## Reversibilidad y disparadores

Reabrir el ADR si Android incorpora “cargar mensajes anteriores”, si el p95 de
esa operación incumple su umbral, si `EXPLAIN` muestra crecimiento relevante de
filas/buffers con el offset, o si conversaciones superan 100.000 mensajes.

La migración reversible agrega cursor sin eliminar `offset`, publica contrato y
pruebas, cambia Android, observa adopción y solo después depreca el parámetro
anterior.

## Evidencia

- [`UTrabajoService.messages`](../../backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt)
- [`ChatController`](../../backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt)
- [`Índice V2`](../../backend/src/main/resources/db/migration/V2__message_recent_index.sql)
- [`Resultado S4`](../../experimentos/medicion-escenario-01/resultados/resultado.json)
- [`Instrumento MSG-LOC-01`](../../experimentos/localizacion-s7/README.md)

## Confirmación requerida

El equipo debe revisar el JSON y ratificar o ajustar la decisión. Además debe
repetir la captura después del commit que contiene el instrumento, porque la
primera ejecución registró la revisión base previa. No se rellenarán resultados
estimados como si fueran mediciones.
