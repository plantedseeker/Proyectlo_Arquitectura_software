# ADR-003 — Integración síncrona mínima entre Mensajería y Ofertas

- Fecha: 2026-10-16
- Estado: **Propuesta respaldada por Spike 1; pendiente de ratificación humana**
- Categoría: Mensajería y mesa de ayuda
- Protege: límites de dominio, consistencia, seguridad, rendimiento y costo operacional

## Contexto

El Context Map identifica a Mensajería como consumidor lógico de Ofertas. Para
crear una conversación necesita comprobar que la oferta existe y conocer su
empresa propietaria. En el código actual, `UTrabajoService.createOrGetChat`
consulta directamente `job_offer` porque ambos contextos viven en el mismo
backend y esquema PostgreSQL.

La consulta directa funciona, pero no expresa una frontera estable. Un cambio
del esquema de Ofertas puede propagarse hacia Mensajería y hacer que el
consumidor conozca más datos del proveedor de los que realmente necesita.

Se compararon tres cursos:

1. conservar acceso directo al esquema compartido;
2. introducir un contrato síncrono mínimo;
3. replicar datos mediante eventos y consistencia eventual.

El Spike 1 probó la segunda opción mediante una representación HTTP explícita,
con condiciones y umbral definidos antes de medir.

## Decisión propuesta

Adoptar para la creación de conversaciones una **interacción síncrona de
solicitud/respuesta** desde Mensajería hacia Ofertas, limitada al siguiente
contrato semántico:

~~~http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
~~~

~~~json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
~~~

- `200` representa una oferta existente y entrega solo `jobId` y `companyId`.
- `404` representa una oferta inexistente.
- Una credencial interna ausente o inválida se rechaza con `401`.
- `active` y `title` no forman parte del contrato de creación de conversación.
- Mensajería no debe apropiarse del esquema completo de `job_offer`.

La decisión es sobre la **frontera y la semántica síncrona**, no sobre separar
procesos inmediatamente. Mientras UTrabajo siga siendo un monolito modular, la
implementación productiva preferida es un puerto de aplicación provisto por
Ofertas; Mensajería no debe hacer una llamada HTTP al mismo proceso. El adaptador
HTTP del spike conserva un contrato verificable y sirve como opción si la
frontera llega a distribuirse. Migrar `createOrGetChat` queda para un cambio
posterior y explícito; el spike no afirma que ese flujo ya fue sustituido.

No se introducen por ahora broker, proyección replicada, CQRS completo ni Event
Sourcing. El token local del experimento tampoco se promueve automáticamente a
un mecanismo productivo de secretos entre servicios.

## Evidencia que respalda la decisión

El experimento oficial midió el commit
`1e8bc8767efa3a601522046930332c714b6eeb65` con PostgreSQL 16, k6 0.54.0,
10 VU y 40 solicitudes medidas por corrida. La corrida 1 fue calentamiento; las
corridas 2–4 fueron válidas.

| Corrida válida | p95 | Umbral | Fallos inesperados | Contrato y negativos |
| ---: | ---: | ---: | ---: | --- |
| 2 | 4,781 ms | 500 ms | 0 % | cumplen |
| 3 | 4,360 ms | 500 ms | 0 % | cumplen |
| 4 | 4,710 ms | 500 ms | 0 % | cumplen |

Los controles verificaron el payload exacto, token ausente/inválido → `401` y
oferta inexistente → `404`. No hubo cambio de esquema. El veredicto experimental
es **hipótesis respaldada** y **alcance ajustada**, porque el payload se redujo
antes de medir de cuatro campos a dos.

La medición fue local y en una máquina compartida entre generador, API y base de
datos. Respalda el criterio del spike, pero no constituye una predicción del p95
en una topología distribuida o en producción.

## Alternativas consideradas

### A. Acceso directo de Mensajería a `job_offer`

- **Beneficio:** es el comportamiento actual y no añade adaptadores.
- **Costo:** acopla el consumidor al esquema persistente del proveedor.
- **Consistencia:** fuerte dentro de la misma transacción/base.
- **Seguridad:** no abre una ruta, pero dispersa el conocimiento de datos.
- **Decisión:** tolerada durante la migración, no elegida como frontera objetivo.

