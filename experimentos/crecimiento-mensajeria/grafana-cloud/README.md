# Grafana Cloud — evidencia complementaria S11

Este índice conecta la evidencia reproducible del repositorio con los paneles
interactivos solicitados para la presentación. Los paneles se generan mediante
una **nueva ejecución local** del experimento; no convierten la medición
histórica en una medición cloud ni sustituyen sus JSON y huellas.

## Generación

Con el árbol Git limpio y Docker Desktop abierto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-growth.ps1 -GrafanaCloud
```

El comando pide el Personal API token de Grafana Cloud de forma oculta y lo
entrega al contenedor solo como variable de entorno. El token no se guarda en
el repositorio. El Stack ID versionado es `1889948`, que no es una credencial.

La ejecución crea:

- 20 paneles: cinco escalones por cuatro corridas;
- un `grafana-cloud-runs.json` dentro del directorio fechado de resultados;
- `ultima-ejecucion.md`, con enlaces directos a cada panel;
- los mismos resúmenes, logs, telemetría y huellas locales del runner normal.

## Acceso rápido

Después de ejecutar y confirmar la evidencia, abrir
[`ultima-ejecucion.md`](ultima-ejecucion.md). Los enlaces pueden exigir iniciar
sesión en la organización de Grafana; si el profesor no tiene acceso, las
gráficas SVG y los JSON versionados siguen siendo la alternativa auditable.

## Límites de interpretación

La ejecución cloud usa `grafana/k6:2.2.0` para seleccionar explícitamente el
stack actual. La medición histórica aprobada usó `grafana/k6:0.54.0`. Por eso
los paneles sirven para visualización y repetición complementaria, no para
reemplazar ni mezclar silenciosamente los valores históricos.
