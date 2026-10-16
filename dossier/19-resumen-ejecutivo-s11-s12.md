# Resumen ejecutivo — Semanas 11 y 12: crecimiento y distribución

Este documento reúne el recorrido verificable de S11–S12 para que cualquier
integrante pueda presentarlo sin depender de memoria individual. Distingue
resultados medidos, estimaciones de costo, propuestas arquitectónicas y
decisiones humanas pendientes.

## Resultado general

S11 aumentó de forma controlada la carga de la medición M2 de Mensajería: 10,
50, 100, 150 y 200 VU, cuatro corridas por escalón y primera corrida descartada
como calentamiento. No apareció degradación sostenida hasta 200 VU. La primera
señal creciente fue la latencia, pero el p95 mediano máximo fue 58,235 ms, muy
por debajo del umbral prerregistrado de 500 ms, con cero fallos y páginas
correctas.

Se aplicó y midió una caché local de un segundo. La táctica funcionó, registró
3.196 hits y conservó el contrato. El p95 mediano bajó 4,908 %, pero el
throughput mediano bajó 1,866 %; por eso el veredicto es
`partially_supported` y la caché continúa deshabilitada por defecto.

S12 usa esos datos para comparar distribución. La propuesta técnica es
mantener el monolito modular: la evidencia no demuestra saturación,
indisponibilidad independiente ni beneficio que compense microservicios,
broker o caché distribuida. ADR-004 permanece pendiente de ratificación humana
en el mini-comité 2.

## Evidencias exigidas

| Evidencia | Resultado | Dónde se comprueba |
| --- | --- | --- |
| Crecimiento con carga aumentada | 20 corridas entre 10 y 200 VU, sin degradación sostenida | [resultado consolidado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json), [contexto](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json) y [huellas](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/SHA256SUMS.txt) |
| Dónde y cómo se degrada | creció primero la latencia; no hubo saturación ni límite atribuible | [análisis de crecimiento](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md) |
| Matriz de fallos y resiliencia | modos observados, descartados y tácticas justificadas | [matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md) y [matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md) |
| Táctica antes/después | caché funcional, beneficio parcial y no adopción | [resultado explicado](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md), [datos de comparación](../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json) y [huellas](../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/SHA256SUMS.txt) |
| Capturas de ejecución | inicio y cierre del crecimiento y de la comparación con caché | [índice visual y comprobación SHA-256](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/README.md) |
| Distribución y costo | cuatro alternativas, dimensiones completas y órdenes de magnitud | [alternativas de distribución](../docs/architecture/alternativas-distribucion-s12.md) |
| ADR 4 | propuesta de mantener Mensajería dentro del monolito modular | [ADR-004](../docs/adr/ADR-004-distribucion-mensajeria.md) |
| Auditoría de IA | patrones aceptados, rechazados y supuestos pendientes | [auditoría de microservicios propuestos por IA](../docs/architecture/auditoria-ia-microservicios-s12.md) |
| Segunda fitness function | regla, umbral cero y fallo de CI implementados | [regla ejecutable](../scripts/check_distribution_decision.py) |
| Mini-comité 2 | formato listo; participantes y veredicto pendientes | [formato del mini-comité](../docs/architecture/mini-comite-s12.md) |

## Evidencia visual de ejecución

Las imágenes son evidencia complementaria: permiten mostrar rápidamente la
ejecución en terminal, mientras que los JSON, logs, contexto y archivos
`SHA256SUMS.txt` conservan la evidencia primaria y reproducible.

1. [Construcción, semilla e inicio del crecimiento](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/02-crecimiento-construccion-semilla-inicio.png).
2. [Cierre del crecimiento, ruta del resultado y huellas](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/01-crecimiento-cierre-y-huellas.png).
3. [Construcción y restauración de la semilla para probar la caché](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/03-cache-construccion-y-semilla.png).
4. [Cierre de la comparación antes/después, ruta y huellas](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/04-cache-cierre-y-huellas.png).

