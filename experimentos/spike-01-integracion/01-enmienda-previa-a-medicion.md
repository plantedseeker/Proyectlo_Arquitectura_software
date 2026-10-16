# Enmienda previa a la medición — payload mínimo

Fecha de decisión: `2026-10-16T15:58:53-05:00`.

Prerregistro original: commit `804d2d295b45d241abe05d78ef0892e1dd96a784`.

Estado temporal: al detectar esta inconsistencia ya existía un borrador local no ejecutado del endpoint, pero todavía no se había compilado, confirmado en Git ni medido. La decisión se toma a partir de la auditoría documental, sin observar resultados. Esta enmienda se confirmará antes del commit de implementación medido y no altera las condiciones de carga ni los umbrales del prerregistro original.

## Inconsistencia detectada

El prerregistro formula la pregunta sobre la información mínima que Mensajería necesita para **crear una conversación**, pero enumera cuatro campos: `jobId`, `companyId`, `active` y `title`.

La evidencia previa en `docs/dominio/responsabilidades-contextos.md` distingue dos necesidades:

- para crear una conversación: `jobId`, `companyId` y existencia de la oferta;
- para listar conversaciones: también se usa actualmente `jobTitle`.

Además, la existencia ya queda representada por la semántica HTTP: `200` cuando la oferta existe y `404` cuando no existe. El flujo actual de creación no consulta `active`.

Mantener `active` y `title` en este experimento contradiría el criterio de refutación que prohíbe exponer más información de la necesaria.

## Ajuste preregistrado

El payload medido se reduce a:

~~~json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
~~~

Los checks positivos validarán exactamente esos dos campos y comprobarán que no aparezcan `active` ni `title`.

Los casos negativos permanecen sin cambios:

- credencial interna ausente o inválida: `401`;
- oferta inexistente: `404`.

## Condiciones que permanecen congeladas

- PostgreSQL 16 y datos demo deterministas;
- 10 VU y 40 iteraciones totales por corrida mediante `shared-iterations`;
- cuatro corridas, descartando la primera como calentamiento;
- tres corridas válidas;
- p95 menor o igual a 500 ms;
- cero fallos HTTP inesperados;
- sin cambios de esquema, broker, CQRS o Event Sourcing.

Esta enmienda corrige coherencia de alcance antes de observar resultados; no es un ajuste post hoc basado en las mediciones.

El veredicto final se expresará como **ajustada** respecto al payload del borrador original, además de indicar por separado si las condiciones funcionales y de rendimiento quedaron respaldadas o refutadas.
