# Crítica de una propuesta arquitectónica generada por IA

Fecha: 2026-09-04.

## Propuesta recibida

Ante el dominio de mensajería y una conversación sintética de 100.000 mensajes,
la IA sugirió como solución general extraer un microservicio de chat, usar un
broker para mensajes, Redis como caché, WebSocket para tiempo real y paginación
por cursor.

La propuesta se usa como objeto de crítica. No se acepta como evidencia por el
solo hecho de haber sido generada ni se presenta como código implementado.

## Evaluación crítica

| Parte de la sugerencia | Veredicto | Razón y evidencia |
| --- | --- | --- |
| Identificar mensajería como frontera observable | Aceptada | Coincide con la categoría confirmada y el flujo C3 real |
| Mantener tamaño máximo de página e índice compuesto | Aceptada | Existe en código y S4 respalda la página reciente bajo condiciones declaradas |
| Comparar cursor con `OFFSET` profundo | Aceptada como experimento | MSG-LOC-01 observó 41,017 ms con `OFFSET 50000` y 0,130 ms con cursor; la captura debe repetirse sobre el commit del instrumento |
| Migrar inmediatamente Android a cursor | Modificada | Se propone compatibilidad gradual y solo si existe navegación profunda y evidencia |
| Extraer ya un microservicio de chat | Rechazada por ahora | S4 no muestra incumplimiento ni necesidad de escalado independiente; aumenta operación y seguridad distribuida |
| Incorporar Redis como caché | Rechazada | No se midió repetición que justifique caché ni se definió invalidación de mensajes |
| Incorporar broker | Rechazada por ahora | El caso medido es lectura; no hay evidencia de volumen asíncrono o desacoplamiento necesario |
| Adoptar WebSocket | Fuera de alcance actual | Es una decisión de entrega en tiempo real, no una corrección automática de lectura histórica |
| Kubernetes para despliegue | Rechazada | Contradice el alcance local actual y no responde a una presión medida |

## Qué se cambió después de revisar código y evidencia

La sugerencia original mezclaba seis decisiones y asumía una causa. Se dividió
en dos ADR independientes: límites/despliegue y paginación. Además se ejecutó
`EXPLAIN (ANALYZE, BUFFERS)` para localizar el costo antes de cambiar contrato o
infraestructura.

## Riesgos de aceptar la propuesta sin crítica

- Confundir un volumen de datos sintético con una necesidad de distribución.
- Aumentar superficie de ataque, secretos, redes y puntos de falla.
- Ocultar costos de consistencia, reintentos y observabilidad.
- Usar caché sin estrategia de invalidación en un dominio mutable.
- Romper el cliente móvil sin migración compatible.
- Atribuir a PostgreSQL un cuello de botella no demostrado por k6.

## Declaración del equipo pendiente

Antes de fusionar, los tres integrantes deben revisar esta tabla y registrar en
el PR si conservan o ajustan cada veredicto. La IA produjo un borrador; la
responsabilidad de la decisión y de su defensa es humana.

## Evidencia relacionada

- [`Comparación de alternativas`](alternativas-s7.md)
- [`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md)
- [`ADR-002`](../adr/ADR-002-paginacion-historial-mensajes.md)
- [`Resultado S4`](../../experimentos/medicion-escenario-01/resultados/resultado.json)
- [`Instrumento S7`](../../experimentos/localizacion-s7/README.md)
