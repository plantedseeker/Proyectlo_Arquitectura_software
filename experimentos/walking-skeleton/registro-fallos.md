# Registro de fallos controlados y observados

Este registro separa observaciones reales de riesgos hipotéticos. No se marca
una recuperación como exitosa hasta repetir el walking skeleton completo.

| Fecha | ID | Tipo | Síntoma observado | Causa confirmada | Acción | Recuperación | Pérdida de datos |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 2026-09-11 | F02 | Observado durante el primer arranque | Docker no pudo enlazar `0.0.0.0:5432`: `port is already allocated` | El stack anterior `proyectlo_arquitectura_software` publicaba 5432 y 8080 mediante sus contenedores `db-1` y `api-1` | Se detuvo únicamente ese proyecto con `docker compose down`, sin `-v`; se añadió detección previa de propietarios | Recuperado: ejecución posterior `supported`, API `UP` y PostgreSQL aceptando conexiones | Ninguna eliminación de volúmenes |
| 2026-09-11 | F03 | Observado tras el arranque parcial | La API reiniciaba con `UnknownHostException: db` | Estado parcial de contenedores/red después del fallo anterior; el wrapper también absorbía `-d` como parámetro de PowerShell | Detener la vista, ejecutar `docker compose down` sin `-v`, corregir el paso de argumentos y recrear la red | Recuperado: diez controles `PASS`, mensaje persistido una vez y marcador visible en Android | Ninguna eliminación de volúmenes |

## Datos mínimos para cerrar un registro

1. Salida del comando que identifica la causa.
2. Acción exacta de recuperación.
3. JSON del walking skeleton posterior con `decision: supported`.
4. Confirmación de que el mensaje quedó visible y no se perdieron datos.
