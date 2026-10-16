# Resumen ejecutivo — Módulo 5: dominio, integración y Spike 1

Este documento reúne en un solo recorrido los avances de las semanas 9 y 10.
Cualquier integrante puede explicar el trabajo siguiendo la evidencia del
repositorio, sin depender de memoria individual ni atribuirse aportes que Git no
demuestre.

## Resultado general

UTrabajo identificó cinco bounded contexts lógicos dentro del monolito modular:
Identidad y Acceso, Perfiles, Ofertas, Postulaciones y Mensajería. La frontera
seleccionada fue **Mensajería → Ofertas**, porque crear una conversación requiere
comprobar la oferta y conocer su empresa propietaria.

Se compararon acceso directo, integración síncrona y eventos asíncronos. El
Spike 1 evaluó un contrato síncrono mínimo con `jobId` y `companyId`. Las tres
corridas válidas cumplieron ampliamente el umbral preregistrado, sin fallos HTTP
inesperados y con los controles funcionales aprobados. Por eso ADR-003 propone
la opción síncrona para la necesidad actual y mantiene diferidos broker, CQRS
completo y Event Sourcing.

El resultado no dice que lo asíncrono sea incorrecto. Dice que su complejidad no
está justificada todavía por esta necesidad y esta evidencia.

## Accesos directos a la evidencia

Todos los enlaces siguientes son relativos al repositorio. En GitHub el docente
puede seleccionarlos y llegar directamente al archivo correspondiente, tanto
desde el pull request como después de la fusión en `main`.

| Qué se quiere comprobar | Enlace directo |
| --- | --- |
| Resumen integrador de Semana 9 | [`09-api-events-integration.md`](09-api-events-integration.md) |
| Cobertura literal de la rúbrica S9–S10 | [`17-checklist-modulo-5.md`](17-checklist-modulo-5.md) |
| Diferencia entre subdominio y bounded context | [`subdominios.md`](../docs/dominio/subdominios.md) |
| Contextos y relaciones justificadas | [`context-map.md`](../docs/dominio/context-map.md) · [`context-map.puml`](../docs/dominio/context-map.puml) |
| Responsabilidades y exclusiones | [`responsabilidades-contextos.md`](../docs/dominio/responsabilidades-contextos.md) |
| Contrato exacto de integración | [`contrato-api.yaml`](../docs/integracion/contrato-api.yaml) |
| Comparación síncrono/asíncrono | [`sincrono-vs-asincrono.md`](../docs/integracion/sincrono-vs-asincrono.md) |
| Eventos candidatos y filtro crítico | [`eventos-candidatos.md`](../docs/integracion/eventos-candidatos.md) · [`registro-critico-ia.md`](../docs/integracion/registro-critico-ia.md) |
| Evaluación CQRS/Event Sourcing | [`cqrs-event-sourcing.md`](../docs/integracion/cqrs-event-sourcing.md) |
| Hipótesis anterior al código | [`00-preregistro.md`](../experimentos/spike-01-integracion/00-preregistro.md) |
| Ajuste anterior a la medición | [`01-enmienda-previa-a-medicion.md`](../experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md) |
| Resultado estructurado oficial | [`resultado.json`](../experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json) |
| Veredicto posterior | [`veredicto.md`](../experimentos/spike-01-integracion/veredicto.md) |
| Capturas, fallos y recuperación | [`evidencias/README.md`](../experimentos/spike-01-integracion/evidencias/README.md) |
| Decisión respaldada por el spike | [`ADR-003-integracion-contextos.md`](../docs/adr/ADR-003-integracion-contextos.md) |

## Las cuatro evidencias centrales del módulo

