# Veredicto — Spike 1 de integración Mensajería → Ofertas

- Fecha de ejecución: 2026-10-16
- Commit medido: `1e8bc8767efa3a601522046930332c714b6eeb65`
- Commit de resultados: `b2d59f1`
- Rama: `spike/s10-integracion-mensajeria-ofertas`
- Resultado estructurado: `resultados/20261016T211031Z/resultado.json`

## Veredicto explícito

- **Validez del experimento:** válido.
- **Veredicto de la hipótesis:** **RESPALDADA** bajo las condiciones registradas.
- **Veredicto de alcance:** **AJUSTADA**.

`AJUSTADA` no indica un fallo posterior a observar métricas. Antes de compilar y
medir se redujo el payload original de cuatro campos a `jobId` y `companyId`,
porque esos son los datos mínimos que Mensajería necesita para crear la
conversación. El cambio, su momento y su justificación quedaron registrados en
`01-enmienda-previa-a-medicion.md` antes de la ejecución.

## Resultado cuantitativo

| Corrida | Uso | Solicitudes medidas | p95 | Checks | Fallos HTTP inesperados | Umbrales |
| ---: | --- | ---: | ---: | ---: | ---: | --- |
| 1 | calentamiento descartado | 40 | 5,779 ms | 100 % | 0 % | cumple |
| 2 | válida | 40 | 4,781 ms | 100 % | 0 % | cumple |
| 3 | válida | 40 | 4,360 ms | 100 % | 0 % | cumple |
| 4 | válida | 40 | 4,710 ms | 100 % | 0 % | cumple |

En las tres corridas válidas:

- p95 mínimo: **4,360 ms**;
- mediana de p95: **4,710 ms**;
- p95 máximo: **4,781 ms**;
- umbral preregistrado: **500 ms**.

La mediana solo resume. La aceptación se evaluó corrida por corrida y ninguna
corrida válida excedió el umbral.

## Comprobación de criterios

| Criterio preregistrado | Resultado |
| --- | --- |
| Tres corridas válidas con p95 <= 500 ms | Cumple: 4,781; 4,360; 4,710 ms |
| Todos los checks funcionales pasan | Cumple: tasa 1,0 en cada corrida |
| Fallos HTTP inesperados = 0 | Cumple: tasa 0,0 en cada corrida |
| 40 solicitudes medidas por corrida válida | Cumple |
| Token ausente → 401 | Cumple en las cuatro corridas |
| Token inválido → 401 | Cumple en las cuatro corridas |
| Oferta inexistente → 404 | Cumple en las cuatro corridas |
| Contrato exacto `jobId`, `companyId` | Cumple en las cuatro corridas |
| Sin cambio de esquema | Cumple |
| Dentro de 180 minutos | Cumple: ejecución automatizada de 7,925 s |

## Interpretación

La evidencia respalda que un contrato REST síncrono y mínimo puede resolver
esta consulta concreta dentro del monolito modular actual sin introducir un
broker, una proyección replicada, CQRS completo ni Event Sourcing. El resultado
no demuestra que la alternativa asíncrona sea universalmente peor; demuestra
que su costo adicional no es necesario para esta necesidad y estas condiciones.

## Límites de la conclusión

- API, PostgreSQL y k6 compartieron la misma máquina física.
- La comunicación fue local mediante Docker Desktop; no se midieron latencia de
  red distribuida, fallos parciales entre hosts ni despliegues independientes.
- Se midieron 10 VU y 40 solicitudes por corrida, no capacidad máxima.
- El endpoint sigue siendo experimental y no reemplaza automáticamente el flujo
  productivo `createOrGetChat`.
- El token local sirve para probar el límite, no constituye por sí solo un
  diseño definitivo de gestión de secretos entre servicios.

Estas limitaciones impiden extrapolar el p95 a producción, pero no invalidan la
comparación contra el criterio preregistrado del spike.

## Trazabilidad e integridad

- Condiciones: `resultados/20261016T211031Z/contexto.json`.
- Agregado: `resultados/20261016T211031Z/resultado.json`.
- Corridas resumidas: `run-1.json` a `run-4.json`.
- Datos crudos: `run-1.ndjson` a `run-4.ndjson`.
- Logs: `run-1.log` a `run-4.log`.
- Hashes: `resultados/20261016T211031Z/SHA256SUMS.txt`.
- Capturas de fallos, recuperación y éxito: `evidencias/`.

Este veredicto es la entrada experimental para ADR-003; no reemplaza la
ratificación humana de la decisión arquitectónica.
