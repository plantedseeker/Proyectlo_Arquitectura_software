# Evidencia de participación — historial real de Git

Este documento registra evidencia obtenida del historial **real** del repositorio UTrabajo. No es una plantilla: los nombres, correos, commits, fechas y archivos que aparecen abajo fueron extraídos de Git.

> Corte de evidencia: 2026-09-18, durante el PR #15. Para obtener el historial completo se ejecutó `git fetch --all --tags --prune --unshallow` y después los comandos de esta guía. Como el comando usa `--all`, la captura bruta incluye el commit temporal de diagnóstico del PR #15 (`45d7615`) y el merge sintético creado por GitHub para probar ese PR (`a695b0e`). Esos dos commits se excluyen cuando se presenta el resumen normalizado previo al diagnóstico.

## 1. Autores y volumen general detectado

Comando ejecutado:

```bash
git shortlog -sne --all
```

Salida real de la captura:

```text
48  PlantedSeeker55 <39866398+plantedseeker@users.noreply.github.com>
17  plantedseeker <camilo.andres1232@gmail.com>
 2  Santiago Jaramillo Sánchez <239506892+Sanw0lk@users.noreply.github.com>
 2  juan147157 <juanchoarv@gmail.com>
 1  Juan Carlos Barragán Arévalo <211776185+juan147157@users.noreply.github.com>
 1  Sanw0lk <santiagojaramillo020@gmail.com>
```

La salida muestra **seis identidades Git**, pero corresponden a **tres cuentas/personas con actividad verificable**. Hay alias porque una misma persona hizo commits desde Git local y también desde GitHub/web.

| Persona/cuenta normalizada | Identidades observadas | Commits de la captura bruta | Ajuste por diagnóstico temporal | Volumen estable previo al diagnóstico |
| --- | --- | ---: | ---: | ---: |
| `plantedseeker` | `PlantedSeeker55` + `plantedseeker` | 65 | -2 (`45d7615`, `a695b0e`) | **63** |
| Santiago Jaramillo Sánchez (`Sanw0lk`) | `Santiago Jaramillo Sánchez` + `Sanw0lk` | **3** | 0 | **3** |
| Juan Carlos Barragán Arévalo (`juan147157`) | `Juan Carlos Barragán Arévalo` + `juan147157` | **3** | 0 | **3** |

**Importante:** este conteo mide commits, no esfuerzo. Un commit puede modificar un archivo y otro puede introducir un conjunto amplio de código, documentación o evidencia.

## 2. Evidencia por integrante

### 2.1 `plantedseeker`

Identidades observadas:

```text
PlantedSeeker55 <39866398+plantedseeker@users.noreply.github.com>
plantedseeker <camilo.andres1232@gmail.com>
```

Rango visible del historial: **2026-08-20 a 2026-09-18** en el corte analizado.

Ejemplos de commits reales:

```text
03c61d0 | 2026-08-20 | feat: iniciar UTrabajo con PostgreSQL 16
b9ba8af | 2026-08-20 | docs: registrar sello y automatizar linea base
e7e84d9 | 2026-08-20 | feat: preparar dossier y experimento de mensajeria
0bbd9fd | 2026-09-04 | docs(c4): integrar vistas aprobadas y trazabilidad S5-S6
a635f2c | 2026-09-11 | feat(walking-skeleton): agregar recorrido vertical y diagnostico
b85db3c | 2026-09-11 | docs(s7): consolidar decision de estilo arquitectonico
15619c4 | 2026-09-18 | docs: agregar guía integral de exposición S5-S8
```

Archivos/áreas verificadas en esos aportes incluyen, entre otros:

- cliente Android y backend inicial (`app/`, `backend/`);
- Docker, PostgreSQL y migraciones;
- experimento y línea base de mensajería S4;
- `dossier/01-*` a documentación posterior del curso;
- C4 de contexto, contenedores y componentes;
- walking skeleton y evidencia de recuperación;
- ADR, mapa modular, checklist y guía de exposición.

Ejemplo concreto: `e7e84d9` modificó `ChatController.kt`, `UTrabajoService.kt`, agregó el índice `V2__message_recent_index.sql`, el experimento k6, el seed de mensajería, scripts y los primeros artefactos del dossier.

### 2.2 Santiago Jaramillo Sánchez (`Sanw0lk`)

Identidades observadas:

```text
Santiago Jaramillo Sánchez <239506892+Sanw0lk@users.noreply.github.com>
Sanw0lk <santiagojaramillo020@gmail.com>
```

Actividad visible en el corte: **2026-09-04**.

Commits reales:

```text
78d8b38 | 2026-09-04 | docs(s7): localizar costo y comparar alternativas de mensajeria
8eaed87 | 2026-09-04 | evidence(s7): registrar localizacion reproducible
d2eb8ce | 2026-09-04 | Merge pull request #6 from plantedseeker/evidence/s7-localization
```

Aportes verificables:

- `docs/adr/ADR-001-limites-modulo-mensajeria.md`;
- ADR de paginación existente en ese momento;
- `docs/architecture/alternativas-s7.md`;
- `docs/architecture/propuesta-ia-critica.md`;
- `dossier/11-declaracion-uso-ia.md`;
- `experimentos/localizacion-s7/`;
- `scripts/run-s7-localization.ps1`;
- resultados reproducibles de localización S7.

El commit `8eaed87` actualizó además los resultados (`localizacion.json`, `SHA256SUMS.txt`) y la documentación asociada al experimento.

### 2.3 Juan Carlos Barragán Arévalo (`juan147157`)

Identidades observadas:

```text
Juan Carlos Barragán Arévalo <211776185+juan147157@users.noreply.github.com>
juan147157 <juanchoarv@gmail.com>
```

Actividad visible en el corte: **2026-09-11** y **2026-09-18**.

Commits reales:

```text
1f13008 | 2026-09-11 | architecture(s8): proteger limites del backend en CI
3e68814 | 2026-09-11 | Merge pull request #10 from plantedseeker/architecture/s8-adr-ci
eed4002 | 2026-09-18 | Merge pull request #13 from plantedseeker/docs/guia-exposicion-integral
```

Aporte técnico verificable del commit `1f13008`:

- modificó `.github/workflows/ci.yml`;
- modificó `README.md` y ADR-001;
- agregó `docs/architecture/evidencia-restriccion-ci.md`;
- agregó `docs/architecture/mini-comite-s8.md`;
- agregó `docs/architecture/modulos-y-limites.md`;
- agregó `dossier/10-checklist-semanas-5-8.md`;
- modificó `dossier/GUIA-EJECUCION.md` y `dossier/README.md`;
- agregó `scripts/check_architecture.py`.

Los otros dos registros son **commits de merge**. Por eso no deben contarse como si cada uno representara por sí solo un bloque adicional de implementación.

## 3. Participación por periodos/semanas observada

El comando usado para ordenar el historial fue:

```bash
git log \
  --all \
  --reverse \
  --date=iso \
  --pretty=format:"%ad | %h | %an <%ae> | %s"
```

Resumen del historial:

| Fecha / periodo | Evidencia predominante observada |
| --- | --- |
| 20–21 ago. 2026 | Inicialización del sistema, PostgreSQL, línea base, experimento de mensajería y dossier S1–S4 (`plantedseeker`) |
| 4 sep. 2026 | C4 S5–S6 (`plantedseeker`) y localización/alternativas S7 (Santiago / `Sanw0lk`) |
| 11 sep. 2026 | Walking skeleton y alineación documental S7–S8 (`plantedseeker`); restricción arquitectónica en CI (Juan / `juan147157`) |
| 18 sep. 2026 | Guía integral y evidencias de exposición; Juan registra el merge del PR #13 |

Esta tabla describe **cuándo aparece actividad en Git**. No demuestra por sí sola cuánto tiempo trabajó cada persona fuera del repositorio.

## 4. Archivos realmente modificados

Comando ejecutado para contrastar commits con archivos:

```bash
git log --all --date=short \
  --pretty=format:"@@ %ad | %h | %an <%ae> | %s" \
  --name-status
```

Las letras observadas se interpretan así:

```text
A = Added
M = Modified
D = Deleted
R = Renamed
```

Ejemplos representativos obtenidos del historial:

```text
78d8b38 — Santiago Jaramillo Sánchez
A docs/adr/ADR-001-limites-modulo-mensajeria.md
A docs/architecture/alternativas-s7.md
A docs/architecture/propuesta-ia-critica.md
A experimentos/localizacion-s7/README.md
A experimentos/localizacion-s7/ejecutar_explain.py
A scripts/run-s7-localization.ps1

1f13008 — Juan Carlos Barragán Arévalo
M .github/workflows/ci.yml
A docs/architecture/evidencia-restriccion-ci.md
A docs/architecture/mini-comite-s8.md
A docs/architecture/modulos-y-limites.md
A dossier/10-checklist-semanas-5-8.md
A scripts/check_architecture.py

0bbd9fd — plantedseeker
A dossier/05-c4-contexto.md
A dossier/06-c4-contenedores.md
A dossier/07-c4-componentes-backend.md
A dossier/08-c4-componentes-android.md
A dossier/09-c4-trazabilidad-localizacion.md
A dossier/12-defensa-individual-c4.md
A dossier/13-revision-par-c4.md
A dossier/14-plan-pr-semanas-5-8.md
```

## 5. Comandos para reproducir o profundizar la revisión

### Ver autores y volumen

```bash
git shortlog -sne --all
```

### Ver todo lo que hizo un autor

```bash
git log --author="Santiago Jaramillo Sánchez" --oneline --decorate
```

O por correo:

```bash
git log --author="239506892+Sanw0lk@users.noreply.github.com" --oneline
```

### Ver fecha, hash, autor y mensaje

```bash
git log --author="Juan Carlos Barragán Arévalo" \
  --pretty=format:"%h | %ad | %an | %s" \
  --date=short
```

### Filtrar por rango de fechas

```bash
git log \
  --author="plantedseeker" \
  --since="2026-08-01" \
  --until="2026-08-31" \
  --pretty=format:"%h | %ad | %s" \
  --date=short
```

### Ver archivos modificados por un autor

```bash
git log --author="Santiago Jaramillo Sánchez" --name-status
```

### Ver la cronología completa

```bash
git log \
  --all \
  --reverse \
  --date=iso \
  --pretty=format:"%ad | %h | %an | %s"
```

## 6. Lectura correcta de esta evidencia

- En el corte analizado aparecen tres personas/cuentas con actividad Git verificable: `plantedseeker`, Santiago (`Sanw0lk`) y Juan (`juan147157`).
- Los alias/correos duplicados deben agruparse antes de comparar cantidades.
- Los commits de merge se distinguen de los commits que modifican directamente archivos.
- El número de commits **no es una medida suficiente de esfuerzo, calidad ni porcentaje de participación**.
- Esta evidencia no cubre reuniones, preparación oral, decisiones tomadas fuera de Git, revisión entre compañeros ni trabajo no committeado.
- Los conteos cambian cuando se agregan nuevos commits; por eso este documento conserva la fecha y el corte del snapshot utilizado.