| Evidencia exigida | Resultado del equipo | Dónde se comprueba |
| --- | --- | --- |
| Mapa de dominio/contextos con límites justificados | Se distingue subdominio —problema— de bounded context —modelo/solución— y se explican responsabilidades y exclusiones | `docs/dominio/subdominios.md`, `responsabilidades-contextos.md`, `context-map.md` y `context-map.puml` |
| Contrato de integración y decisión síncrono/asíncrono | Existe OpenAPI versionado, payload mínimo, errores 401/404/500 y comparación explícita de costos | `docs/integracion/contrato-api.yaml` y `sincrono-vs-asincrono.md` |
| Spike 1 preregistrado, ejecutado y referenciado por ADR-003 | Hipótesis y criterios preceden al código; se conservan cuatro corridas, veredicto y decisión posterior | `experimentos/spike-01-integracion/` y `docs/adr/ADR-003-integracion-contextos.md` |
| Análisis de CQRS, eventos y consistencia eventual | Se evalúan utilidad, costos y condiciones de revisión sin adoptar patrones por moda | `docs/integracion/eventos-candidatos.md` y `cqrs-event-sourcing.md` |

El artefacto integrador con el nombre literal solicitado para Semana 9 sí existe
en esta rama y puede abrirse directamente desde
[`dossier/09-api-events-integration.md`](09-api-events-integration.md).

Ese archivo conecta el modelo de dominio, las alternativas, el contrato, el
registro crítico de IA, el preregistro y el resultado del spike.

## Semana 9 — qué se construyó

### 1. Subdominios y bounded contexts

El documento `docs/dominio/subdominios.md` evita tratar ambos términos como
sinónimos. Para cada área identifica problema, responsabilidad, conceptos y lo
que queda por fuera. Las fronteras se derivan del comportamiento As-Is del
código, no solamente de nombres de tablas.

### 2. Context Map

Cada flecha se lee desde el consumidor hacia el proveedor e incluye:

- quién consume;
- quién provee;
- qué información cruza;
- por qué existe la dependencia;
- cómo funciona hoy dentro del monolito.

La relación estudiada es:

```text
Mensajería ──consume──> Ofertas
              jobId + companyId
```

Son contextos lógicos, no cinco microservicios.

### 3. Contrato y alternativas

El contrato experimental es:

```http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
```

Respuesta mínima:

```json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
```

La opción síncrona usa el estado actual y mantiene un modelo pequeño, pero crea
dependencia temporal. La opción asíncrona permitiría una proyección local, pero
introduciría consistencia eventual, duplicados, reintentos, idempotencia,
publicación confiable y operación adicional.

### 4. Eventos e IA

El catálogo separa hechos de dominio candidatos de interacciones de interfaz,
detalles JDBC y funcionalidades inexistentes. `registro-critico-ia.md` conserva
qué propuestas se aceptaron o rechazaron y por qué. La IA apoyó el análisis; no
tomó la decisión arquitectónica.

### 5. Preregistro

Antes del código experimental quedaron definidos:

- pregunta e hipótesis falsable;
- variable principal p95;
- aceptación y refutación;
- alcance y exclusiones;
- 10 VU, 40 solicitudes y cuatro corridas;
- primera corrida descartada como calentamiento;
- umbral p95 <= 500 ms;
- rama experimental prevista.

La reducción del payload de cuatro a dos campos se registró antes de compilar y
medir en `01-enmienda-previa-a-medicion.md`. No se cambió carga ni umbral después
de observar resultados.

## Semana 10 — ejecución y resultado

La ejecución oficial usó:

- rama `spike/s10-integracion-mensajeria-ofertas`;
- commit medido `1e8bc8767efa3a601522046930332c714b6eeb65`;
- PostgreSQL 16.14;
- k6 0.54.0;
- 10 usuarios virtuales;
- 40 solicitudes medidas por corrida;
- API, base y generador en el mismo equipo físico.

| Corrida | Uso | p95 | Checks | Fallos HTTP inesperados |
| ---: | --- | ---: | ---: | ---: |
| 1 | calentamiento descartado | 5,779 ms | 100 % | 0 % |
| 2 | válida | 4,781 ms | 100 % | 0 % |
| 3 | válida | 4,360 ms | 100 % | 0 % |
| 4 | válida | 4,710 ms | 100 % | 0 % |

