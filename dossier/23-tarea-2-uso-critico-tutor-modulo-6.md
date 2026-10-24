# Tarea 2 — Uso crítico del Tutor de Arquitectura — Módulo 6 (Corte 3)

> **Objetivo de este archivo:** permitir que el profesor verifique en pocos clics el avance A1–A6 del Módulo 6 y vea cómo las recomendaciones del Tutor fueron **contrastadas, aceptadas, rechazadas o condicionadas** con evidencia real del repositorio.
>
> Este documento **no inventa actividades humanas**. En particular, el mini-comité 2 aún no tiene un veredicto real registrado; por eso A5 queda como **parcial** hasta que esa actividad ocurra.

## Navegación rápida

| Evidencia | Enlace directo |
| --- | --- |
| Resumen ejecutivo S11–S12 | [`dossier/19-resumen-ejecutivo-s11-s12.md`](19-resumen-ejecutivo-s11-s12.md) |
| Checklist verificable S11–S12 | [`dossier/20-checklist-semanas-11-12.md`](20-checklist-semanas-11-12.md) |
| Experimento de crecimiento S11 | [`experimentos/crecimiento-mensajeria/`](../experimentos/crecimiento-mensajeria/) |
| Prerregistro S11 | [`00-preregistro.md`](../experimentos/crecimiento-mensajeria/00-preregistro.md) |
| Contexto reproducible del experimento | [`contexto.json`](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json) |
| Resultado consolidado S11 | [`resultado.json`](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json) |
| Análisis de crecimiento | [`02-analisis-crecimiento.md`](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md) |
| Matriz de fallos | [`03-matriz-fallos.md`](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md) |
| Matriz de resiliencia | [`04-matriz-resiliencia.md`](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md) |
| Protocolo de la táctica | [`05-protocolo-tactica-cache.md`](../experimentos/crecimiento-mensajeria/05-protocolo-tactica-cache.md) |
| Resultado antes/después de la caché | [`06-resultado-tactica-cache.md`](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md) |
| JSON comparación antes/después | [`resultado-comparacion.json`](../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json) |
| Alternativas y costos de distribución | [`alternativas-distribucion-s12.md`](../docs/architecture/alternativas-distribucion-s12.md) |
| ADR-004 | [`ADR-004-distribucion-mensajeria.md`](../docs/adr/ADR-004-distribucion-mensajeria.md) |
| Auditoría de microservicios propuestos por IA | [`auditoria-ia-microservicios-s12.md`](../docs/architecture/auditoria-ia-microservicios-s12.md) |
| Mini-comité 2 | [`mini-comite-s12.md`](../docs/architecture/mini-comite-s12.md) |
| Fitness function documentada | [`fitness-function-distribucion-s12.md`](../docs/architecture/fitness-function-distribucion-s12.md) |
| Fitness function ejecutable | [`scripts/check_distribution_decision.py`](../scripts/check_distribution_decision.py) |
| Workflow CI | [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) |
| Evidencia de CI rojo/verde | [`evidencia-fitness-ci-s14.md`](../docs/architecture/evidencia-fitness-ci-s14.md) |
| CI con violación detectada | [GitHub Actions run #60](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894) |
| CI corregido en verde | [GitHub Actions run #64](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129) |
| PR de experimento S11 | [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20) |
| PR de alineación S12 | [PR #25](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25) |

---

# 1. Estado A1–A6

| Criterio de la tarea | Estado real | Evidencia principal | Veredicto |
| --- | --- | --- | --- |
| **A1 — Tengo mediciones reproducibles y condiciones registradas** | ✅ **Cumple** | [prerregistro](../experimentos/crecimiento-mensajeria/00-preregistro.md), [contexto](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json), [resultado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json), [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20) | La medición registra commit, rama, árbol limpio, máquina, energía, versiones, semilla, VU, corridas, thresholds y resultados. |
| **A2 — Mi matriz distingue fallos observados e hipotéticos** | ✅ **Cumple** | [matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md), [análisis](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md) | No se presenta una sospecha como fallo demostrado; se separan señales observadas de hipótesis de API/DB/k6/equipo. |
| **A3 — Cada táctica tiene prueba o está declarada como propuesta** | ✅ **Cumple** | [matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md), [protocolo caché](../experimentos/crecimiento-mensajeria/05-protocolo-tactica-cache.md), [resultado caché](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md) | La caché fue probada; retry, timeout, circuit breaker, bulkhead, etc. quedan como alternativas/propuestas y no como resultados medidos. |
| **A4 — Comparé alternativas con costos y supuestos explícitos** | ✅ **Cumple** | [alternativas S12](../docs/architecture/alternativas-distribucion-s12.md), [auditoría IA](../docs/architecture/auditoria-ia-microservicios-s12.md) | Se comparan monolito, réplicas, microservicio síncrono y eventos con rangos C0, órdenes de magnitud y supuestos. |
| **A5 — Mi ADR 4 tiene fuentes y registra el veredicto real del comité** | 🟡 **Parcial** | [ADR-004](../docs/adr/ADR-004-distribucion-mensajeria.md), [mini-comité 2](../docs/architecture/mini-comite-s12.md) | **Las fuentes están completas; el veredicto real del mini-comité sigue pendiente. No se inventa.** |
| **A6 — Mi fitness function tiene regla y condición de fallo definidas** | ✅ **Cumple y fue demostrada** | [regla](../docs/architecture/fitness-function-distribucion-s12.md), [script](../scripts/check_distribution_decision.py), [workflow](../.github/workflows/ci.yml), [run rojo](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894), [run verde](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129) | Umbral: 0 violaciones. Una violación real hizo fallar el backend en CI y luego se corrigió antes de merge. |

## Resultado global

~~~text
A1  ✅
A2  ✅
A3  ✅
A4  ✅
A5  🟡 fuentes completas; veredicto humano pendiente
A6  ✅
~~~

La única casilla que **no debe marcarse todavía como totalmente cumplida** es A5, porque el enunciado exige un **veredicto real del comité** y el repositorio conserva correctamente ese evento como pendiente.

---

# 2. A1 — Mediciones reproducibles y condiciones registradas

## Qué ayuda se solicitó al Tutor

Se pidió revisar si la reejecución del escenario de Mensajería podía considerarse reproducible y qué condiciones debían quedar registradas para defender el experimento.

## Respuesta que se adoptó críticamente

La recomendación útil fue no limitar la evidencia a “corrí k6 y obtuve un p95”. La medición debía congelar:

- endpoint;
- semilla;
- conversación extrema;
- VU;
- solicitudes;
- número de corridas;
- warm-up descartado;
- thresholds;
- máquina;
- energía;
- versiones;
- commit medido;
- estado limpio de Git;
- topología local;
- criterio de invalidación.

## Cómo se verificó

La verificación no se hizo por memoria del equipo. Está versionada en:

- [prerregistro](../experimentos/crecimiento-mensajeria/00-preregistro.md);
- [contexto estructurado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json);
- [resultado estructurado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json).

El contexto registra, entre otros:

~~~text
git_revision: 73895be...
working_tree_clean_before_measurement: true
PostgreSQL: 16.14
k6: grafana/k6:0.54.0
executor: shared-iterations
VU: 10, 50, 100, 150, 200
runs_per_level: 4
discarded_run_per_level: 1
~~~

También registra semilla:

~~~text
1.000 conversaciones
289.000 mensajes
900 × 10
90 × 1.000
9 × 10.000
1 × 100.000
~~~

## Decisión

Se consideró válida la medición solo dentro de su frontera experimental:

> **No se observó degradación sostenida hasta 200 VU; no se extrapola más allá.**

No se transformó ese resultado en la afirmación “el sistema aguanta cualquier carga”.

## Evidencia clicable

- [Resultado consolidado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json)
- [Contexto](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json)
- [Trazabilidad de prerregistro](../docs/architecture/trazabilidad-prerregistro-s11.md)
- [Gráficas reproducibles](../experimentos/crecimiento-mensajeria/graficas/README.md)
- [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20)

---

# 3. A2 — Fallos observados vs hipotéticos

## Qué ayuda se solicitó al Tutor

Se pidió interpretar correctamente el crecimiento de latencia y determinar si era válido atribuir el problema a PostgreSQL, a la API o a k6.

## Respuesta que se verificó

El Tutor indicó que un crecimiento de latencia **no basta para declarar el componente causal**. Había que separar:

1. señales medidas;
2. hipótesis de fallo;
3. fallo realmente demostrado.

## Qué se observó

Los p95 medianos crecieron:

| VU | p95 mediano |
| ---: | ---: |
| 10 | 8,963 ms |
| 50 | 18,768 ms |
| 100 | 39,258 ms |
| 150 | 41,060 ms |
| 200 | 58,235 ms |

Pero:

- no hubo degradación sostenida;
- no hubo fallos HTTP;
- las páginas siguieron siendo válidas;
- el throughput continuó aumentando.

Por eso no se afirmó:

> “PostgreSQL se saturó”.

La afirmación respaldada es:

> **“La latencia creció con la concurrencia, pero no se pudo atribuir un límite aislado a API, PostgreSQL o k6 dentro de las condiciones de la prueba.”**

## Evidencia

- [Análisis de crecimiento](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md)
- [Matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md)
- [Resultado estructurado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json)

La matriz conserva explícitamente fallos como pool JDBC, PostgreSQL, API, k6 y máquina compartida sin convertirlos automáticamente en hechos observados.

---

# 4. A3 — Tácticas probadas vs propuestas

## Qué ayuda se solicitó al Tutor

Se pidió decidir qué táctica tenía sentido probar después del crecimiento y evitar escoger una técnica solo porque aparecía en el checklist.

## Respuesta que se usó críticamente

Las tácticas se evaluaron contra el **tipo de señal observada**.

Se consideraron:

- caché;
- timeout;
- retry;
- circuit breaker;
- bulkhead;
- aumentar pool JDBC;
- escalar API;
- SSE/WebSocket/backoff.

## Qué se decidió

No hubo fallos transitorios ni timeouts. Por eso:

- **retry** no se probó como si fuera solución demostrada;
- **circuit breaker** no se presentó como necesario;
- **timeout** no se presentó como respuesta al crecimiento de latencia;
- **caché local TTL** sí se probó porque la operación era de lectura repetida y la única tendencia observada era latencia creciente.

## Táctica medida

La caché quedó configurada experimentalmente con:

~~~text
TTL = 1 s
máximo = 1.000 entradas
solo offset=0
autorización antes de cache
invalidación al enviar mensaje
default producción = OFF
~~~

Resultado:

| Métrica | Antes | Después | Cambio |
| --- | ---: | ---: | ---: |
| p95 | 58,235 ms | 55,377 ms | -4,908 % |
| throughput | 2.303,017 req/s | 2.260,038 req/s | -1,866 % |
| errores | 0 | 0 | = |
| páginas válidas | 100 % | 100 % | = |

Veredicto:

~~~text
partially_supported
~~~

## Decisión

La caché **no se adoptó por defecto** porque mejoró ligeramente p95, pero redujo throughput y no produjo una ventaja suficientemente material para pagar coherencia, memoria e invalidación.

## Evidencia

- [Matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md)
- [Protocolo previo de la táctica](../experimentos/crecimiento-mensajeria/05-protocolo-tactica-cache.md)
- [Resultado explicado](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md)
- [JSON antes/después](../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json)

---

# 5. A4 — Alternativas, costos y supuestos explícitos

## Qué ayuda se solicitó al Tutor

Se pidió evaluar si la evidencia de S11 justificaba una distribución mayor, especialmente microservicios, y cómo comparar alternativas sin inventar precios de nube.

## Respuesta que se adoptó

Usar **costo relativo**, no una falsa cotización.

La unidad usada en S12 es **C0**, definida como el costo completo de operar la solución actual de referencia.

## Alternativas comparadas

| Alternativa | Costo relativo | Orden |
| --- | ---: | ---: |
| Monolito modular | 1 C0 | 10^0 |
| Réplicas del monolito | 2–3 C0 | 10^0 |
| Microservicio síncrono | 4–8 C0 | 10^0–10^1 |
| Eventos + proyecciones | 8–20 C0 | 10^1 |

Además se separan:

- infraestructura;
- operación;
- coordinación/Conway;
- disponibilidad;
- escalabilidad;
- reversibilidad;
- complejidad de fallos;
- seguridad.

## Decisión

Con los datos disponibles, mantener el monolito modular es la alternativa de menor costo que satisface el escenario medido.

Eso **no significa** que los microservicios sean incorrectos para siempre. El cambio se reabre ante evidencia nueva.

## Evidencia

- [Alternativas y costos S12](../docs/architecture/alternativas-distribucion-s12.md)
- [Auditoría de estrategia de microservicios propuesta por IA](../docs/architecture/auditoria-ia-microservicios-s12.md)
- [ADR-004](../docs/adr/ADR-004-distribucion-mensajeria.md)
- [PR #25](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25)

---

# 6. A5 — ADR 4, fuentes y veredicto del comité

## Fuentes del ADR 4 — ✅ Cumple

ADR-004 no se redactó de forma aislada. Sus fuentes están enlazadas y versionadas:

1. [prerregistro S11](../experimentos/crecimiento-mensajeria/00-preregistro.md);
2. [resultado de crecimiento](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json);
3. [análisis de crecimiento](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md);
4. [matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md);
5. [matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md);
6. [resultado de táctica](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md);
7. [alternativas y costos](../docs/architecture/alternativas-distribucion-s12.md);
8. [auditoría de IA](../docs/architecture/auditoria-ia-microservicios-s12.md).

**ADR:** [`docs/adr/ADR-004-distribucion-mensajeria.md`](../docs/adr/ADR-004-distribucion-mensajeria.md)

## Veredicto real del comité — ⏳ Pendiente humano

El archivo [`mini-comite-s12.md`](../docs/architecture/mini-comite-s12.md) está preparado, pero el repositorio todavía no contiene:

- fecha real de la sesión;
- participantes reales;
- restricción sorpresa real;
- veredicto real;
- razón emitida por el comité.

Por tanto, este documento **no marca A5 como totalmente cumplido**.

Eso es deliberado: el prerregistro de S11 ya establecía:

> “El veredicto del mini-comité no se anticipará ni se atribuirá a una persona que no lo haya emitido.”

## Qué falta para cerrar A5

Después del mini-comité:

1. registrar participantes y fecha;
2. registrar la restricción nueva;
3. marcar el veredicto real:
   - ratificar;
   - ajustar;
   - reconsiderar;
4. justificarlo;
5. actualizar ADR-004 con referencia al veredicto;
6. hacer commit/PR de esa actividad.

Hasta entonces:

~~~text
A5 = PARCIAL
fuentes ✅
veredicto humano ⏳
~~~

---

# 7. A6 — Fitness function con regla y condición de fallo

## Qué ayuda se solicitó al Tutor

Se pidió verificar si la decisión de ADR-004 podía protegerse automáticamente y cómo demostrar que la regla realmente rompe CI.

## Regla

La fitness function está documentada en:

[`docs/architecture/fitness-function-distribucion-s12.md`](../docs/architecture/fitness-function-distribucion-s12.md)

y ejecutada por:

[`scripts/check_distribution_decision.py`](../scripts/check_distribution_decision.py)

Protege, entre otras cosas:

- topología Compose aprobada;
- ausencia de broker no aprobado;
- ausencia de Redis/Kafka/RabbitMQ/Pulsar/NATS introducidos silenciosamente;
- caché experimental deshabilitada por defecto.

## Umbral

~~~text
0 violaciones
~~~

Condición de fallo:

~~~text
>= 1 violación → exit code != 0 → job backend falla
~~~

## Integración en CI

Workflow:

[`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

Step:

~~~text
Enforce ADR-004 distribution decision
~~~

## Evidencia real de que falla

En una rama temporal se cambió deliberadamente:

~~~yaml
MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-true}
~~~

La fitness function detectó:

~~~text
ADR-004 violado
Compose debe mantener MESSAGE_CACHE_ENABLED=false por defecto
Process completed with exit code 1
~~~

Evidencia:

- [Documento de la prueba controlada](../docs/architecture/evidencia-fitness-ci-s14.md)
- [GitHub Actions run #60](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894)

El workflow global aparece finalmente como cancelado por concurrencia de un commit posterior, pero el job **backend** terminó en **failure** y el step causal fue exactamente la fitness function.

Después se corrigió la configuración y se verificó el estado permitido:

- [GitHub Actions run #64 — verde](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129)

La violación nunca fue fusionada a `main`.

---

# 8. Registro de uso crítico del Tutor

Este apartado responde a la parte de la tarea que no se reduce a “usar IA”, sino a demostrar que las recomendaciones fueron verificadas antes de aceptarlas.

## Interacción 1 — interpretar la carga de k6

**Ayuda solicitada:** entender qué representa un VU y cómo leer los escalones de carga.

**Respuesta recibida:** VU = Virtual User; representa concurrencia simulada, no “requests por segundo”.

**Verificación:** el [prerregistro](../experimentos/crecimiento-mensajeria/00-preregistro.md) fija 4 solicitudes por VU y el [resultado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json) registra requests y throughput por separado.

**Decisión:** no interpretar 200 VU como “200 requests/s”; reportar VU y throughput como métricas diferentes.

## Interacción 2 — interpretar degradación

**Ayuda solicitada:** decidir si el crecimiento de p95 demostraba saturación.

**Respuesta recibida:** el aumento de latencia por sí solo no prueba saturación ni identifica automáticamente el componente causal.

**Verificación:** [resultado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json), [análisis](../experimentos/crecimiento-mensajeria/02-analisis-crecimiento.md) y [matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md).

**Decisión:** registrar “sin degradación sostenida hasta 200 VU” y no afirmar un cuello de botella no observado.

## Interacción 3 — seleccionar táctica

**Ayuda solicitada:** elegir entre caché, timeout/retry y otras tácticas.

**Respuesta recibida:** la táctica debe responder al fallo/señal observada, no a una preferencia genérica.

**Verificación:** no hubo fallos HTTP ni timeout que justificaran retry/circuit breaker; sí hubo lecturas repetidas y crecimiento de latencia.

**Decisión:** probar caché local y dejar las demás como propuestas en la [matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md).

## Interacción 4 — costo de distribución

**Ayuda solicitada:** explicar C0 y comparar distribución sin inventar precios.

**Respuesta recibida:** usar una unidad relativa de costo y hacer explícitos los supuestos.

**Verificación:** [alternativas S12](../docs/architecture/alternativas-distribucion-s12.md).

**Decisión:** comparar 1 C0, 2–3 C0, 4–8 C0 y 8–20 C0 como rangos relativos, no como pesos/dólares.

## Interacción 5 — fitness function

**Ayuda solicitada:** verificar si la regla de ADR-004 estaba realmente ejecutándose y si una violación era capaz de detener CI.

**Respuesta recibida:** una prueba unitaria de la regla era útil, pero la evidencia más fuerte era una violación controlada en una rama/PR que activara el fallo real del workflow.

**Verificación:** [run #60](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894) y [run #64](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129).

**Decisión:** conservar la evidencia rojo→verde sin fusionar la violación.

---

# 9. Qué recomendaciones del Tutor NO se aceptaron automáticamente

El uso fue crítico porque varias posibilidades fueron rechazadas o limitadas:

| Propuesta/interpretación | Decisión | Razón |
| --- | --- | --- |
| “200 VU significa 200 requests/s” | ❌ Rechazada | VU y throughput son conceptos distintos |
| “La latencia creció, entonces PostgreSQL se saturó” | ❌ Rechazada | no hubo evidencia causal suficiente |
| “Aplicar retry porque el checklist lo menciona” | ❌ Rechazada | hubo 0 fallos transitorios |
| “La caché mejoró p95, entonces debe quedar activa” | ❌ Rechazada | throughput empeoró y el veredicto fue parcial |
| “Microservicios son mejores por escalabilidad” | ❌ Rechazada actualmente | no existe presión medida que compense el costo |
| “Marcar A5 completo aunque no hubo comité” | ❌ Rechazada | el veredicto debe ser real y humano |
| “Una fitness function documentada ya es suficiente” | ❌ Rechazada como evidencia máxima | se demostró también en CI real |

---

# 10. Decisiones finales respaldadas por el repositorio

1. **La medición S11 es reproducible dentro de las condiciones registradas.**
2. **No se observó degradación sostenida hasta 200 VU.**
3. **No se atribuye un cuello de botella específico sin evidencia causal.**
4. **La caché fue probada, pero no se adopta por defecto.**
5. **Las demás tácticas permanecen como propuestas cuando no tienen experimento propio.**
6. **La distribución se mantiene en monolito modular por costo/evidencia actual.**
7. **ADR-004 tiene fuentes suficientes, pero el veredicto humano del comité sigue pendiente.**
8. **La fitness function tiene regla, umbral y condición de fallo ejecutables.**
9. **La violación controlada fue detectada por CI y corregida antes de merge.**

---

# 11. Recorrido recomendado para el profesor — 3 a 5 minutos

1. Ver la tabla [A1–A6](#1-estado-a1a6).
2. Abrir [resultado S11](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json) y [contexto](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/contexto.json) para A1.
3. Abrir [matriz de fallos](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md) para A2.
4. Abrir [matriz de resiliencia](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md) y [resultado de caché](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md) para A3.
5. Abrir [alternativas y costos](../docs/architecture/alternativas-distribucion-s12.md) para A4.
6. Abrir [ADR-004](../docs/adr/ADR-004-distribucion-mensajeria.md) y [mini-comité](../docs/architecture/mini-comite-s12.md) para comprobar por qué A5 sigue parcial.
7. Abrir [fitness function](../scripts/check_distribution_decision.py), [run rojo](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894) y [run verde](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129) para A6.
8. Revisar [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20) y [PR #25](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25) para trazabilidad del trabajo.

---

# 12. Respuesta corta para la sustentación

> En el Módulo 6 usamos el Tutor para revisar método, interpretación y decisiones, pero no aceptamos sus propuestas automáticamente. Verificamos cada recomendación contra el repositorio. A1 se respalda con prerregistro, contexto y resultados reproducibles; A2 con una matriz que separa fallos observados de hipótesis; A3 con una caché realmente medida y otras tácticas declaradas como propuestas; A4 con cuatro alternativas y costos relativos explícitos; A5 tiene todas las fuentes del ADR 4, pero el veredicto real del mini-comité sigue pendiente y no lo inventamos; y A6 tiene una fitness function con umbral cero que fue demostrada haciendo fallar el backend en CI y luego volviendo a verde después de corregir la violación.

---

# 13. Estado honesto para AVATA

Si la plataforma permite marcar las casillas según evidencia actual:

~~~text
A1 ☑
A2 ☑
A3 ☑
A4 ☑
A5 ☐  pendiente del veredicto real del comité
A6 ☑
~~~

Si el mini-comité ocurre antes de entregar esta tarea, se debe actualizar primero [`mini-comite-s12.md`](../docs/architecture/mini-comite-s12.md), luego ADR-004 y finalmente este documento. No se debe marcar A5 únicamente para completar visualmente el formulario.