### B. Contrato síncrono mínimo — seleccionada

- **Beneficio:** estado actual, fallo explícito y payload pequeño.
- **Costo:** acoplamiento temporal; si el proveedor no responde, la creación no
  puede obtener la empresa de la oferta.
- **Consistencia:** no requiere una copia eventualmente consistente.
- **Seguridad:** el adaptador remoto exige autenticación entre servicios y
  gestión real de secretos si algún día se distribuye.
- **Operación:** no requiere nueva infraestructura en el monolito actual.
- **Reversibilidad:** alta porque el consumidor depende de un contrato pequeño.

### C. Eventos de Ofertas + proyección en Mensajería

- **Beneficio potencial:** desacoplamiento temporal y lectura local aunque el
  proveedor esté temporalmente indisponible.
- **Costo:** broker/outbox, idempotencia, orden, reintentos, observabilidad,
  reconstrucción y reconciliación de estado replicado.
- **Consistencia:** eventual; una oferta retirada podría permanecer desfasada.
- **Decisión:** diferida hasta que exista una necesidad medida de disponibilidad
  independiente o volumen que compense el costo.

### D. CQRS completo o Event Sourcing

- **Beneficio potencial:** modelos de lectura separados o historial inmutable.
- **Costo:** proyecciones, evolución de eventos, reconstrucción y mayor operación.
- **Decisión:** descartada para esta necesidad; el spike no mostró presión que
  justifique esos patrones.

## Consecuencias

### Positivas

- La dirección Mensajería → Ofertas queda explícita y comprobable.
- El consumidor recibe solo los datos necesarios para crear la conversación.
- Se conserva consistencia actual sin mantener una proyección duplicada.
- La solución cabe en el monolito modular y no obliga a crear microservicios.
- El contrato puede convertirse en frontera remota si aparece evidencia futura.

### Negativas

- La disponibilidad de la creación de chat depende de resolver Ofertas en el
  mismo momento.
- Hace falta migrar el acceso directo actual hacia un puerto explícito.
- La variante HTTP amplía superficie de ataque y requiere endurecimiento antes
  de cualquier exposición fuera del entorno experimental.
- El resultado local no cubre latencia ni fallos de una red distribuida.

## Plan de adopción segura

1. definir un puerto interno `JobConversationContextProvider` con el modelo de
   dos campos;
2. hacer que Mensajería use ese puerto en lugar de consultar `job_offer`;
3. conservar pruebas de autorización, 404 y contrato exacto;
4. no hacer self-HTTP dentro del monolito;
5. mantener el adaptador HTTP deshabilitado cuando no exista una credencial
   explícita y revisar su autenticación antes de distribuir la frontera;
6. tratar el título usado al listar chats como una necesidad distinta, no
   ampliar silenciosamente este contrato de creación.

## Reversibilidad y disparadores de revisión

La decisión se revisará si Mensajería debe continuar creando conversaciones
durante una indisponibilidad de Ofertas, si los contextos pasan a despliegues o
equipos independientes, si una medición comparable incumple el SLO acordado, o
si el volumen hace que la consulta síncrona sea un cuello de botella.

Ante esos disparadores se ejecutará otro spike que compare la opción síncrona
con eventos, outbox, idempotencia y tolerancia al desfase. No se introduce un
broker solo por anticipación.

## Trazabilidad

- [`Context Map`](../dominio/context-map.md)
- [`Responsabilidades de contextos`](../dominio/responsabilidades-contextos.md)
- [`Contrato OpenAPI`](../integracion/contrato-api.yaml)
- [`Comparación síncrono/asíncrono`](../integracion/sincrono-vs-asincrono.md)
- [`Preregistro`](../../experimentos/spike-01-integracion/00-preregistro.md)
- [`Enmienda previa a medición`](../../experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md)
- [`Resultado estructurado`](../../experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json)
- [`Veredicto`](../../experimentos/spike-01-integracion/veredicto.md)
- [`Evidencia visual`](../../experimentos/spike-01-integracion/evidencias/README.md)

## Confirmación requerida

El equipo debe ratificar o ajustar esta propuesta. La evidencia técnica permite
defender la decisión, pero no se fabrican aprobaciones humanas ni se confunde el
resultado del spike con una aceptación automática del ADR.
