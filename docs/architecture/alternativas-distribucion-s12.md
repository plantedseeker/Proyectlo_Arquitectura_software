# Alternativas de distribución y costo relativo — S12

Estado: **comparación técnica anterior al mini-comité 2**.

## Pregunta de decisión

¿La evidencia de crecimiento de Mensajería justifica distribuir UTrabajo o
incorporar infraestructura adicional en este momento?

La decisión usa la evidencia de
[`MSG-PERF-GROWTH-01`](../../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json)
y
[`MSG-PERF-CACHE-01`](../../experimentos/crecimiento-mensajeria/comparacion-cache/20261016T230407Z/resultado-comparacion.json).
El experimento no observó degradación sostenida hasta 200 VU; la caché redujo
el p95 mediano 4,908 %, pero también redujo el throughput mediano 1,866 % y no
cumplió la regla de éxito completa.

## Método de estimación de costo

No existen facturas de nube ni una topología productiva desplegada. Por ello
no se inventan precios monetarios. Se usa una **unidad relativa C0**, equivalente
al costo total de operar hoy una API desplegable y una base PostgreSQL:

- cómputo y almacenamiento;
- despliegue, respaldo y restauración;
- logs, métricas y alertas;
- secretos y certificados;
- tiempo de diagnóstico y guardia.

Los rangos son estimaciones de orden de magnitud para comparar complejidad. No
son resultados medidos ni cotizaciones. `10^0 C0` significa el mismo orden que
la solución actual; `10^1 C0`, aproximadamente un orden mayor.

Para hacer visible el costo que el material de M6 separa, la estimación también
usa tres ejes ordinales:

- **infraestructura:** instancias, base, balanceador, broker y almacenamiento;
- **operación:** despliegue, observabilidad, respaldo, recuperación y guardia;
- **coordinación/Conway:** contratos, sincronización entre equipos, propiedad
  de datos y costo de cambio organizacional.

Las bandas `O(1)`, `O(10)` y `O(100)` significan respectivamente costo relativo
bajo, medio y alto respecto de la topología actual. Son categorías comparativas,
no notación de complejidad algorítmica, dinero ni mediciones de producción.

## Alternativas

### A. Monolito modular actual — propuesta seleccionada

- **Topología:** una API y una base, con límites lógicos y puertos internos.
- **Rendimiento:** no mostró degradación sostenida hasta 200 VU; evita red y
  serialización entre Mensajería y Ofertas.
- **Disponibilidad:** API y base siguen siendo unidades de fallo compartidas.
- **Complejidad:** la menor; exige disciplina de módulos y pruebas.
- **Operación:** un despliegue, una base, un conjunto principal de alarmas.
- **Escalabilidad:** vertical o réplica completa antes de extraer contextos.
- **Costo relativo:** `1 C0`, orden `10^0`.
- **Reversibilidad:** alta; los contratos lógicos permiten extraer después.

### B. Réplicas del monolito completo

- **Topología:** dos o más réplicas de API, balanceador y PostgreSQL compartido.
- **Rendimiento:** aumenta capacidad de API si esta llega a ser el límite; no
  corrige por sí sola un cuello de botella en la base.
- **Disponibilidad:** tolera caída de una réplica, pero no la de PostgreSQL.
- **Complejidad:** sesiones, archivos locales, balanceo y despliegue coordinado.
- **Operación:** más instancias y observabilidad, sin duplicar el modelo.
- **Escalabilidad:** gruesa; escala todo aunque solo Mensajería tenga presión.
- **Costo relativo:** `2–3 C0`, todavía orden `10^0`.
- **Reversibilidad:** alta si se mantiene compatibilidad de datos y despliegue.

### C. Microservicio síncrono de Mensajería

- **Topología:** API principal y servicio de Mensajería; base propia o frontera
  de datos explícita; llamadas remotas con autenticación.
- **Rendimiento:** agrega salto de red; permite escalar Mensajería de forma
  independiente si se demuestra que ese contexto es el límite.
- **Disponibilidad:** una caída de Mensajería puede aislarse, pero aparecen
  fallos parciales, timeout y coordinación con Identidad y Ofertas.
- **Complejidad:** contratos remotos, migración de datos, compatibilidad,
  trazas distribuidas, secretos y pruebas de integración.
