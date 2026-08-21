# Matriz de evidencia S1–S4

Fecha de preparación: 2026-08-20. “Preparado” significa que existe el artefacto, no que ya se obtuvo una corrida externa.

| Semana | Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- | --- |
| S1 | Sistema real identificado | Cumple | `README.md`: actores, flujos y arquitectura |
| S1 | Categoría confirmada | Confirmada | Mensajería y mesa de ayuda; anexar la comunicación original del profesor |
| S1 | Hipótesis antes de medir | Preparada para sellar | `dossier/04-escenarios-calidad.md`; aún no ejecutar la corrida de mensajería |
| S1 | Métrica y escenario | Preparado | últimos 50 de 100.000 mensajes, p95 500 ms, 40 solicitudes y 10 VU |
| S1 | Semilla identificada | Preparado | 1.000 conversaciones y 289.000 mensajes con distribución 900/90/9/1 |
| S1 | Presión externa | Preparado | SLA 250 ms, más de 100.000 mensajes, Internet, páginas profundas o tiempo real |
| S2 | Base con código existente | Cumple | Aplicación Kotlin/Compose migrada a API REST |
| S2 | Instrucciones para arrancar | Verificadas | `README.md`, Docker Compose y scripts PowerShell; mensajería pendiente de corrida final |
| S2 | Pruebas reales | Cumple | reglas Android, pruebas del instrumento e integración API/PostgreSQL |
| S2 | CI en verde | Cumple | `UTrabajo CI` aprobó Android y backend en la [corrida 32343026489](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/32343026489) |
| S2 | Persistencia PostgreSQL 16 | Cumple | imagen 16.14, Flyway, restricciones e índices verificados localmente |
| S2 | Semilla sesgada | Preparada | `seed-mensajeria.sql` reproduce conversaciones pequeñas, medianas, grandes y extrema |
| S3 | Instrumentación | En actualización | k6 y semilla de mensajería preparados en `/experimentos/medicion-escenario-01` |
| S3 | Escenario y umbral | Cumple | `dossier/03-atributos-calidad.md` y `04-escenarios-calidad.md` |
| S3 | Script reproducible | Preparado | k6, agregador y `scripts/run-messaging-baseline.ps1` |
| S3 | Validez del instrumento | En actualización | El nuevo instrumento mide últimos 50 mensajes de la conversación extrema, operación propia de la categoría confirmada |
| S4 | Línea base de mensajería | Pendiente | ejecutar después del commit de sellado; las líneas base de ofertas quedan históricas |
| S4 | ≥3 corridas y primera descartada | Pendiente | k6 está configurado para cuatro corridas |
| S4 | Mediana de corridas válidas | Pendiente | el agregador calculará corridas 2–4 |
| S4 | Condiciones registradas | Preparado | máquina, energía, plan, carga, versión, duración y alcance se guardan automáticamente |
| S4 | Topología de la medición | Preparado | k6 + API + PostgreSQL compartirán Docker Desktop y equipo físico |
| S4 | Comparación con H1 | Pendiente | completar en `dossier/04-escenarios-calidad.md` con el dato real de k6 |

## Verificaciones durante la preparación

- Backend Kotlin: imagen construida con JDK 21 y API saludable contra PostgreSQL 16.
- Android: `test`, `lint` y APK aprobados con SDK 36 en GitHub Actions.
- Backend: pruebas de integración contra PostgreSQL 16 aprobadas en GitHub Actions.
- Instrumento: cinco pruebas de cálculo aprobadas; la versión 2 registra mediana, máquina, energía, topología y límites de alcance.
- Docker Desktop 4.87.0 y Engine 29.7.2: PostgreSQL 16.14 y API ejecutados localmente; líneas base conservadas en `results/baseline.json` y `results/baseline-s4-audit.json`.