En cada corrida se validó:

- contrato positivo con exactamente `jobId` y `companyId`;
- token ausente → `401`;
- token inválido → `401`;
- oferta inexistente → `404`;
- 40 solicitudes medidas;
- tasa de fallos inesperados igual a cero.

Veredicto:

- experimento: **válido**;
- hipótesis: **respaldada bajo las condiciones registradas**;
- alcance: **ajustada antes de medir**.

La fuente autoritativa es
`experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json`.
Los NDJSON, logs y hashes permiten verificar que el resumen no oculta una
corrida desfavorable.

## Prueba de temporalidad

La secuencia exigida por el módulo es comprobable en Git:

```text
804d2d2  preregistro original con Resultado vacío
    <
de83109  enmienda de alcance anterior a medición
    <
4b1c43d  implementación experimental
    <
1e8bc87  commit medido con árbol limpio
    <
b2d59f1  resultados, logs, datos crudos y capturas
    <
4072eba  veredicto posterior a los resultados
    <
9a62b1d  ADR-003 y cierre documental
```

Esto evita la secuencia inválida de implementar, medir y después inventar una
hipótesis que coincida con los resultados.

## Decisión de ADR-003

ADR-003 propone una interacción síncrona y mínima de Mensajería hacia Ofertas.
Mientras ambos contextos continúen en el mismo proceso, la implementación
productiva preferida es un puerto de aplicación interno: no tiene sentido que
el monolito se llame a sí mismo por HTTP. El adaptador HTTP del spike demuestra
el contrato y deja preparada una frontera verificable si alguna vez se
distribuye.

La decisión conserva:

- estado actual al crear la conversación;
- payload pequeño;
- fallo explícito;
- una sola base sin proyección duplicada;
- reversibilidad si aparece una necesidad distinta.

La decisión se revisa si Mensajería debe operar durante una caída de Ofertas,
si aparecen despliegues independientes, si una medición comparable incumple el
SLO o si el volumen convierte la consulta síncrona en cuello de botella.

## Continuidad de evidencia S4 → S9 → S10

- **S4** estableció disciplina de medición reproducible y una línea base de
  mensajería en la topología local. No se usa como comparación directa de este
  endpoint porque mide otra operación.
- **S7–S8** localizaron costos, conservaron el monolito modular y protegieron la
  regla `controller !→ JDBC/SQL directo`.
- **S9** hizo explícita la frontera Mensajería → Ofertas y comparó contratos.
- **S10** implementó y midió el contrato seleccionado, emitió veredicto y
  alimentó ADR-003.

La decisión no nace de un único número: continúa la evidencia arquitectónica de
los módulos anteriores y delimita exactamente qué aporta cada medición.

## Recorrido demostrable en el repositorio

Desde la portada del repositorio se puede abrir la sección **Módulo 5 — dominio
e integración** y seguir este orden:

1. `dossier/09-api-events-integration.md` — panorama de S9.
2. `docs/dominio/context-map.md` — frontera seleccionada.
3. `docs/integracion/contrato-api.yaml` — contrato exacto.
4. `experimentos/spike-01-integracion/00-preregistro.md` — criterio anterior.
5. `experimentos/spike-01-integracion/resultados/.../resultado.json` — cifras.
6. `experimentos/spike-01-integracion/veredicto.md` — interpretación posterior.
7. `docs/adr/ADR-003-integracion-contextos.md` — decisión y consecuencias.
8. `dossier/17-checklist-modulo-5.md` — cobertura literal de la rúbrica.

Para mostrar el resultado resumido en PowerShell sin ejecutar otra medición:

```powershell
$r = Get-Content ".\experimentos\spike-01-integracion\resultados\20261016T211031Z\resultado.json" -Raw | ConvertFrom-Json
$r | Select-Object verdict, hypothesis_verdict, experiment_valid, acceptance_passed
$r.run_evaluations | Select-Object run, discarded, p95_ms, acceptance_passed | Format-Table
```