- **Operación:** al menos dos despliegues y, con base propia, dos ciclos de
  respaldo, migración y recuperación.
- **Escalabilidad:** fina para Mensajería.
- **Costo relativo:** `4–8 C0`, frontera entre `10^0` y `10^1`.
- **Reversibilidad:** media; volver exige reconciliar datos y contratos.

### D. Mensajería distribuida por eventos

- **Topología:** alternativa C más broker, outbox/inbox, consumidores,
  proyecciones, reintentos y cola de mensajes fallidos.
- **Rendimiento:** desacopla temporalmente y absorbe picos, pero no elimina el
  costo de lectura ni garantiza menor latencia extremo a extremo.
- **Disponibilidad:** puede mantener ciertas operaciones durante fallos, a
  cambio de consistencia eventual y reconciliación.
- **Complejidad:** idempotencia, orden, duplicados, esquemas de eventos,
  replay, observabilidad y políticas de retención.
- **Operación:** broker altamente disponible, monitoreo de lag y procedimientos
  de recuperación adicionales.
- **Escalabilidad:** la más flexible cuando existe volumen y asincronía reales.
- **Costo relativo:** `8–20 C0`, orden `10^1`.
- **Reversibilidad:** baja a media por los eventos y estados replicados.

## Comparación consolidada

| Dimensión | A. Monolito modular | B. Réplicas completas | C. Microservicio síncrono | D. Eventos |
| --- | --- | --- | --- | --- |
| Evidencia actual que lo exige | sí, funciona | no hay límite | no hay límite aislado | no hay fallo de disponibilidad |
| Rendimiento esperado hoy | sin salto remoto | más capacidad de API | latencia de red adicional | latencia y procesamiento eventual |
| Disponibilidad | compartida | API redundante | fallos parciales nuevos | desacoplamiento temporal |
| Complejidad | baja | media | alta | muy alta |
| Operación | 1 despliegue + 1 DB | balanceador + N API + DB | ≥2 despliegues y fronteras de datos | lo anterior + broker/proyecciones |
| Escalabilidad | gruesa, suficiente en la frontera medida | gruesa | por contexto | por consumidores/particiones |
| Costo relativo | `1 C0` (`10^0`) | `2–3 C0` (`10^0`) | `4–8 C0` (`10^0–10^1`) | `8–20 C0` (`10^1`) |
| Reversibilidad | alta | alta | media | baja–media |

## Matriz explícita de costo por dimensión

| Alternativa | Infraestructura | Operación | Coordinación / Conway | Razón dominante |
| --- | --- | --- | --- | --- |
| A. Monolito modular | `O(1)` | `O(1)` | `O(1)` | un despliegue, una base y un equipo |
| B. Réplicas completas | `O(10)` | `O(10)` | `O(1)` | balanceo y más instancias, sin separar propiedad funcional |
| C. Microservicio síncrono | `O(10)` | `O(10)` | `O(10)` | dos despliegues, frontera de datos y coordinación de contrato |
| D. Eventos y proyecciones | `O(10)` | `O(100)` | `O(100)` | broker, replay, esquemas, consumidores y coordinación de estados |

El equipo actual es pequeño y no tiene ciclos de despliegue independientes.
Por eso C y D añaden una frontera organizacional artificial: la coordinación
sube aunque la infraestructura pudiera ejecutarse en la misma máquina. Esta
dimensión se revisará si aparecen equipos autónomos o una exigencia real de
despliegue independiente.

## Propuesta anterior al comité

Conservar **A, monolito modular**, con la caché experimental deshabilitada. No
hay evidencia de saturación, errores, necesidad de despliegue independiente ni
continuidad durante una caída que compense las alternativas C o D. Si aparece
un límite de API antes que de base, B es una transición menor y debe medirse
antes de extraer Mensajería.

Reabrir la decisión si una medición comparable muestra, durante al menos dos de
tres corridas válidas:

- p95 superior a 500 ms;
- errores o respuestas inválidas por encima de cero;
- throughput que deja de crecer y atribución sustentada a la API;
- indisponibilidad de un contexto que deba aislarse;
- necesidad organizacional real de despliegue independiente.

El mini-comité 2 debe ratificar, ajustar o rechazar esta propuesta. Este archivo
no anticipa su veredicto.
