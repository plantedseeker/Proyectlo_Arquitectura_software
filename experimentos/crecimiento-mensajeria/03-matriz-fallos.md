# Matriz de fallos observados — S11

| ID | Fallo o presión evaluada | Evidencia observada | Primer escalón | Severidad en alcance | Conclusión |
| --- | --- | --- | ---: | --- | --- |
| G01 | Agotamiento del pool JDBC de 10 conexiones | p95 creció, pero hubo 0 errores y el throughput continuó aumentando | No observado hasta 200 VU | No materializada | No se puede afirmar agotamiento del pool |
| G02 | PostgreSQL como límite de lectura | p95 mediano máximo 58,235 ms; muestras DB escasas y sin presión sostenida verificable | No observado hasta 200 VU | No materializada | No atribuir el crecimiento de latencia a DB |
| G03 | API como límite | 0 respuestas 5xx/429 y throughput máximo 2.303,017 req/s | No observado hasta 200 VU | No materializada | No atribuir saturación a la API |
| G04 | k6 como límite | throughput siguió creciendo; telemetría k6 demasiado corta para atribución | No observado; instrumentación inconclusa | Incertidumbre de medición | Repetir con carga sostenida o generador separado si se requiere localizar el límite |
| G05 | Equipo compartido como límite | sin aplanamiento conjunto ni fallos; solo dos muestras Docker por corrida | No observado hasta 200 VU | No materializada | No hay base para atribuir un límite al host |
| G06 | Rechazos o errores HTTP | 6.120/6.120 solicitudes válidas fueron HTTP 200; 0 errores | Ninguno | Nula en el alcance | Retry/timeout no están justificados por el dato |
| G07 | Contrato inválido bajo carga | todas las respuestas contenían exactamente 50 mensajes | Ninguno | Nula en el alcance | Contrato conservado |
| G08 | Aumento relativo de latencia | p95 mediano 8,963 → 58,235 ms entre 10 y 200 VU | 50 VU como primera subida visible | P1 potencial, sin incumplir SLA | Primera presión observable; no equivale a saturación |

La matriz separa una tendencia de latencia de un fallo real. El criterio
prerregistrado exigía dos de tres corridas válidas por encima de 500 ms o con
errores/contrato inválido; ningún escalón lo cumplió.
