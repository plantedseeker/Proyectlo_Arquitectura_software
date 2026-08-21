# Medición MSG-PERF-01 — Mensajería y mesa de ayuda

Este directorio contiene la evidencia reproducible exigida para semana 4.

## Contenido

- `seed-mensajeria.sql`: 1.000 conversaciones y 289.000 mensajes sintéticos.
- `carga-mensajeria.js`: carga k6 de 40 solicitudes con 10 VU.
- `resumir_resultados.py`: captura entorno, valida corridas y calcula mediana.
- `resultados/`: contexto, cuatro corridas y resultado agregado.

## Ejecución

Desde la raíz, con Docker Desktop abierto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

La operación medida es:

```http
GET /api/chats/3fb4ce25-b840-4685-d2ea-53f9a4bdedc6/messages?limit=50&offset=0
```

El usuario de carga es participante de la conversación. k6 exige HTTP 200,
exactamente 50 mensajes, tasa de fallos 0 y `p95 <= 500 ms`.

El método, hipótesis e invalidaciones están registrados antes de medir en
`../../dossier/04-escenarios-calidad.md`.
