# Experimento S11 — crecimiento de mensajería

Este directorio extiende de manera trazable la medición M2
`MSG-PERF-01`. El protocolo y el instrumento deben estar confirmados en Git con
el árbol limpio antes de producir resultados.

## Archivos previos a la medición

- `00-preregistro.md`: hipótesis, escalones, métricas e invalidaciones.
- `01-riesgos-preregistrados.md`: fallos candidatos sin resultados.
- `carga-crecimiento.js`: instrumento k6 parametrizado por escalón.
- `resumir_resultados.py`: captura de contexto y agregación reproducible.
- `../../scripts/run-messaging-growth.ps1`: orquestador oficial.

## Ejecución oficial

Con Docker Desktop abierto, desde la raíz:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-growth.ps1
```

El runner rechaza un árbol Git sucio, vuelve a construir la aplicación, carga y
verifica la semilla, ejecuta los cinco escalones y crea un directorio fechado
bajo `resultados/`. Una salida 99 de k6 por incumplimiento de umbral se conserva
como resultado experimental y no detiene los escalones restantes.

### Panel complementario en Grafana Cloud

Para repetir los mismos cinco escalones localmente y transmitir sus métricas a
Grafana Cloud, ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-growth.ps1 -GrafanaCloud
```

El runner solicita el Personal API token de forma oculta, usa el Stack ID
`1889948` y no escribe la credencial en archivos ni en Git. El modo cloud está
desactivado por defecto porque consume la cuota de la cuenta. Fija
`grafana/k6:2.2.0` para disponer del protocolo actual de selección de stack;
la ejecución oficial histórica permanece identificada con
`grafana/k6:0.54.0` y no se reinterpreta retroactivamente.

Al terminar se generan `grafana-cloud-runs.json` junto con los nuevos
resultados locales y un [índice estable de paneles](grafana-cloud/README.md).
Los paneles son evidencia visual complementaria: los JSON, logs, contexto y
huellas SHA-256 locales siguen siendo la evidencia primaria reproducible.

No se debe editar manualmente un resultado primario. Cualquier exclusión o
repetición se documenta en el análisis con su causa.

## Lectura del crecimiento observado

- `02-analisis-crecimiento.md`: conclusión y límites de atribución.
- `03-matriz-fallos.md`: fallos observados, descartados y pendientes.
- `04-matriz-resiliencia.md`: tácticas justificadas por la evidencia.
- `graficas/README.md`: curvas reproducibles de p95/throughput y comparación
  antes/después generadas desde los JSON versionados.
- `resultados/20261016T224331Z/`: 20 corridas primarias, contexto y huellas.

La relación entre el prerregistro original y el formato más estricto solicitado
posteriormente por el material de M6 está registrada en
[`trazabilidad-prerregistro-s11.md`](../../docs/architecture/trazabilidad-prerregistro-s11.md).
La diferencia se declara; no se reescribe el pasado experimental.

## Comparación de la táctica

`05-protocolo-tactica-cache.md` congela antes de medir una caché local de la
página reciente. La funcionalidad está deshabilitada por defecto y el runner la
activa solo durante el experimento. Con el protocolo y el código confirmados y
el árbol limpio, ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-cache-comparison.ps1
```

El runner reconstruye la API, restaura la misma semilla, reinicia los contadores
de caché y ejecuta cuatro corridas de 200 VU. `comparar_tactica.py` contrasta las
corridas 2–4 con el escalón base ya registrado, emite un veredicto y genera
huellas SHA-256 bajo `comparacion-cache/<fecha>/`.

## Resultado de la táctica

- `06-resultado-tactica-cache.md`: interpretación, veredicto y decisión.
- `comparacion-cache/20261016T230407Z/`: cuatro corridas, contexto, estadísticas
  de caché, comparación estructurada y huellas.
- `evidencias-ejecucion/`: capturas visuales del inicio y cierre de ambas
  mediciones, con descripción e integridad.

El veredicto fue `partially_supported`: el p95 mediano bajó 4,908 %, pero el
throughput mediano bajó 1,866 %. La caché permanece deshabilitada por defecto y
no se recomienda adoptarla con la evidencia actual.

Para regenerar las visualizaciones sin cambiar los datos:

```powershell
python .\experimentos\crecimiento-mensajeria\generar_graficas.py
```
