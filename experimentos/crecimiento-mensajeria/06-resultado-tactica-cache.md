# Resultado antes/después — caché de la página reciente

Evidencia primaria: [`comparacion-cache/20261016T230407Z/resultado-comparacion.json`](comparacion-cache/20261016T230407Z/resultado-comparacion.json).

Commit medido después de definir la táctica y el protocolo:
`0849dea148e90f91ff0a07e225d76d844a5c5242`.

Resultado base: [`resultados/20261016T224331Z/resultado.json`](resultados/20261016T224331Z/resultado.json), medido sobre
`73895be48e03a35731c53d77a45e471808313168`.

## Validez de la comparación

- el árbol estaba limpio y el código de la táctica ya estaba confirmado;
- se reconstruyó la API con la caché habilitada solo para el experimento;
- se restauró y verificó la misma semilla de 1.000 conversaciones y 289.000
  mensajes;
- se conservaron 200 VU, 800 solicitudes por corrida, endpoint, cuenta, página,
  máquina, energía y versión de k6;
- se hicieron cuatro corridas y se descartó la primera como calentamiento;
- las cuatro corridas conservaron HTTP 200, cero fallos, 800 solicitudes y
  páginas válidas;
- los 17 archivos primarios están cubiertos por `SHA256SUMS.txt` y las huellas
  fueron verificadas sin discrepancias.

No se identificó una condición de invalidación de la medición.

## Resultado cuantitativo

| Métrica mediana, corridas 2–4 | Antes | Después | Cambio | Criterio |
| --- | ---: | ---: | ---: | --- |
| p95 | 58,235 ms | 55,377 ms | -4,908 % | Cumple |
| Throughput | 2.303,017 req/s | 2.260,038 req/s | -1,866 % | No cumple |
| Fallos HTTP | 0 | 0 | sin cambio | Cumple |
| Páginas válidas | 100 % | 100 % | sin cambio | Cumple |

Las corridas válidas posteriores fueron:

| Corrida | p95 | Throughput |
| ---: | ---: | ---: |
| 2 | 72,403 ms | 2.021,714 req/s |
| 3 | 55,377 ms | 2.260,038 req/s |
| 4 | 49,975 ms | 2.444,452 req/s |

La caché empezó en cero y terminó con 3.196 hits, 4 misses y 4 inserciones: un
hit rate de 99,875 % para las 3.200 lecturas. Esto demuestra que la táctica fue
ejercitada. La prueba de integración separada demuestra la invalidación al
enviar un mensaje; esta carga fue deliberadamente de solo lectura y, por ello,
registró cero invalidaciones durante las corridas.

## Veredicto

El agregador emite **`partially_supported`**. La corrección se conservó, hubo
hits y el p95 mediano disminuyó, pero el throughput mediano también disminuyó y
por tanto no se cumplió la regla de éxito completa congelada antes de medir.

Además, los valores posteriores caen dentro de los rangos ya observados sin
caché: el p95 base válido estaba entre 47,014 y 74,306 ms, y el throughput entre
2.254,076 y 2.417,286 req/s. Con tres corridas válidas por condición no se puede
atribuir como mejora material una diferencia pequeña que se solapa con esa
variabilidad.

## Decisión resultante

- no adoptar la caché como comportamiento de producción con esta evidencia;
- mantenerla deshabilitada por defecto;
- no introducir retry, timeout, circuit breaker ni un servicio de caché sin un
  fallo que los justifique;
- si se reconsidera, medir tráfico sostenido, múltiples chats y varios nodos,
  incluyendo escrituras e invalidaciones;
- usar este resultado en S12 como evidencia contra añadir distribución o
  complejidad operativa prematuramente.

La conclusión se limita a una conversación caliente, un nodo de API y ráfagas
locales. No demuestra el comportamiento de una caché distribuida ni de una
carga con alta cardinalidad de chats.
