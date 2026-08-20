# Protocolo reproducible

El protocolo solo usa servicios locales y datos sintéticos. Requiere Docker Desktop con Compose y Python 3.10 o posterior.

## 1. Sellar la hipótesis

Desde la raíz:

```bash
git add docs/experiment/01-preregistration.md
git commit -m "docs: prerregistrar experimento PostgreSQL"
git rev-parse HEAD
```

Copiar el hash en el prerregistro antes de medir. Si el curso admite un tag firmado o una entrega previa, conservar también esa evidencia.

## 2. Arrancar la solución local

```bash
docker compose up --build -d
docker compose ps
```

Esperar a que `db` y `api` aparezcan saludables. Comprobar:

```bash
curl http://localhost:8080/actuator/health
```

## 3. Verificar el instrumento

```bash
python -m unittest discover -s experiments/postgresql/tests -v
```

Estas pruebas verifican percentiles, resumen y entradas inválidas; no miden el sistema.

## 4. Cargar la semilla

Linux/macOS/Git Bash:

```bash
docker compose exec -T db psql -U utrabajo -d utrabajo < experiments/postgresql/seed.sql
```

PowerShell:

```powershell
Get-Content .\experiments\postgresql\seed.sql -Raw |
  docker compose exec -T db psql -U utrabajo -d utrabajo
```

La consulta final debe informar 10.000 sintéticas, 9.000 activas y el tamaño promedio de descripción.

## 5. Ejecutar cuatro corridas

```bash
python experiments/postgresql/run_experiment.py \
  --runs 4 --requests 40 --concurrency 10 \
  --output docs/experiment/results/baseline.json
```

El instrumento valida primero autenticación, tamaño de respuesta y reloj. Después marca la corrida 1 como descartada y compara automáticamente las corridas 2–4 con `p95 <= 500 ms`.

## 6. Revisar evidencia

Confirmar que el JSON contiene:

- fecha UTC, plataforma, Python, CPU lógica y commit;
- volumen, distribución, página, concurrencia y solicitudes;
- cuatro corridas con la primera marcada `discarded: true`;
- latencias, rendimiento y decisión automática.

Los JSON se ignoran por defecto. Agregar el resultado aprobado de forma explícita:

```bash
git add -f docs/experiment/results/baseline.json
```

No modificar el JSON manualmente ni reemplazar líneas base anteriores.
