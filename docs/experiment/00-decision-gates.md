# Registro de decisiones

Estado: **alcance confirmado por el equipo**.

| Decisión | Estado | Evidencia disponible |
| --- | --- | --- |
| Categoría del catálogo | Confirmada: **Aplicación móvil** | El equipo reporta que el docente la asignó por la estructura del proyecto |
| Persistencia | Confirmada: **PostgreSQL 16** | El docente indicó usar una base de datos robusta; Firebase fue retirado |
| Datos iniciales | Confirmados: **sintéticos** | Decisión del equipo del 2026-08-19 |
| Despliegue de esta entrega | Confirmado: **local** | Android → API local → PostgreSQL 16; Internet queda como evolución futura |
| Escenario de calidad | Confirmado: consulta paginada de ofertas | Definido en `01-preregistration.md` |

## Evidencia que conviene anexar

El repositorio registra las decisiones, pero no puede fabricar la evidencia del docente. Antes de entregar, anexar en la plataforma del curso o enlazar aquí una captura, mensaje o acta donde consten la categoría y PostgreSQL 16.

## Sello previo

Antes de ejecutar la línea base, revisar `01-preregistration.md` y guardarlo en un commit. Escribir allí el hash de ese commit. Así se demuestra que la hipótesis existía antes de conocer los resultados.