El [índice de capturas](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/README.md)
muestra las cuatro imágenes dentro de una sola página, explica qué comprueba
cada una y registra su SHA-256.

## Semana 11 — método, datos y conclusión

### Método

- extensión controlada de `MSG-PERF-01`, no escenario nuevo inventado;
- semilla verificada de 1.000 conversaciones y 289.000 mensajes;
- conversación extrema de 100.000 mensajes;
- escalones 10 → 50 → 100 → 150 → 200 VU;
- 4 solicitudes por VU y cuatro corridas por escalón;
- corrida 1 descartada y corridas 2–4 válidas;
- degradación sostenida: al menos dos de tres corridas válidas incumplen;
- commit, máquina, energía, versiones, respuestas y hashes registrados.

### Datos por escalón

| VU | Solicitudes/corrida | p95 mediano | Throughput mediano | Degradación sostenida |
| ---: | ---: | ---: | ---: | --- |
| 10 | 40 | 8,963 ms | 177,431 req/s | No |
| 50 | 200 | 18,768 ms | 813,531 req/s | No |
| 100 | 400 | 39,258 ms | 1.395,339 req/s | No |
| 150 | 600 | 41,060 ms | 1.879,413 req/s | No |
| 200 | 800 | 58,235 ms | 2.303,017 req/s | No |

### Conclusión

La latencia creció, pero el throughput también y todas las respuestas fueron
correctas. No se atribuye cuello de botella a API, PostgreSQL ni k6 porque las
ráfagas fueron cortas y la telemetría tuvo baja resolución. La conclusión
válida es: **sin límite observado hasta 200 VU; no extrapolar más allá**.

## Táctica antes/después

Se descartaron retry, timeout y circuit breaker porque no hubo fallos
transitorios, espera fuera de umbral ni dependencia caída. Se eligió una caché
local limitada para responder a la única tendencia observada: lecturas
repetidas y crecimiento de latencia.

| Métrica mediana | Antes | Después | Cambio | Regla |
| --- | ---: | ---: | ---: | --- |
| p95 | 58,235 ms | 55,377 ms | -4,908 % | Cumple |
| Throughput | 2.303,017 req/s | 2.260,038 req/s | -1,866 % | No cumple |
| Fallos | 0 | 0 | sin cambio | Cumple |
| Páginas válidas | 100 % | 100 % | sin cambio | Cumple |

La variación posterior se solapa con el rango base. El experimento demuestra
que la caché fue ejercitada, no que aporte una mejora material reproducible.

## Semana 12 — decisión de distribución

### Costos relativos

`C0` representa el costo completo de operar hoy una API y una base. Es una
unidad comparativa; no una factura inventada.

| Alternativa | Costo | Orden | Estado propuesto |
| --- | ---: | ---: | --- |
| Monolito modular | `1 C0` | `10^0` | Seleccionada |
| Réplicas del monolito | `2–3 C0` | `10^0` | Primer paso si API limita |
| Microservicio síncrono | `4–8 C0` | `10^0–10^1` | Diferido |
| Eventos + proyecciones | `8–20 C0` | `10^1` | No justificado |

La comparación incluye rendimiento, disponibilidad, complejidad, operación,
escalabilidad, costo y reversibilidad. La distribución no se rechaza para
siempre; se condiciona a disparadores medibles.

### ADR-004

La propuesta conserva Mensajería dentro del monolito modular y mantiene:

- límites de contextos y contrato pequeño con Ofertas;
- una base y un despliegue mientras no exista necesidad contraria;
- caché experimental apagada;
- re-medición antes de introducir infraestructura distribuida;
- ruta reversible mediante spike y comparación de alternativas.

El ADR no está “aprobado”: requiere mini-comité 2. El repositorio no fabrica
nombres, votos ni ratificaciones.

### Fitness function

La segunda fitness function rompe CI si:

