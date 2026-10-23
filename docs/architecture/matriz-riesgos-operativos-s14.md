# Matriz de riesgos operativos — S14

Escala: probabilidad e impacto 1–5; riesgo = P×I. P0=16–25, P1=9–15, P2=1–8.

| ID | Riesgo operativo | P | I | Nivel | Señal | Prevención | Recuperación / riesgo residual |
| --- | --- | ---: | ---: | ---: | --- | --- | --- |
| O01 | PostgreSQL no disponible | 3 | 5 | **15 P1** | health, errores JDBC, pool pending | DB administrada, backups, health | restaurar/failover según proveedor; API degrada mientras DB no responde |
| O02 | instancia API cae | 3 | 4 | **12 P1** | health DOWN, 5xx/conexión | restart/health, despliegue reproducible | reemplazar instancia; alternativa B reduce impacto |
| O03 | almacenamiento de documentos lleno/no disponible | 3 | 4 | **12 P1** | storage %, IOException | object storage/quotas/alerta | preservar metadata, reintento controlado; no perder DB |
| O04 | secret/config incorrecto | 3 | 5 | **15 P1** | startup fail, auth/integration errors | secret manager + perfiles | rollback/rotación; nunca fallback a secreto demo en producción |
| O05 | publicación sin TLS o con perfil demo | 2 | 5 | **10 P1** | gate de deploy/config | HTTPS y checklist S13 | bloquear promoción; no “arreglar” después de exponer |
| O06 | 5xx crecen sin alarma | 3 | 4 | **12 P1** | error rate | observabilidad/alerta >5 % | triage por traceId/error.type |
| O07 | latencia supera SLO | 3 | 4 | **12 P1** | p95 >500 ms | capacity monitoring | localizar API/pool/DB antes de escalar |
| O08 | migración Flyway falla | 2 | 5 | **10 P1** | startup/migration log | migraciones versionadas + CI | nueva migración correctiva/restore; no borrar volumen |
| O09 | dependencia/imagen defectuosa | 2 | 5 | **10 P1** | build/deploy failure | versiones, CI, revisión supply chain | rollback a versión conocida |
| O10 | backup no restaurable | 2 | 5 | **10 P1** | prueba restore/edad backup | política y ensayo de restauración | riesgo crítico residual hasta probar restore |
| O11 | polling/tráfico crece más que capacidad | 3 | 3 | **9 P1** | request rate, p95 | medir + backoff/rate control | escalar API o cambiar transporte solo con atribución |
| O12 | observabilidad expone PII/secrets | 2 | 5 | **10 P1** | revisión/log scan | denylist de campos sensibles | rotar secreto si se filtra; corregir pipeline |
| O13 | una sola API es SPOF | 3 | 3 | **9 P1** | health/down time | alternativa B preparada | aceptar inicialmente; redecidir por SLO disponibilidad |
| O14 | fitness function bloquea cambio legítimo | 2 | 2 | **4 P2** | CI rojo esperado | procedimiento de re-decisión | actualizar ADR + regla en mismo PR |

## Prioridades

Antes de un despliegue público:

1. O04/O05 — configuración y secrets/TLS;
2. O01/O10 — DB, backup y recuperación;
3. O03 — almacenamiento de documentos;
4. O06/O07 — alertas y SLO;
5. O12 — higiene de telemetría.

## Relación con S11–S13

- S11 aporta el SLO p95=500 ms y evidencia de capacidad local.
- S12 aporta costo y decisión de no distribuir prematuramente.
- S13 aporta amenazas de seguridad y controles previos a Internet.
- S14 convierte esos riesgos en señales, alertas, despliegue y recuperación.
