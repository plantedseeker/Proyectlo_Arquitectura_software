# Capturas de ejecución — S11

Estas capturas complementan la defensa visual del experimento. No sustituyen
los JSON, logs, contexto ni `SHA256SUMS.txt`, que son la evidencia primaria y
reproducible.

| Captura | Qué permite comprobar |
| --- | --- |
| `01-crecimiento-cierre-y-huellas.png` | cierre del crecimiento, registro del resultado y cálculo de huellas |
| `02-crecimiento-construccion-semilla-inicio.png` | construcción de la imagen, contenedores saludables, semilla y comienzo de escalones |
| `03-cache-construccion-y-semilla.png` | compilación exitosa, reconstrucción de la API y restauración de la misma semilla para el después |
| `04-cache-cierre-y-huellas.png` | corrida final, límites de interpretación, huellas y ruta del resultado antes/después |
| `05-grafana-cloud-limite-100-vu.png` | rechazo verificable de Grafana Cloud al intentar 150 VU y límite del proyecto de 100 VU |
| `06-grafana-cloud-cierre-y-huellas.png` | cierre exitoso de la reejecución complementaria, consolidación y ruta del nuevo `resultado.json` |

## Integridad de las capturas recibidas

| Archivo | SHA-256 |
| --- | --- |
| `01-crecimiento-cierre-y-huellas.png` | `9da5fe39b846fc6ce9bc47b2a17d2d3f81f4d1292964da39956ad188bd592c86` |
| `02-crecimiento-construccion-semilla-inicio.png` | `b87694a53e846c0ceb6ccb1b685df8635f6e4dda55647441a8ff04f0190572b8` |
| `03-cache-construccion-y-semilla.png` | `bb34199c260431e07dd7b770e76b4ec531eb4d6451196693b2cd0b795e3dff20` |
| `04-cache-cierre-y-huellas.png` | `dac8d6a89990f35d149d2b874661b588661c477024ca8286badac6b65118cf64` |
| `05-grafana-cloud-limite-100-vu.png` | `081ab0bb94d5d281dcba6947c909b7d45825824739ebd1ab4e8ed0168ab319e4` |
| `06-grafana-cloud-cierre-y-huellas.png` | `92681468d8efa1f7c276d68d09bfaca0bf2c9ac0e2254afbc685a9f0877c332a` |

## Secuencia visual

### Crecimiento: inicio

![Construcción, semilla e inicio del crecimiento](02-crecimiento-construccion-semilla-inicio.png)

### Crecimiento: cierre

![Cierre y huellas del crecimiento](01-crecimiento-cierre-y-huellas.png)

### Táctica: construcción y condiciones

![Construcción y semilla de la comparación](03-cache-construccion-y-semilla.png)

### Táctica: cierre

![Cierre y huellas de la comparación](04-cache-cierre-y-huellas.png)

### Grafana Cloud: límite de cuota comprobado

![Grafana Cloud rechaza 150 VU porque el proyecto admite 100](05-grafana-cloud-limite-100-vu.png)

### Grafana Cloud: cierre de la reejecución complementaria

![Cierre, consolidación y huellas de la reejecución complementaria](06-grafana-cloud-cierre-y-huellas.png)
