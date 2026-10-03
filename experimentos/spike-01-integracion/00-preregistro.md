# Spike 1 — preregistro de integración Mensajería → Ofertas

> **FRONTERA TEMPORAL:** este archivo debe existir en Git antes de escribir código experimental del Spike 1. El campo Resultado permanece vacío hasta ejecutar y medir el spike.

## Pregunta experimental

¿Un contrato REST síncrono explícito, limitado a la información mínima que Mensajería necesita de Ofertas para crear una conversación, conserva un comportamiento funcional correcto y un p95 dentro del umbral experimental bajo una carga controlada equivalente en forma a la línea base S4?

## Hipótesis

Bajo las condiciones registradas del Spike 1, el contrato síncrono:

- responderá correctamente para una oferta existente;
- rechazará credenciales internas inválidas;
- responderá 404 para una oferta inexistente;
- mantendrá la operación válida con **p95 <= 500 ms** en las corridas consideradas válidas;
- no requerirá introducir broker, proyección replicada ni consistencia eventual para satisfacer esta necesidad concreta.

## Variable observada

Principal:

- p95 de la operación GET del contrato experimental.

Secundarias:

- tasa de checks;
- tasa de errores HTTP inesperados;
- número de solicitudes medidas;
- validez de campos jobId, companyId, active y title;
- códigos esperados para casos negativos.

## Criterio de aceptación

La hipótesis queda **respaldada para las condiciones del spike** si:

1. tres corridas válidas cumplen p95 <= 500 ms;
2. todos los checks funcionales de esas corridas pasan;
3. la tasa de fallos HTTP inesperados es 0;
4. cada corrida válida contiene 40 solicitudes medidas;
5. los casos negativos verifican rechazo de token inválido y 404 de oferta inexistente;
6. no se modifica el modelo de datos para conseguir el resultado.

## Criterio de refutación

La hipótesis queda **refutada** si ocurre al menos uno:

- p95 > 500 ms en alguna corrida válida según el método registrado;
- errores funcionales invalidan la comparación;
- el contrato expone más información de la necesaria para Mensajería;
- la seguridad experimental no distingue credencial válida/ inválida;
- la prueba solo funciona cambiando semilla, autorización o condiciones después de observar resultados.

Una refutación limpia y reproducible se considera un resultado válido del experimento.

## Alcance acotado

Incluye únicamente:

- una representación experimental del contrato Ofertas → Mensajería;
- lectura de jobId, companyId, active y title;
- autenticación interna mínima para el endpoint experimental;
- script k6 del contrato;
- automatización de medición y agregación;
- casos positivos y negativos del contrato.

No incluye:

- Kafka, RabbitMQ u otro broker;
- microservicios;
- Redis;
- WebSocket;
- notificaciones;
- CQRS completo;
- Event Sourcing;
- migración del flujo productivo createOrGetChat al contrato experimental;
- cambios de esquema.

## Condiciones que no se modifican después de medir

- PostgreSQL 16;
- datos demo deterministas del repositorio;
- usuario demo y credenciales existentes;
- 10 VU;
- 40 iteraciones por corrida;
- 4 corridas;
- corrida 1 descartada como calentamiento;
- corridas 2, 3 y 4 válidas;
- umbral p95 <= 500 ms;
- imagen k6 0.54.0;
- validación de 200 + payload correcto en la operación medida.

## Contrato bajo prueba

~~~http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
~~~

Proveedor lógico: **Ofertas**.

Consumidor lógico: **Mensajería**.

Contrato detallado: docs/integracion/contrato-api.yaml.

## Relación con evidencia previa

- S4 demuestra la disciplina de línea base HTTP reproducible.
- S7 demuestra que una decisión debe localizar el costo antes de introducir complejidad.
- ADR-001 mantiene monolito modular.
- ADR-002 protege dependencias internas.
- ADR-004 conserva la decisión de paginación.
- Este spike alimentará **ADR-003 de Módulo 5**, que aún no debe redactar una decisión final.

## Rama experimental prevista

~~~text
spike/s10-integracion-mensajeria-ofertas
~~~

La rama se creará únicamente después de que este preregistro esté fusionado o tenga un commit verificable anterior a la implementación.

## Resultado

**VACÍO — el experimento aún no se ha ejecutado.**

No completar esta sección antes de implementar y medir el Spike 1.
