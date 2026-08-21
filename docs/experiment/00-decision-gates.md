# Registro de decisiones

Estado: **categoría confirmada por el profesor y experimento de mensajería ejecutado**.

| Decisión | Estado | Evidencia disponible |
| --- | --- | --- |
| Categoría del catálogo | Confirmada: **Mensajería y mesa de ayuda** | Confirmación del profesor reportada por el equipo el 2026-08-20; anexar la comunicación original |
| Persistencia | Confirmada: **PostgreSQL 16** | El docente indicó usar una base de datos robusta; Firebase fue retirado |
| Datos iniciales | Confirmados: **sintéticos** | Decisión del equipo del 2026-08-19 |
| Despliegue de esta entrega | Confirmado: **local** | Android → API local → PostgreSQL 16; Internet queda como evolución futura |
| Escenario de calidad | Confirmado: últimos 50 mensajes de una conversación extrema | Definido en `dossier/04-escenarios-calidad.md` |

## Evidencia que conviene anexar

El repositorio registra la confirmación reportada, pero no puede sustituir la
comunicación original del profesor. Antes de entregar, anexar una captura, mensaje
o acta. El experimento principal se trasladó a conversaciones con distribución
extrema y está documentado en `dossier/` y `experimentos/medicion-escenario-01/`.

## Sello previo

El prerregistro de ofertas fue sellado en `03c61d0` antes de su línea base
histórica. El escenario de mensajería se selló por separado en `e7e84d9`, antes
de ejecutar k6. El resultado principal está en
`experimentos/medicion-escenario-01/resultados/`; ambas evidencias se conservan
sin reemplazarse.
