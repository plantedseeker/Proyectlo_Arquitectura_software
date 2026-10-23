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
entrega al contenedor solo como variable de entorno. Antes de medir consulta
los proyectos del stack, selecciona el predeterminado y pasa su Project ID a
cada corrida. El token no se guarda en el repositorio. El Stack ID versionado
es `1889948`, que no es una credencial.

El campo enmascarado puede copiar solamente los puntos visibles. Si ocurre,
Grafana rechaza el encabezado `Authorization`. Se debe usar **Regenerate API
token**, copiar inmediatamente el valor nuevo que se revela y pegarlo en el
prompt oculto. No se debe guardar el token en capturas, archivos ni mensajes.

El proyecto actual permite hasta **100 VU**. El runner mantiene intactos los
cinco escalones prerregistrados: transmite 10, 50 y 100 VU a Grafana Cloud, y
ejecuta 150 y 200 VU localmente con los mismos JSON, logs, telemetría y
huellas. Si un administrador aumenta la cuota, se puede indicar el nuevo valor
con `-GrafanaCloudMaxVus 200`.

La ejecución crea:

- 12 paneles bajo la cuota actual: tres escalones por cuatro corridas;
- 20 resultados locales: cinco escalones por cuatro corridas;
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
