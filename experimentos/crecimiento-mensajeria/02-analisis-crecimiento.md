# Análisis S11 — crecimiento antes de aplicar una táctica

Evidencia primaria: [`resultados/20261016T224331Z/resultado.json`](resultados/20261016T224331Z/resultado.json).

Commit medido: `73895be48e03a35731c53d77a45e471808313168`.

Commit que conserva los resultados sin interpretación posterior: `8bc6059`.

## Validez de la ejecución

- árbol Git limpio antes de medir;
- protocolo e instrumento confirmados antes de crear resultados;
- semilla verificada: 1.000 conversaciones y 289.000 mensajes;
- una conversación extrema con 100.000 mensajes;
- equipo conectado a corriente al inicio y al final;
- Docker 29.7.2, Compose 5.4.0, PostgreSQL 16.14 y k6 0.54.0;
- 20 corridas completas y cantidad exacta de solicitudes en todas;
- 83 archivos primarios cubiertos por SHA-256, sin discrepancias.

No se identificó una condición de invalidación.

## Resultado por escalón

Las cifras son medianas de las corridas válidas 2, 3 y 4. La corrida 1 de cada
escalón fue calentamiento, como establecía el prerregistro.

| VU | Solicitudes/corrida | p95 mediano | Throughput mediano | HTTP no 200 | Páginas inválidas | Degradación sostenida |
| ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 10 | 40 | 8,963 ms | 177,431 req/s | 0 | 0 | No |
| 50 | 200 | 18,768 ms | 813,531 req/s | 0 | 0 | No |
| 100 | 400 | 39,258 ms | 1.395,339 req/s | 0 | 0 | No |
| 150 | 600 | 41,060 ms | 1.879,413 req/s | 0 | 0 | No |
| 200 | 800 | 58,235 ms | 2.303,017 req/s | 0 | 0 | No |

## Qué se degradó primero

La primera señal que creció fue la **latencia**, no los errores ni el contrato.
Entre 10 y 200 VU, el p95 mediano pasó de 8,963 ms a 58,235 ms: un aumento de
aproximadamente 6,5 veces. Aun así, 58,235 ms representa cerca del 11,6 % del
umbral de 500 ms.

El throughput no se aplanó: pasó de 177,431 a 2.303,017 req/s. Las 6.120
solicitudes de las quince corridas válidas respondieron HTTP 200, devolvieron
exactamente 50 mensajes y no produjeron 401, 403, 404, 429 ni 5xx.

Por tanto:

- **H1 queda respaldada** dentro de la frontera probada;
- no apareció saturación sostenida hasta 200 VU;
- el resultado no demuestra capacidad por encima de 200 VU;
- tampoco representa Internet, múltiples nodos ni tráfico sostenido durante
  horas: son ráfagas locales sobre una conversación caliente.

## Sistema frente a instrumento

No hay evidencia de que API, PostgreSQL o k6 alcanzaran su límite. El throughput
siguió creciendo y las métricas funcionales permanecieron correctas.

La telemetría Docker solo obtuvo dos muestras por corrida porque las ráfagas
terminaron rápidamente. Los valores de k6 en `0,0 %` no prueban ausencia de uso;
reflejan una resolución insuficiente para capturar un contenedor tan corto. Por
esa razón la clasificación final es:

> **Sin límite observado hasta 200 VU; atribución de recursos no concluyente por
> la duración de las ráfagas.**

No se afirmará que PostgreSQL, la API o el generador fueron el cuello de botella.

## Táctica seleccionada para el antes/después

El profesor exige aplicar una táctica y medir antes/después. Como no hubo
timeouts, errores transitorios ni dependencia caída, se rechaza aplicar `retry`,
`timeout` o `circuit breaker`: no responden a una señal observada y un retry
podría multiplicar carga durante una saturación futura.

Se selecciona experimentalmente una **caché local de un segundo para la página
más reciente de mensajes**, con estas restricciones:

- deshabilitada por defecto y activada solo para la medición posterior;
- se consulta únicamente después de verificar que el usuario pertenece al chat;
- clave formada por chat, límite y offset;
- invalidación inmediata al enviar un mensaje;
- capacidad limitada;
- estadísticas de hits, misses, inserciones e invalidaciones;
- comparación en 200 VU, porque no existió un escalón degradado anterior.

La táctica responde a la única tendencia observada —aumento de latencia ante
lecturas repetidas— y al polling móvil ya documentado. No se adopta como decisión
de producción: el experimento concentra todas las solicitudes en una sola
conversación, por lo que puede favorecer artificialmente una caché de clave
caliente. La decisión se tomará después del resultado antes/después.
