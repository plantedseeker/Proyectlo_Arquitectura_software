# Semana 9 — API, eventos e integración

> Artefacto principal de Módulo 5 para S9. No reemplaza el archivo histórico 09-c4-trazabilidad-localizacion.md de S5–S8; ambos responden a módulos distintos del curso.

## 1. Pregunta de la semana

> ¿Dónde están las fronteras del dominio de UTrabajo y cómo deben comunicarse sin introducir complejidad que todavía no esté justificada?

La categoría arquitectónica continúa siendo **Mensajería y mesa de ayuda**. Por eso el Spike 1 se conecta con la frontera Mensajería → Ofertas en lugar de abandonar el problema estudiado en módulos anteriores.

## 2. Modelo de dominio

Artefactos:

- docs/dominio/subdominios.md
- docs/dominio/responsabilidades-contextos.md
- docs/dominio/context-map.md
- docs/dominio/context-map.puml

Contextos identificados:

1. Identidad y Acceso;
2. Perfiles;
3. Ofertas;
4. Postulaciones;
5. Mensajería.

Son fronteras lógicas en el monolito modular; no cinco microservicios.

## 3. Frontera seleccionada

El código actual demuestra que Mensajería necesita información de Ofertas:

~~~text
ChatController.create
  → UTrabajoService.createOrGetChat
  → SELECT company_id FROM job_offer
  → INSERT/UPSERT chat
~~~

Además, el listado de chats usa job_offer.title.

La necesidad de negocio es real; lo que S9 hace explícito es **cómo debería cruzar esa información la frontera**.

## 4. Alternativas comparadas

### A. Contrato síncrono

Mensajería consulta el contexto mínimo de una oferta al necesitarlo.

- estado actualizado al momento de la operación;
- menor complejidad actual;
- acoplamiento temporal al proveedor;
- si se distribuyera, exigiría timeouts y manejo de fallos.

### B. Eventos asíncronos

Ofertas publica hechos y Mensajería conserva una proyección mínima.

- menor acoplamiento temporal;
- posibilidad de seguir leyendo una copia local;
- consistencia eventual;
- duplicados, reintentos, idempotencia y publicación confiable;
- mayor costo operativo.

**Selección para experimentar:** A, contrato síncrono.

**Resultado:** hipótesis respaldada bajo las condiciones medidas; alcance
ajustado antes de medir al payload mínimo `jobId` + `companyId`.

**Decisión propuesta:** ADR-003 recomienda la interacción síncrona mínima y
mantener diferida la alternativa asíncrona. Continúa pendiente la ratificación
humana del equipo.

Documento completo: docs/integracion/sincrono-vs-asincrono.md.

## 5. Contrato experimental

Archivo:

- docs/integracion/contrato-api.yaml

Operación propuesta:

~~~http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
~~~

Proveedor lógico: **Ofertas**.

Consumidor lógico: **Mensajería**.

Respuesta mínima:

~~~json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
~~~

La reducción respecto al borrador inicial está registrada antes de medir en
`experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md`.

El contrato NO permite modificar la oferta, aplicar a ella, cambiar perfiles ni enviar mensajes.

## 6. Eventos candidatos y filtro crítico

Archivo:

- docs/integracion/eventos-candidatos.md

Aceptados como candidatos —sin afirmar que ya se publiquen—:

- OfertaPublicada;
- OfertaActualizada;
- OfertaRetirada;
- PostulacionCreada;
- PostulacionCancelada;
- ChatCreado;
- MensajeEnviado.

Rechazados o marcados como inventados/redundantes:

- BotonEnviarMensajePresionado;
- PantallaChatAbierta;
- ApiConsultada;
- JdbcActualizado;
- LatestMessageUpdated como evento independiente;
- MessageRead;
- NotificationSent;
- StudentHired;
- CompanyVerified;
- JobMatched.

## 7. CQRS y Event Sourcing

Archivo:

- docs/integracion/cqrs-event-sourcing.md

Conclusión posterior al spike:

- **CQRS completo:** no justificado con la evidencia actual.
- **Event Sourcing:** no justificado con la evidencia actual.
- **Consistencia eventual:** podría ser aceptable para proyecciones derivadas, no para autorización, persistencia del mensaje o unicidad de postulación.

Spike 1 no necesitó estado replicado y respaldó la consulta síncrona concreta.
Estas conclusiones se reabren si aparece evidencia de disponibilidad
independiente, despliegue separado o proyecciones que compensen su costo.

## 8. Registro crítico de IA

Archivo:

- docs/integracion/registro-critico-ia.md

Se conserva explícitamente qué propuestas fueron aceptadas para análisis, cuáles se rechazaron y por qué. Ningún broker, evento o patrón se incorpora solo por recomendación de IA.

## 9. Spike 1

El preregistro vive en:

~~~text
experimentos/spike-01-integracion/00-preregistro.md
~~~

Regla temporal obligatoria:

~~~text
modelo/contrato
    ↓
hipótesis escrita
    ↓
COMMIT DE PREREGISTRO
    ↓
rama experimental
    ↓
implementación
    ↓
medición
    ↓
veredicto
    ↓
ADR-003 de M5
~~~

La regla temporal se cumplió. La secuencia verificable es:

~~~text
804d2d2  preregistro original
de83109  enmienda de alcance antes de medir
4b1c43d  implementación experimental
1e8bc87  corrección del runner y commit medido
b2d59f1  resultados y capturas
4072eba  veredicto posterior
~~~

Resultado oficial:

| Corrida | Uso | p95 | Fallos inesperados |
| ---: | --- | ---: | ---: |
| 1 | calentamiento | 5,779 ms | 0 % |
| 2 | válida | 4,781 ms | 0 % |
| 3 | válida | 4,360 ms | 0 % |
| 4 | válida | 4,710 ms | 0 % |

Los controles de contrato, `401` y `404` pasaron en las cuatro corridas. La
evidencia completa está en `experimentos/spike-01-integracion/`.

## 10. Checklist S9

| Requisito | Evidencia |
| --- | --- |
| Mapear subdominios y bounded contexts | docs/dominio/ |
| Explicar responsabilidades | responsabilidades-contextos.md |
| Context Map con relaciones | context-map.md + context-map.puml |
| Diseñar contrato API | docs/integracion/contrato-api.yaml |
| Comparar síncrono/asíncrono | docs/integracion/sincrono-vs-asincrono.md |
| Filtrar eventos de IA | eventos-candidatos.md + registro-critico-ia.md |
| Evaluar CQRS/ES | cqrs-event-sourcing.md |
| Preregistrar Spike 1 | experimentos/spike-01-integracion/00-preregistro.md |
| PR S9 | esta rama/PR |

## 11. Qué puede y qué no puede afirmarse

- Sí puede afirmarse que el contrato síncrono cumplió el criterio
  preregistrado bajo las condiciones locales del spike.
- No puede afirmarse que la alternativa asíncrona sea universalmente peor.
- No puede afirmarse que CQRS nunca vaya a ser útil.
- El endpoint experimental no sustituye todavía el flujo productivo.
- ADR-003 está redactado y respaldado, pero pendiente de ratificación humana.

El p95 local tampoco se extrapola como SLO de producción o de una topología
distribuida.
