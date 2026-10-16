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

No se debe editar manualmente un resultado primario. Cualquier exclusión o
repetición se documenta en el análisis con su causa.
