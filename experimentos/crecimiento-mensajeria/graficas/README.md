# Gráficas reproducibles — S11

Estas curvas se generan exclusivamente desde los JSON versionados del
experimento. No sustituyen los datos primarios ni agregan mediciones.

## Evidencia visual

### Crecimiento por escalones

![p95 y throughput por escalón](01-crecimiento-p95-throughput.svg)

Fuente:
[`resultado.json`](../resultados/20261016T224331Z/resultado.json).

### Táctica antes/después

![comparación de caché](02-cache-antes-despues.svg)

Fuente:
[`resultado-comparacion.json`](../comparacion-cache/20261016T230407Z/resultado-comparacion.json).

## Reproducción

Desde la raíz del repositorio:

```powershell
python .\experimentos\crecimiento-mensajeria\generar_graficas.py
```

El script usa únicamente la biblioteca estándar de Python y vuelve a escribir
los dos SVG con los valores presentes en los JSON. La primera gráfica muestra
mediana y rango mínimo–máximo de las tres corridas válidas por escalón. La
segunda muestra las medianas comparadas a 200 VU.

El umbral p95 de 500 ms se declara fuera de la escala del primer gráfico para
no aplastar visualmente los valores medidos, cuyo máximo mediano fue
58,235 ms.
