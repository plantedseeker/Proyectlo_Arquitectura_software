# Resultados de línea base

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

Este resultado no demuestra el comportamiento por Internet ni la latencia de
renderizado del cliente Android; esas condiciones permanecen como amenazas a
la validez y como presión futura para reabrir la decisión arquitectónica.

No sobrescribir esta línea base. Si cambian código, semilla o condiciones, se
debe generar otro archivo y conservar `baseline.json`.
