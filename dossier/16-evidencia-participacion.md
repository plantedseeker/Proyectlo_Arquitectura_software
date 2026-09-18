# Evidencia de participación

Este documento reúne comandos de Git para revisar de forma verificable la participación de cada integrante del equipo a partir del historial del repositorio.

> Ejecutar los comandos desde la raíz del repositorio. Los ejemplos usan nombres y correos ficticios; deben reemplazarse por los datos reales del integrante que se quiera revisar.

## 1. Ver autores y volumen general de participación

```bash
git shortlog -sne --all
```

Muestra cantidad de commits por autor y correo.

Ejemplo:

```text
18  Ana Pérez <ana@correo.com>
12  Luis Gómez <luis@correo.com>
9   Marta Ruiz <marta@correo.com>
```

Sirve para detectar rápidamente autores, alias o correos duplicados.

## 2. Ver todo lo que hizo un estudiante

Por nombre:

```bash
git log --author="Ana Pérez" --oneline --decorate
```

O por correo:

```bash
git log --author="ana@correo.com" --oneline
```

Esto permite revisar la secuencia completa de aportes de un integrante.

Para tener más contexto:

```bash
git log --author="Ana Pérez" \
  --pretty=format:"%h | %ad | %an | %s" \
  --date=short
```

Salida de ejemplo:

```text
a14e92f | 2026-08-20 | Ana Pérez | agrega línea base de búsqueda
b87d410 | 2026-08-18 | Ana Pérez | documenta escenario de calidad
...
```

## 3. Ver contribuciones por un rango de fechas

```bash
git log \
  --author="Ana Pérez" \
  --since="2026-08-01" \
  --until="2026-08-31" \
  --pretty=format:"%h | %ad | %s" \
  --date=short
```

Este filtro permite verificar aportes dentro de un corte, semana o periodo concreto.

## 4. Ver qué archivos modificó realmente

```bash
git log --author="Ana Pérez" --name-status
```

Da una salida similar a:

```text
commit a14e92f
A   experimentos/linea-base.md
M   k6/busqueda.js
M   docs/escenarios-calidad.md
```

Las letras más comunes son:

```text
A = Added
M = Modified
D = Deleted
R = Renamed
```

Esto permite relacionar los commits con archivos y artefactos concretos del proyecto.

## 5. Ver participación semana a semana

```bash
git log \
  --author="Ana Pérez" \
  --date=short \
  --pretty=format:"%ad | %h | %s"
```

Después puede agruparse conceptualmente por semana o entregable, por ejemplo:

```text
S3 — escenario de calidad
S4 — línea base
S5 — C4
S7 — ADR
S8 — ajustes de comité
```

La agrupación por semana es una interpretación del equipo; la evidencia primaria sigue siendo el historial de Git.

## 6. Detectar commits tardíos o masivos

Para todos los autores:

```bash
git log \
  --all \
  --date=iso \
  --pretty=format:"%ad | %h | %an | %s"
```

Ordenados cronológicamente desde el más antiguo:

```bash
git log \
  --all \
  --reverse \
  --date=iso \
  --pretty=format:"%ad | %h | %an | %s"
```

Esto ayuda a identificar concentraciones de commits cerca de una fecha de entrega y a revisar la secuencia temporal real de los cambios.

## Uso recomendado durante la revisión

1. Ejecutar `git shortlog -sne --all` para identificar nombres, alias y correos.
2. Elegir un integrante y filtrar su historial por nombre o correo.
3. Revisar fechas, mensajes de commit y archivos modificados.
4. Contrastar los commits con PR, artefactos del `dossier/`, experimentos y código relacionados.
5. No inferir participación únicamente por cantidad de commits: un commit pequeño y uno amplio pueden representar esfuerzos muy distintos.

## Límite de la evidencia

Estos comandos muestran actividad registrada en Git, pero no prueban por sí solos todo el trabajo realizado fuera del repositorio, como reuniones, preparación oral, revisión de compañeros o trabajo no committeado. También puede haber una misma persona registrada con varios nombres o correos; por eso primero debe revisarse `git shortlog -sne --all` antes de sacar conclusiones.
