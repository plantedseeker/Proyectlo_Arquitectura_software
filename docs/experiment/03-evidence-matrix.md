# Matriz de evidencia S1–S4

Fecha de preparación: 2026-08-20. “Preparado” significa que existe el artefacto, no que ya se obtuvo una corrida externa.

| Semana | Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- | --- |
| S1 | Sistema real identificado | Cumple | `README.md`: actores, flujos y arquitectura |
| S1 | Categoría confirmada | **Abierto** | El equipo propone aplicación móvil, pero falta anexar la confirmación explícita del docente |
| S1 | Hipótesis antes de medir | Sellada | `01-preregistration.md`; commit `03c61d0` publicado antes de medir |
| S1 | Métrica y escenario | Preparado | p95, 500 ms, 40 solicitudes y concurrencia 10 |
| S1 | Semilla identificada | Preparado | 10.000 ofertas, 90 % activas, ~1 KiB |
| S1 | Presión externa | Preparado | SLA 250 ms, 50.000 ofertas, Internet o incumplimiento |
| S2 | Base con código existente | Cumple | Aplicación Kotlin/Compose migrada a API REST |
| S2 | Instrucciones para arrancar | Verificadas | `README.md`, `docker-compose.yml` y `scripts/run-local-baseline.ps1` ejecutados en Windows |
| S2 | Pruebas reales | Cumple | reglas Android, pruebas del instrumento e integración API/PostgreSQL |
| S2 | CI en verde | Cumple | `UTrabajo CI` aprobó Android y backend en la [corrida 32343026489](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/32343026489) |
| S2 | Persistencia PostgreSQL 16 | Cumple | imagen 16.14, Flyway, restricciones e índices verificados localmente |
| S2 | Semilla sesgada | Verificada | 10.000 sintéticas, 9.000 activas y descripciones de carga en PostgreSQL 16 |
| S3 | Instrumentación | Parcial | `run_experiment.py` mide API + PostgreSQL; no incluye todavía el cliente Android |
| S3 | Escenario y umbral | Cumple | `01-preregistration.md` |
| S3 | Script reproducible | Verificado | `02-protocol.md` y automatización PowerShell ejecutada |
| S3 | Validez del instrumento | **Abierto** | Los cálculos y HTTP están verificados; falta contrastar el alcance con la categoría confirmada y, si es móvil, medir también en Android |
| S4 | Línea base | Cumple para API + PostgreSQL | `results/baseline-s4-audit.json`, posterior al prerregistro y a la revisión medida `d247f93` |
| S4 | ≥3 corridas y primera descartada | Cumple | cuatro corridas en la evidencia v2; corrida 1 marcada `discarded: true` |
| S4 | Mediana de corridas válidas | Cumple | mediana de p95 = **19.457 ms**, calculada y guardada por el instrumento v2 |
| S4 | Condiciones registradas | Cumple | JSON v2 identifica máquina, energía, plan, carga, versión, duración y alcance durante la corrida |
| S4 | Topología de la medición | Cumple | JSON v2 declara que generador Python + API + PostgreSQL compartieron equipo físico; API y base corrieron en Docker Desktop |
| S4 | Comparación con H1 | Parcial | La ruta API + PostgreSQL apoyó H1; no se extiende la conclusión al rendimiento móvil de extremo a extremo |

## Verificaciones durante la preparación

- Backend Kotlin: imagen construida con JDK 21 y API saludable contra PostgreSQL 16.
- Android: `test`, `lint` y APK aprobados con SDK 36 en GitHub Actions.
- Backend: pruebas de integración contra PostgreSQL 16 aprobadas en GitHub Actions.
- Instrumento: cinco pruebas de cálculo aprobadas; la versión 2 registra mediana, máquina, energía, topología y límites de alcance.
- Docker Desktop 4.87.0 y Engine 29.7.2: PostgreSQL 16.14 y API ejecutados localmente; líneas base conservadas en `results/baseline.json` y `results/baseline-s4-audit.json`.
