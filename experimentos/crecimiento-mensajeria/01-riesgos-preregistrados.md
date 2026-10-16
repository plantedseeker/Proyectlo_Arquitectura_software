# Riesgos y fallos candidatos antes de medir

Este documento enumera hipótesis de fallo; **no contiene resultados**. Su
propósito es impedir que la explicación se invente después de ver las métricas.

| ID | Fallo candidato | Señal primaria | Evidencia de apoyo | Posible táctica, no seleccionada |
| --- | --- | --- | --- | --- |
| G01 | Agotamiento del pool JDBC | p95 crece y throughput deja de escalar; presión en API/DB | pool configurado con máximo 10 conexiones | ajustar consultas/pool solo si los datos lo justifican |
| G02 | PostgreSQL se vuelve el límite de lectura | latencia y CPU/memoria de DB crecen antes que k6 | operación sobre conversación de 100.000 mensajes | caché o cambio de consulta/índice según el plan observado |
| G03 | API se satura antes que la base | CPU/memoria de API y p95 aumentan con DB estable | telemetría separada de contenedores | límite de concurrencia, escalado o timeout según la causa |
| G04 | k6 limita la medición | throughput se aplana con presión del generador y servidor estable | métricas k6 y telemetría Docker | separar el generador o reducir el alcance de la conclusión |
| G05 | Equipo compartido contamina la atribución | API, DB y k6 se presionan simultáneamente | contexto de máquina y telemetría | repetir con generador separado; declarar límite del entorno |
| G06 | Aparecen rechazos o errores | HTTP 429/5xx o tasa de fallo mayor que cero | conteos por código | backoff/timeout; no aplicar retry ciego ante saturación |
| G07 | Respuesta rápida pero contrato inválido | HTTP 200 con página distinta de 50 mensajes | check de contrato | corregir consistencia/consulta antes de optimizar rendimiento |

La matriz de fallos final añadirá ocurrencia, primer escalón, severidad y
evidencia concreta. La matriz de resiliencia se construirá después del primer
recorrido y comparará tácticas contra los fallos realmente observados.
