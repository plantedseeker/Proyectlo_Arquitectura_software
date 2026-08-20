# Resultados de línea base

No hay una línea base publicada todavía: primero debe existir el commit que selle `01-preregistration.md` y luego debe ejecutarse PostgreSQL 16 local mediante Docker.

El instrumento genera `baseline.json`; Git lo ignora para evitar publicar corridas accidentales. Después de verificar una corrida válida:

```bash
git add -f docs/experiment/results/baseline.json
```

Nunca sobrescribir una línea base. Si cambian código, semilla o condiciones, usar otro nombre y conservar el resultado anterior.
