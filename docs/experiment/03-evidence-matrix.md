# Matriz de evidencia S1–S4

Fecha de preparación: 2026-08-20. “Preparado” significa que existe el artefacto, no que ya se obtuvo una corrida externa.

| Semana | Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- | --- |
| S1 | Sistema real identificado | Cumple | `README.md`: actores, flujos y arquitectura |
| S1 | Categoría confirmada | Confirmada por el equipo | Aplicación móvil; anexar evidencia del docente en la entrega |
| S1 | Hipótesis antes de medir | Preparada, no sellada | `01-preregistration.md`; crear el commit previo |
| S1 | Métrica y escenario | Preparado | p95, 500 ms, 40 solicitudes y concurrencia 10 |
| S1 | Semilla identificada | Preparado | 10.000 ofertas, 90 % activas, ~1 KiB |
| S1 | Presión externa | Preparado | SLA 250 ms, 50.000 ofertas, Internet o incumplimiento |
| S2 | Base con código existente | Cumple | Aplicación Kotlin/Compose migrada a API REST |
| S2 | Instrucciones para arrancar | Preparado | `README.md` y `docker-compose.yml` |
| S2 | Pruebas reales | Preparado | reglas Android e integración completa de API/PostgreSQL |
| S2 | CI en verde | Workflow preparado | `.github/workflows/ci.yml`; falta observar el primer check en GitHub |
| S2 | Persistencia PostgreSQL 16 | Cumple en código | imagen 16.14, Flyway, restricciones e índices |
| S2 | Semilla sesgada | Preparada | `experiments/postgresql/seed.sql`; falta ejecutar con Docker |
| S3 | Instrumentación | Preparada | `run_experiment.py` y `metrics.py` sin dependencias externas |
| S3 | Escenario y umbral | Preparado | `01-preregistration.md` |
| S3 | Script reproducible | Preparado | `02-protocol.md` |
| S3 | Validez del instrumento | Verificada parcialmente | pruebas unitarias; la verificación HTTP ocurre antes de medir |
| S4 | Línea base | Pendiente deliberado | ejecutar solo después del commit de prerregistro |
| S4 | ≥3 corridas y primera descartada | Automatizado | cuatro corridas, primera descartada |
| S4 | Condiciones registradas | Automatizado | JSON incluye máquina, carga, código y duración |
| S4 | Comparación con H1 | Automatizado | `decision` es `supported` o `not_supported` |

## Verificaciones durante la preparación

- Backend Kotlin y pruebas: `compileKotlin compileTestKotlin` → **BUILD SUCCESSFUL**.
- Android: `:app:compileDebugKotlin` → **BUILD SUCCESSFUL** con el SDK 36. La fase Java completa fue interrumpida después por una restricción de acceso a JAR del entorno de preparación; el workflow de GitHub cierra `test`, `lint` y APK en Linux.
- Instrumento: tres pruebas de cálculo aprobadas y ambos scripts validados por el compilador de Python.
- Docker: no está instalado en el entorno de preparación; por honestidad no se inventa una línea base.
