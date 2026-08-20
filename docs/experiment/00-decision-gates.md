# Registro de decisiones

Estado: **alcance técnico decidido por el equipo; categoría pendiente de evidencia externa**.

| Decisión | Estado | Evidencia disponible |
| --- | --- | --- |
| Categoría del catálogo | **Pendiente de confirmación auditable** | El equipo propone aplicación móvil por la estructura Android, pero falta captura, mensaje o acta del docente |
| Persistencia | Confirmada: **PostgreSQL 16** | El docente indicó usar una base de datos robusta; Firebase fue retirado |
| Datos iniciales | Confirmados: **sintéticos** | Decisión del equipo del 2026-08-19 |
| Despliegue de esta entrega | Confirmado: **local** | Android → API local → PostgreSQL 16; Internet queda como evolución futura |
| Escenario de calidad | Confirmado: consulta paginada de ofertas | Definido en `01-preregistration.md` |

## Evidencia que conviene anexar

El repositorio registra las decisiones del equipo, pero no puede sustituir la
confirmación del docente. Antes de cerrar S1 y la validez de S3/S4, anexar en la
plataforma del curso o enlazar aquí una captura, mensaje o acta donde conste la
categoría. Si se confirma **aplicación móvil**, complementar el generador HTTP con
una medición Android de extremo a extremo.

## Sello previo

El prerregistro fue sellado en `03c61d0320cbf8658c0500857ad92154ee2d9da1`
antes de la línea base histórica. Las nuevas corridas deben conservar ese vínculo
y generar archivos distintos, sin reemplazar evidencia previa.
