# Resultados de línea base

> Evidencia histórica de la exploración de ofertas. La categoría confirmada
> posteriormente es **Mensajería y mesa de ayuda**; el resultado principal se
> genera en `experimentos/medicion-escenario-01/` y no reemplaza estos archivos.

Línea base capturada el 2026-08-20 después del prerregistro y almacenada en
`baseline.json`. El archivo fue generado por el instrumento; no fue editado
manualmente.

## Condiciones principales

- Revisión medida: `62c48d59d8011d788120e442e8dab1968357c037`.
- PostgreSQL 16 y API ejecutados localmente con Docker Desktop.
- 10.000 ofertas sintéticas, 90 % activas y páginas de 100 elementos.
- 40 solicitudes por corrida y concurrencia 10.
- Cuatro corridas; la primera fue calentamiento y se descartó.
- Umbral prerregistrado: `p95 <= 500 ms` en las corridas válidas.

## Resultado

| Corrida | Uso | p50 (ms) | p95 (ms) | Máximo (ms) | Solicitudes/s |
| --- | --- | ---: | ---: | ---: | ---: |
| 1 | Descartada | 21.785 | 31.652 | 32.457 | 358.499 |
| 2 | Válida | 20.380 | 24.763 | 25.824 | 434.309 |
| 3 | Válida | 18.367 | 22.430 | 24.664 | 460.820 |
| 4 | Válida | 20.278 | 41.364 | 46.842 | 382.883 |

Las tres corridas válidas quedaron por debajo de 500 ms y no produjeron
respuestas inválidas. Por la regla definida antes de medir, la decisión del
instrumento es **`supported`**: H1 queda apoyada para este entorno local.

La mediana de los p95 de las tres corridas válidas (`22.430`, `24.763` y
`41.364` ms) es **`24.763 ms`**.

## Aclaración de auditoría sobre esta línea base

- En la ejecución real, el generador de carga Python, la API y PostgreSQL 16
  compartieron el mismo equipo físico: Python corrió en Windows y los otros dos
  componentes en Docker Desktop sobre ese host.
- El equipo se identificó después como **Acer Predator PH16-71**, Intel Core
  i9-13900HX, 95.7 GiB de RAM, 32 procesadores lógicos y Windows 11
  `10.0.26200`. El JSON original solo guardó sistema operativo y CPU lógicas.
- La condición de energía no fue registrada durante la corrida original y no se
  reconstruye retroactivamente. Por eso `baseline.json` conserva valor histórico,
  pero todavía no cierra por sí solo el requisito de condiciones de S4.

## Línea base auditable versión 2

`baseline-s4-audit.json` fue capturada el 2026-08-20 sobre la revisión
`d247f932140944ac9358a3dbbca28a7e89b6753a`, sin sobrescribir la línea base
histórica. Su SHA-256 es
`80AF88268EDC0E161A03465B7122A5339CE10B07D41045103090D6F40C126A78`.
La corrida registró automáticamente:

- Acer Predator PH16-71, Intel Core i9-13900HX, 95.7 GiB de RAM, 32
  procesadores lógicos y Windows 11 `10.0.26200`;
- alimentación conectada, batería al 80 % y plan de energía `Equilibrado`;
- generador Python, API y PostgreSQL 16 en el mismo equipo físico; los dos
  servicios se ejecutaron en contenedores de Docker Desktop;
- cuatro corridas, con la primera descartada, y mediana automática de los p95.

| Corrida | Uso | p95 (ms) |
| --- | --- | ---: |
| 1 | Descartada | 26.444 |
| 2 | Válida | 19.557 |
| 3 | Válida | 16.732 |
| 4 | Válida | 19.457 |

La mediana de p95 de las corridas válidas es **`19.457 ms`**. Las tres cumplen
`p95 <= 500 ms`, por lo que la decisión del instrumento sigue siendo
**`supported`** para el trayecto HTTP API + PostgreSQL en este entorno local.

El JSON también declara expresamente que el cliente Android no formó parte de
la medición; la correspondencia completa con la categoría continúa abierta.

Este resultado no demuestra el comportamiento por Internet ni la latencia de
renderizado del cliente Android; esas condiciones permanecen como amenazas a
la validez y como presión futura para reabrir la decisión arquitectónica.

No sobrescribir estas líneas base. Si cambian código, semilla o condiciones, se
debe generar otro archivo y conservar `baseline.json` y `baseline-s4-audit.json`.
