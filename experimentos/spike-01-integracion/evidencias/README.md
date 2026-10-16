# Evidencia visual y diagnóstica — Spike 1

Estas capturas conservan el recorrido real de preparación y ejecución del
Spike 1. Incluyen fallos encontrados, correcciones verificadas y la ejecución
final. No sustituyen la evidencia estructurada: los JSON, logs, NDJSON y hashes
de `../resultados/20261016T211031Z/` son la fuente autoritativa de la medición.

| Archivo | Tipo | Qué registra |
| --- | --- | --- |
| `01-rama-base-sincronizada.png` | preparación | sincronización de `main` y commits del trabajo de Módulo 5 |
| `02-docker-no-disponible-backend-exitoso.png` | fallo + control | Docker Desktop no disponible y pruebas backend exitosas por separado |
| `03-android-sdk-no-configurado.png` | fallo | Gradle no encontraba la ubicación del Android SDK |
| `04-android-sdk-stacktrace.png` | diagnóstico | causa completa del fallo de localización del SDK |
| `05-android-build-fallido-resumen.png` | fallo | cierre de la primera ejecución Android fallida |
| `06-api-iniciando-health-transitorio.png` | diagnóstico | contenedor de API aún en estado `health: starting` durante una comprobación prematura |
| `07-android-build-fallido-final.png` | fallo | resumen `BUILD FAILED` antes de corregir lint |
| `08-android-lint-y-build-exitosos.png` | recuperación | `lintDebug`, pruebas y `assembleDebug` completados con `BUILD SUCCESSFUL` |
| `09-spike-falso-error-stderr-compose.png` | fallo del instrumento | progreso normal de Compose interpretado por Windows PowerShell como `NativeCommandError` |
| `10-spike-ejecucion-exitosa.png` | éxito | cierre `9/9`, evidencia creada, commit medido y ruta de resultados |
| `11-spike-resumen-corridas.png` | resultado | salida agregada de las corridas, controles y umbrales |
| `12-android-lint-fallo-completo.txt` | diagnóstico textual | salida completa del único error lint y sus advertencias antes de la corrección |

## Lectura correcta de los fallos

- Los fallos de Docker y Android ocurrieron durante la preparación; no forman
  parte de las corridas válidas de k6.
- El primer intento del runner se interrumpió antes de medir por el tratamiento
  de `stderr` de Windows PowerShell. Solo produjo `contexto.json` y se apartó de
  los resultados oficiales.
- La ejecución oficial partió del árbol limpio en el commit medido
  `1e8bc8767efa3a601522046930332c714b6eeb65` y produjo las cuatro corridas.

La integridad de estos archivos se registra en `SHA256SUMS.txt`.
