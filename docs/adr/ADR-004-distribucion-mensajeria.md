# ADR-004 — Mantener Mensajería en el monolito modular

- Fecha: 2026-10-16
- Estado: **Propuesta respaldada por S11; pendiente de mini-comité 2**
- Categoría: Mensajería y mesa de ayuda
- Protege: rendimiento, disponibilidad, simplicidad operativa, reversibilidad y costo

## Contexto basado en evidencia

S11 reejecutó la medición M2 con escalones de 10, 50, 100, 150 y 200 VU. Se
registraron 20 corridas, contexto, semilla, respuestas, telemetría y huellas.
No apareció degradación sostenida hasta 200 VU: todas las solicitudes válidas
respondieron HTTP 200, devolvieron páginas correctas y el throughput siguió
creciendo. El p95 mediano a 200 VU fue 58,235 ms frente al umbral de 500 ms.

Después se midió una caché local TTL sobre el mismo escalón. Conservó la
corrección y produjo 3.196 hits; el p95 mediano bajó 4,908 %, pero el throughput
mediano bajó 1,866 %. Los rangos antes/después se solapan y el veredicto fue
`partially_supported`, por lo que la táctica permanece deshabilitada.

La evidencia no muestra saturación, errores, necesidad de disponibilidad
independiente ni un límite atribuible a Mensajería que requiera distribución.

## Alternativas consideradas

1. conservar el monolito modular actual;
2. replicar horizontalmente el monolito completo;
3. extraer un microservicio síncrono de Mensajería;
4. separar Mensajería mediante eventos, broker y proyecciones.

La comparación completa de rendimiento, disponibilidad, complejidad,
operación, escalabilidad, reversibilidad y costo está en
[`alternativas-distribucion-s12.md`](../architecture/alternativas-distribucion-s12.md).
El costo relativo estimado va de `1 C0` (`10^0`) para el estado actual a
`8–20 C0` (`10^1`) para la alternativa orientada a eventos. Son órdenes de
magnitud comparativos, no precios ni mediciones productivas. La matriz separa
además infraestructura, operación y coordinación/Conway; esta última aumenta
cuando se crean fronteras de propiedad y contratos remotos que el único equipo
actual tendría que coordinar consigo mismo.

## Decisión propuesta

Mantener Mensajería dentro del **monolito modular**, con límites lógicos,
contratos pequeños y dependencias verificadas por CI.

- No extraer microservicios en S12.
- No introducir broker, proyección duplicada, base por servicio ni caché
  distribuida.
- Mantener la caché experimental deshabilitada por defecto.
- Conservar el contrato lógico Mensajería → Ofertas de ADR-003 sin hacer
  self-HTTP dentro del mismo proceso.
- Medir de nuevo antes de distribuir; una futura necesidad debe identificar el
  componente limitante y comparar al menos la réplica completa con la
  extracción de un contexto.

## Consecuencias positivas

- no se paga complejidad operativa sin un fallo que la justifique;
- se evita agregar latencia de red al flujo actual;
- se conserva consistencia fuerte en una sola base;
- despliegue, respaldo y diagnóstico continúan siendo simples;
- los límites existentes mantienen una ruta reversible hacia extracción.

## Consecuencias negativas

- API y base siguen compartiendo destino de despliegue y fallos;
- Mensajería no puede escalarse independientemente;
- una caída total de la API o la base afecta todos los contextos;
- si aparece un límite real, la separación requerirá otro spike, migración de
  datos y revisión de contratos.

## Reversibilidad

La decisión es reversible porque no elimina los bounded contexts ni sus
contratos. Ante un disparador se puede:

1. repetir el experimento con carga sostenida y múltiples chats;
2. atribuir el límite entre generador, API y PostgreSQL;
3. probar primero réplica del monolito si el límite es de API;
4. construir un spike de extracción con contrato y propiedad de datos;
5. comparar costo y disponibilidad antes de migrar tráfico.

No se aceptará una migración irreversible sin plan de retorno y reconciliación.

## Supuestos y condiciones de revisión

La propuesta supone un solo equipo de producto, una topología de despliegue y
que no existe requisito vigente de continuidad de Mensajería durante una caída
de otros contextos. Revisar si ocurre cualquiera de estas condiciones:

- p95 > 500 ms en al menos dos de tres corridas comparables;
- errores, páginas inválidas o throughput estancado con causa atribuida;
- necesidad contractual de disponibilidad independiente;
- equipos o ciclos de despliegue realmente independientes;
- costo medido de escalar todo el monolito mayor que extraer Mensajería.

## Fitness function que protege la decisión

`scripts/check_distribution_decision.py` impide introducir silenciosamente un
segundo servicio de aplicación, broker/Redis o activar por defecto la caché
experimental. El umbral es **cero violaciones**. Cualquier violación termina con
código 1 y rompe el job backend de CI. La regla se puede cambiar solo junto con
una revisión explícita de este ADR.

En paralelo, `DatabaseIsolationArchitectureTests` usa ArchUnit para proteger la
frontera interna del monolito: controladores sin JDBC/SQL, persistencia limitada
a los paquetes autorizados y ausencia de ciclos entre paquetes superiores. Así
la decisión no depende solo de una convención documental.

## Auditoría de IA

La propuesta de IA de extraer servicios, base por servicio, broker, retry,
circuit breaker y caché distribuida fue auditada contra los datos de S11. Se
aceptaron contratos y observabilidad; se rechazó introducir infraestructura sin
señal causal. Véase
[`auditoria-ia-microservicios-s12.md`](../architecture/auditoria-ia-microservicios-s12.md).

## Trazabilidad

- [`Prerregistro S11`](../../experimentos/crecimiento-mensajeria/00-preregistro.md)
- [`Resultado de crecimiento`](../../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json)
- [`Análisis de crecimiento`](../../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md)
- [`Matriz de fallos`](../../experimentos/crecimiento-mensajeria/03-matriz-fallos.md)
- [`Matriz de resiliencia`](../../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md)
- [`Resultado antes/después`](../../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md)
- [`Comparación estructurada`](../../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json)
- [`Gráficas reproducibles`](../../experimentos/crecimiento-mensajeria/graficas/README.md)
- [`Trazabilidad del prerregistro`](../architecture/trazabilidad-prerregistro-s11.md)
- [`Fitness function con ArchUnit`](../../backend/src/test/kotlin/com/tab/utrabajo/api/architecture/DatabaseIsolationArchitectureTests.kt)
- [`ADR-001 — monolito modular`](ADR-001-limites-modulo-mensajeria.md)
- [`ADR-003 — integración Mensajería → Ofertas`](ADR-003-integracion-contextos.md)

## Confirmación requerida

Esta es una propuesta técnica respaldada por evidencia, no un veredicto humano.
El mini-comité 2 debe registrarla como `RATIFICADA`, `AJUSTADA` o
`RECONSIDERADA`, con participantes reales y razones. Ese resultado se añadirá
después del comité y no se anticipa en este ADR.
