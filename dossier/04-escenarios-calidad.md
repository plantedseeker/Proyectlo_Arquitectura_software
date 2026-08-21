# 04 — Escenario principal y línea base

Estado: **prerregistro sellado y contrastado con medición real**.

Categoría: **Mensajería y mesa de ayuda**, confirmada por el profesor.

Commit de sellado: `e7e84d98b866cc0455fff8e45fb6bf2bdd00b983`.

## Hipótesis previa MSG-PERF-01

Con 1.000 conversaciones sintéticas distribuidas de forma extrema —900 con 10
mensajes, 90 con 1.000, 9 con 10.000 y una con 100.000—, consultar los últimos
50 mensajes de la conversación extrema mediante 40 solicitudes y 10 usuarios
virtuales mantendrá `p95 <= 500 ms` en cada corrida válida, sin respuestas
inválidas.

H0: al menos una corrida válida tendrá `p95 > 500 ms` o una comprobación fallará.

Se ejecutan cuatro corridas con k6; la primera es calentamiento y se descarta.
Se registra además la mediana de los p95 de las corridas 2, 3 y 4.

## Método reproducible

Herramienta: `grafana/k6:0.54.0`, ejecutada mediante Docker Compose.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

El script construye API, aplica Flyway, carga la semilla, registra máquina,
energía, commit y topología, ejecuta cuatro corridas y agrega el resultado en:

```text
experimentos/medicion-escenario-01/resultados/resultado.json
```

## Condiciones que invalidan la medición

La corrida no se acepta como línea base si ocurre cualquiera de estas condiciones:

1. el commit registrado no contiene este prerregistro, paginación e índice;
2. la semilla no informa exactamente 1.000 conversaciones y 289.000 mensajes;
3. la conversación extrema no contiene exactamente 100.000 mensajes;
4. k6 no ejecuta 40 solicitudes, una comprobación falla o hay respuesta no 2xx;
5. cambia la conexión a corriente/batería durante las cuatro corridas;
6. Docker reinicia API o PostgreSQL, o la salud deja de estar `UP`;
7. generador, API y PostgreSQL dejan de compartir el equipo declarado;
8. se modifica concurrencia, tamaño de página o umbral sin nuevo prerregistro;
9. se usa información real o personal en lugar de la semilla sintética.

## Contraste con el dato real

La medición se ejecutó el 2026-08-20, hora de Colombia
(`2026-08-21T03:32:21Z`), después del commit de sellado. La primera corrida se
descartó como calentamiento, tal como se había definido antes de medir.

| Corrida | Uso | Solicitudes | p95 | Comprobaciones | Fallos HTTP |
| --- | --- | ---: | ---: | ---: | ---: |
| 1 | Calentamiento descartado | 40 | 83,553 ms | 100 % | 0 % |
| 2 | Válida | 40 | 9,451 ms | 100 % | 0 % |
| 3 | Válida | 40 | 8,265 ms | 100 % | 0 % |
| 4 | Válida | 40 | 9,109 ms | 100 % | 0 % |

La mediana de los p95 válidos fue **9,109 ms**. Las tres corridas válidas
quedaron por debajo de 500 ms, devolvieron exactamente 50 mensajes por solicitud
y no tuvieron respuestas fallidas. Por tanto, el dato **respalda H1** bajo las
condiciones declaradas; no demuestra el mismo comportamiento en Internet, con
varios nodos o con una carga diferente.

Condiciones auditadas:

- equipo Acer Predator PH16-71, Intel Core i9-13900HX, 95,7 GiB de RAM y 32 CPU
  lógicas;
- conectado a corriente antes y después, batería al 80 % y plan Equilibrado;
- k6, API Spring Boot y PostgreSQL 16.14 compartieron Docker Desktop y el mismo
  equipo físico;
- `grafana/k6:0.54.0`, 10 VU, 40 solicitudes por corrida y páginas de 50;
- revisión medida `c7c0edd66f49ed28fb694cf9c1970bab03c4b640`;
- conteo real posterior: 1.000 conversaciones, 289.000 mensajes y distribución
  900/90/9/1, incluida una conversación de 100.000 mensajes.

La evidencia primaria está en `../experimentos/medicion-escenario-01/resultados/`:
`contexto.json`, `run-1.json` a `run-4.json`, `resultado.json` y
`verificacion-semilla.json`. No se observó ninguna condición de invalidación.

## Presiones que obligan a redecidir

- conversaciones proyectadas por encima de 100.000 mensajes;
- SLA menor a 250 ms;
- despliegue por Internet o varios nodos de API;
- necesidad de páginas profundas, que favorece cursor en lugar de `offset`;
- adopción de entrega en tiempo real mediante WebSocket o notificaciones push;
- incumplimiento de una corrida válida o exposición entre participantes.