- aparece un servicio Compose diferente de `db`, `api` o `k6`;
- se agrega Kafka, RabbitMQ, Redis, Pulsar o NATS;
- el backend incorpora sus dependencias cliente;
- la caché experimental queda habilitada por defecto.

Umbral: **cero violaciones**. La regla obliga a actualizar ADR-004 antes de
cambiar la topología, no prohíbe una re-decisión sustentada.

### Auditoría de IA

La propuesta de microservicios de IA fue tratada como hipótesis. Se aceptaron
contratos, métricas y trazabilidad; se rechazaron extracción, broker, retry,
circuit breaker y caché distribuida porque los datos no muestran los fallos que
justificarían esos costos.

## Secuencia Git demostrable

```text
73895be  protocolo e instrumento confirmados
    <
8bc6059  resultados de crecimiento sin interpretación acomodada
    <
46655bc  análisis y selección de táctica
    <
0849dea  táctica y protocolo antes/después confirmados
    <
f916213  resultados antes/después
    <
64cbe3b  veredicto y evidencia visual
    <
ADR-004 propuesto → mini-comité 2 pendiente → veredicto humano pendiente
```

## Recorrido para mostrárselo al profesor

1. Abrir este resumen.
2. Abrir el [índice de capturas](../experimentos/crecimiento-mensajeria/evidencias-ejecucion/README.md) y mostrar el inicio y cierre de ambas mediciones.
3. Mostrar el [resultado del crecimiento](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json) y la tabla de escalones.
4. Abrir las [matrices de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md) y [resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md).
5. Mostrar el [resultado antes/después](../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json) y el veredicto parcial.
6. Abrir la [tabla de alternativas y costos](../docs/architecture/alternativas-distribucion-s12.md).
7. Mostrar [ADR-004](../docs/adr/ADR-004-distribucion-mensajeria.md), especialmente consecuencias y disparadores.
8. Ejecutar las dos fitness functions.
9. Aclarar que el mini-comité todavía debe emitir el veredicto humano.

Comandos breves:

```powershell
python .\scripts\check_architecture.py
python .\scripts\check_distribution_decision.py
python -m unittest discover -s .\scripts\tests -v
```

No es necesario repetir las 24 corridas durante la exposición; los resultados,
logs, contexto y hashes ya están conservados. Si el profesor pide
reproducibilidad y hay tiempo/Docker, los runners están documentados en el
README del experimento.

## Defensa individual de 7–10 minutos

| Tiempo | Contenido |
| ---: | --- |
| 0:00–1:00 | pregunta, hipótesis y relación con M2 |
| 1:00–2:30 | protocolo, escalones, controles e invalidaciones |
| 2:30–4:00 | datos y ausencia de degradación sostenida |
| 4:00–5:15 | táctica, antes/después y veredicto parcial |
| 5:15–7:00 | alternativas de distribución y costo |
| 7:00–8:30 | ADR-004, consecuencias y reversibilidad |
| 8:30–9:30 | fitness function y auditoría de IA |
| 9:30–10:00 | restricción nueva y re-decisión |

Explicación compacta:

> Aumentamos la medición M2 por escalones y no observamos degradación sostenida
> hasta 200 VU. La latencia creció, pero el sistema mantuvo 100 % de páginas
> válidas, cero errores y throughput creciente. Probamos una caché local: bajó
> ligeramente el p95, pero también el throughput, así que no la adoptamos. Con
> esos datos comparamos cuatro topologías y sus costos. ADR-004 propone mantener
> el monolito modular y una fitness function impide distribuirlo accidentalmente.
> La decisión sigue abierta a re-evaluación y el mini-comité debe ratificarla.

## Estado honesto de cierre

La evidencia técnica de S11 está completa. Los artefactos técnicos de S12 están
preparados y son ejecutables. Permanecen pendientes actividades humanas que no
se pueden anticipar: mini-comité 2, veredicto final, revisión del PR y defensa
individual. Después del comité se actualizará ADR-004 con el resultado real.

