# Matriz de evidencia S1–S4

Fecha de preparación: 2026-08-20. “Preparado” significa que existe el artefacto, no que ya se obtuvo una corrida externa.

| Semana | Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- | --- |
| S1 | Sistema real identificado | Cumple | `README.md`: actores, flujos y arquitectura |
| S1 | Categoría confirmada | Confirmada por el equipo | Aplicación móvil; anexar evidencia del docente en la entrega |
| S1 | Hipótesis antes de medir | Sellada | `01-preregistration.md`; commit `03c61d0` publicado antes de medir |
| S1 | Métrica y escenario | Preparado | p95, 500 ms, 40 solicitudes y concurrencia 10 |
| S1 | Semilla identificada | Preparado | 10.000 ofertas, 90 % activas, ~1 KiB |
| S1 | Presión externa | Preparado | SLA 250 ms, 50.000 ofertas, Internet o incumplimiento |
| S2 | Base con código existente | Cumple | Aplicación Kotlin/Compose migrada a API REST |
| S2 | Instrucciones para arrancar | Verificadas | `README.md`, `docker-compose.yml` y `scripts/run-local-baseline.ps1` ejecutados en Windows |
| S2 | Pruebas reales | Cumple | reglas Android, pruebas del instrumento e integración API/PostgreSQL |
| S2 | CI en verde | Workflow preparado | `.github/workflows/ci.yml`; falta observar el primer check en GitHub |
| S2 | Persistencia PostgreSQL 16 | Cumple | imagen 16.14, Flyway, restricciones e índices verificados localmente |
| S2 | Semilla sesgada | Verificada | 10.000 sintéticas, 9.000 activas y descripciones de carga en PostgreSQL 16 |
| S3 | Instrumentación | Verificada | `run_experiment.py` y `metrics.py` sin dependencias externas |
| S3 | Escenario y umbral | Cumple | `01-preregistration.md` |
| S3 | Script reproducible | Verificado | `02-protocol.md` y automatización PowerShell ejecutada |
| S3 | Validez del instrumento | Verificada | pruebas unitarias y comprobación HTTP ejecutadas antes de medir |
| S4 | Línea base | Cumple | `results/baseline.json`, capturada después del commit de prerregistro |
| S4 | ≥3 corridas y primera descartada | Cumple | cuatro corridas; corrida 1 marcada `discarded: true` |
| S4 | Condiciones registradas | Cumple | JSON incluye máquina, carga, revisión de código y duración |
| S4 | Comparación con H1 | Cumple | `decision: supported`; p95 válidos entre 22.430 y 41.364 ms |

## Verificaciones durante la preparación

- Backend Kotlin: imagen construida con JDK 21 y API saludable contra PostgreSQL 16.
- Android: `:app:compileDebugKotlin` → **BUILD SUCCESSFUL** con el SDK 36. La fase Java completa fue interrumpida después por una restricción de acceso a JAR del entorno de preparación; el workflow de GitHub cierra `test`, `lint` y APK en Linux.
- Instrumento: tres pruebas de cálculo aprobadas y ambos scripts validados por el compilador de Python.
- Docker Desktop 4.87.0 y Engine 29.7.2: PostgreSQL 16.14 y API ejecutados localmente; línea base real conservada en `results/baseline.json`.
