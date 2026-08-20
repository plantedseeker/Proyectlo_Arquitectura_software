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
  --deployment-topology same-machine \
  --output docs/experiment/results/baseline-s4-audit.json
```

El instrumento valida primero autenticación, tamaño de respuesta y reloj. Después marca la corrida 1 como descartada y compara automáticamente las corridas 2–4 con `p95 <= 500 ms`.

`same-machine` declara expresamente la condición local usada: el generador Python
se ejecuta en Windows y la API y PostgreSQL 16 se ejecutan en contenedores de
Docker Desktop sobre ese mismo equipo físico. Si los componentes se separan,
se debe usar `--deployment-topology distributed` y tratarlo como otro escenario.

Durante la misma ejecución el instrumento registra fabricante, modelo, CPU, RAM,
sistema operativo, CPU lógicas, conexión a corriente o batería y plan de energía.
Si el sistema no permite detectar la energía, se debe declarar la observada con
`--energy-condition plugged_in` o `--energy-condition battery`; no se acepta una
condición desconocida.

## 6. Revisar evidencia

Confirmar que el JSON contiene:

- fecha UTC, plataforma, Python, CPU lógica y commit;
- firma de máquina con fabricante, modelo, procesador y memoria física;
- condición de energía y plan de energía capturados durante la medición;
- declaración de si generador, API y PostgreSQL comparten equipo físico;
- volumen, distribución, página, concurrencia y solicitudes;
- cuatro corridas con la primera marcada `discarded: true`;
- latencias, rendimiento, mediana del p95 de las corridas válidas y decisión automática.

Los JSON se ignoran por defecto. Agregar el resultado aprobado de forma explícita:

```bash
git add -f docs/experiment/results/baseline-s4-audit.json
```

No modificar el JSON manualmente ni reemplazar líneas base anteriores.

## 7. Comprobar correspondencia con la categoría

Este instrumento mide únicamente el trayecto HTTP API + PostgreSQL que consume
el cliente Android. No mide red móvil, decodificación en Retrofit ni renderizado
en Compose. Por ello, sus resultados respaldan solo el componente servidor.

Antes de declarar la validez completa, se debe anexar la confirmación de categoría
del docente. Si la categoría confirmada es **aplicación móvil**, se requiere además
un instrumento Android de extremo a extremo (dispositivo o emulador identificado)
que mida desde la acción de abrir la lista hasta que las ofertas sean visibles.
