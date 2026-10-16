# Matriz de resiliencia y selección de táctica — S11

| Táctica | Fallo al que respondería | Beneficio esperado | Riesgo/costo | Respaldo en S11 | Estado |
| --- | --- | --- | --- | --- | --- |
| Caché local TTL para última página | lecturas repetidas y crecimiento de latencia | reducir consultas repetidas y p95 en una conversación caliente | memoria, invalidación, datos obsoletos, beneficio menor con muchos chats y coherencia por nodo | Parcial: la latencia fue la única señal creciente | **Seleccionada solo para experimento antes/después** |
| Timeout | esperas bloqueadas o latencia fuera de presupuesto | liberar recursos ante respuestas demasiado lentas | falsos fallos y no reduce trabajo ya iniciado | No hubo timeout; p95 máximo mediano 58,235 ms | Rechazada por falta de señal |
| Retry | fallos transitorios | recuperar una operación idempotente | tormenta de reintentos y mayor carga | 0 fallos HTTP | Rechazada; puede empeorar saturación |
| Circuit breaker | dependencia remota fallando repetidamente | cortar llamadas condenadas a fallar | estado adicional y falsos abiertos | PostgreSQL local no presentó fallos | Rechazada por falta de señal |
| Bulkhead | agotamiento de recursos compartidos | aislar mensajería de otras funciones | pools/colas adicionales y operación más compleja | no se observó agotamiento | Diferida |
| Aumentar pool JDBC | cola por falta de conexiones | más paralelismo hacia DB | sobrecarga DB y conexiones ociosas | no se demostró agotamiento del pool | Rechazada sin métrica de pool |
| Escalar API o separar mensajería | límite de capacidad del proceso | más capacidad/aislamiento | despliegue, red, observabilidad y costo operativo | no apareció límite hasta 200 VU | No justificada por S11 |
| SSE/WebSocket o backoff de polling | multiplicación de consultas periódicas | reducir solicitudes repetidas del cliente | conexión persistente, operación y estado adicional | riesgo F12, pero esta prueba fue una ráfaga y no un ensayo de transporte | Mantener como alternativa futura |

## Regla de adopción de la caché

La táctica solo se considerará respaldada por el experimento si, a 200 VU:

1. mantiene HTTP 200, cero fallos y páginas de 50 mensajes;
2. demuestra hits y al menos una invalidación correcta en pruebas;
3. reduce el p95 mediano frente a 58,235 ms sin reducir throughput;
4. no exige asumir coherencia entre varios nodos.

Aunque mejore la medición, puede rechazarse para producción si el beneficio
depende de que todos los usuarios consulten la misma conversación. El resultado
servirá para discutir la decisión de distribución, no para predeterminarla.
