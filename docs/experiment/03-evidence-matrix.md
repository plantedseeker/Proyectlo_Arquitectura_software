# Matriz de evidencia S1–S4

Fecha de corte: 2026-08-20, hora de Colombia. La medición terminó a las
`2026-08-21T03:32:21Z` y sus archivos primarios están versionados en el
repositorio.

| Semana | Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- | --- |
| S1 | Sistema real identificado | Cumple | `README.md`: actores, flujos y arquitectura |
| S1 | Categoría confirmada | Confirmada | Mensajería y mesa de ayuda; anexar la comunicación original del profesor |
| S1 | Hipótesis antes de medir | Sellada | `dossier/04-escenarios-calidad.md`; commit previo `e7e84d9` |
| S1 | Métrica y escenario | Cumple | últimos 50 de 100.000 mensajes, p95 500 ms, 40 solicitudes y 10 VU |
| S1 | Semilla identificada | Cumple | 1.000 conversaciones y 289.000 mensajes con distribución 900/90/9/1, verificada contra PostgreSQL |
| S1 | Presión externa | Preparado | SLA 250 ms, más de 100.000 mensajes, Internet, páginas profundas o tiempo real |
| S2 | Base con código existente | Cumple | Aplicación Kotlin/Compose migrada a API REST |
| S2 | Instrucciones para arrancar | Verificadas | `README.md`, Docker Compose y `scripts/run-messaging-baseline.ps1`, ejecutado de extremo a extremo |
| S2 | Pruebas reales | Cumple | reglas Android, pruebas del instrumento e integración API/PostgreSQL |
| S2 | CI en verde | Cumple | `UTrabajo CI` aprobó Android, backend e instrumento en la [corrida del PR #1](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/32444217625) |
| S2 | Persistencia PostgreSQL 16 | Cumple | imagen 16.14, Flyway, restricciones e índices verificados localmente |
| S2 | Semilla sesgada | Cumple | `seed-mensajeria.sql` y `verificacion-semilla.json`: 900×10, 90×1.000, 9×10.000 y 1×100.000 |
| S3 | Instrumentación | Cumple | `grafana/k6:0.54.0` ejecutó cuatro corridas en `/experimentos/medicion-escenario-01` |
| S3 | Escenario y umbral | Cumple | `dossier/03-atributos-calidad.md` y `04-escenarios-calidad.md` |
| S3 | Script reproducible | Cumple | k6, agregador y `scripts/run-messaging-baseline.ps1` produjeron los archivos auditados |
| S3 | Validez del instrumento | Cumple | Mide los últimos 50 mensajes de la conversación extrema, devuelve 50 elementos y corresponde a la categoría confirmada |
| S4 | Línea base de mensajería | Cumple | `resultado.json`, revisión `c7c0edd`; las líneas base de ofertas quedan históricas |
| S4 | ≥3 corridas y primera descartada | Cumple | cuatro corridas de 40 solicitudes; corrida 1 descartada y corridas 2–4 válidas |
| S4 | Mediana de corridas válidas | Cumple | p95 válidos: 9,451; 8,265; 9,109 ms; mediana: **9,109 ms** |
| S4 | Condiciones registradas | Cumple | Acer Predator PH16-71, i9-13900HX, 95,7 GiB, 32 CPU; corriente, 80 %, plan Equilibrado |
| S4 | Topología de la medición | Cumple | k6 + API + PostgreSQL 16.14 compartieron Docker Desktop y el mismo equipo físico |
| S4 | Comparación con H1 | Cumple | mediana 9,109 ms y cada corrida válida ≤ 500 ms; H1 respaldada para las condiciones medidas |

## Verificaciones durante la preparación

- Backend Kotlin: imagen construida con JDK 21 y API saludable contra PostgreSQL 16.
- Android: `test`, `lint` y APK aprobados con SDK 36 en GitHub Actions.
- Backend: pruebas de integración contra PostgreSQL 16 aprobadas en GitHub Actions.
- Instrumento: cinco pruebas de cálculo aprobadas; la versión 2 registra mediana, máquina, energía, topología y límites de alcance.
- Docker Desktop 4.87.0 y Engine 29.7.2: PostgreSQL 16.14 y API ejecutados localmente; líneas base conservadas en `results/baseline.json` y `results/baseline-s4-audit.json`.
- Mensajería: k6 completó cuatro corridas con 160 solicitudes totales, 100 % de
  comprobaciones y 0 % de fallos; el conteo SQL de la semilla coincidió con el
  prerregistro.