Para reproducir el experimento completo —solo si hay tiempo, Docker y árbol
limpio—:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-spike-01-integracion.ps1
```

## Explicación oral compacta

> El problema fue hacer explícita la frontera entre Mensajería y Ofertas sin
> agregar complejidad prematura. Primero mapeamos los contextos y comparamos un
> contrato síncrono con eventos. Antes de programar definimos hipótesis, carga,
> umbral y refutación. El alcance se redujo antes de medir a los dos campos que
> realmente necesita crear un chat. Ejecutamos cuatro corridas; descartamos la
> primera y las tres válidas tuvieron p95 entre 4,360 y 4,781 ms, con todos los
> checks y casos 401/404 aprobados. ADR-003 propone la opción síncrona mínima,
> pero reconoce sus límites y define cuándo revisar la alternativa asíncrona.

## Preguntas probables y respuestas verificables

### ¿Por qué no dejaron el SQL directo si ya funcionaba?

Porque funciona técnicamente, pero acopla Mensajería al esquema persistente de
Ofertas. El contrato hace explícito qué dato pertenece al proveedor y limita lo
que el consumidor conoce.

### ¿Por qué se quitaron `active` y `title`?

Porque crear una conversación solo necesita existencia, `jobId` y `companyId`.
La existencia se representa con 200/404. La corrección se documentó antes de
medir y no se acomodó a las métricas.

### ¿Por qué eligieron 500 ms?

Fue el umbral experimental escrito antes del código. No se presenta como SLO de
producción; sirve para impedir que el criterio cambie al ver el resultado.

### ¿El p95 de 4–5 ms se puede prometer en producción?

No. k6, API y PostgreSQL compartieron un equipo local. El resultado respalda la
hipótesis bajo esas condiciones, no una red distribuida ni carga máxima.

### ¿Por qué no Kafka, CQRS o Event Sourcing?

No hay evidencia de disponibilidad independiente, separación de despliegues o
reconstrucción desde eventos que pague su costo. La decisión incluye
disparadores concretos para reconsiderarlos.

### ¿El endpoint ya reemplazó `createOrGetChat`?

No. El spike prueba el contrato. La migración productiva hacia un puerto interno
es un cambio posterior; el dossier lo declara para no confundir experimento con
implementación definitiva.

### ¿Cómo se controló seguridad?

El endpoint experimental exige `X-Integration-Token`, rechaza ausencia e
invalidez con 401 y no acepta ninguna credencial si la configuración está vacía.
Una separación real requeriría gestión productiva de secretos y autenticación
entre servicios.

### ¿Qué falló durante la preparación?

Se encontraron Docker detenido, la ruta del Android SDK, un error lint y el
tratamiento de stderr de Compose en Windows PowerShell. Se corrigieron antes de
la medición oficial. Las capturas conservan tanto los fallos como la recuperación
y el resultado final.

## Trazabilidad de participación

La autoría técnica se presenta con evidencia Git, no con asignaciones inventadas.
Los commits de la rama S10 identifican a su autor real; las revisiones y
aprobaciones deben quedar registradas en el pull request por la cuenta de cada
integrante. Cualquier integrante puede explicar el diseño, pero solo debe
atribuirse cambios o revisiones que sean comprobables.

Comprobación local:

```powershell
git log main..spike/s10-integracion-mensajeria-ofertas --format="%h | %an | %s"
```

## Cierre

El Módulo 5 está materialmente construido: dominio y contextos, relaciones,
contrato, catálogo de eventos, crítica de IA, preregistro, rama experimental,
medición, evidencia física, veredicto, análisis CQRS/Event Sourcing y ADR-003.

Lo pendiente no es inventar más arquitectura: es revisar el pull request y
ratificar humanamente ADR-003. Hasta esa ratificación, el documento usa
“propuesta respaldada” y no “decisión aprobada”.
