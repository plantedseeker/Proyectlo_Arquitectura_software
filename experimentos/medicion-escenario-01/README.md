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

## Resultado registrado

La línea base se ejecutó el 2026-08-20, hora de Colombia. La primera corrida se
descartó como calentamiento. Los p95 de las corridas válidas fueron 9,451;
8,265 y 9,109 ms; su mediana fue **9,109 ms**, por debajo del umbral
prerregistrado de 500 ms. Las 120 solicitudes válidas tuvieron 100 % de
comprobaciones y 0 % de fallos.

La medición usó la revisión
`c7c0edd66f49ed28fb694cf9c1970bab03c4b640`. El generador k6, la API y
PostgreSQL 16.14 compartieron Docker Desktop y el mismo Acer Predator PH16-71,
conectado a corriente antes y después. `resultados/verificacion-semilla.json`
documenta el conteo SQL real de 1.000 conversaciones y 289.000 mensajes.

Archivos primarios:

- `resultados/contexto.json`: máquina, energía, versión y topología;
- `resultados/run-1.json` a `run-4.json`: salida resumida de cada corrida;
- `resultados/resultado.json`: mediana, decisión y condiciones agregadas;
- `resultados/verificacion-semilla.json`: conteo directo en PostgreSQL e índice.
- `resultados/SHA256SUMS.txt`: huellas SHA-256 de la evidencia primaria.
