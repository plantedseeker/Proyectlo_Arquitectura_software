# Dossier de arquitectura — UTrabajo

Este directorio usa los nombres exigidos por los checklists de las semanas 1–8.
La categoría confirmada por el profesor es **Mensajería y mesa de ayuda**.

| Entrega | Artefacto |
| --- | --- |
| Contexto y sistema base | `01-contexto-sistema.md` |
| Stakeholders, restricciones, drivers y riesgos | `02-stakeholders-drivers.md` |
| Atributos, escenarios y mapa atributo–decisión | `03-atributos-calidad.md` |
| Prerregistro, método, invalidación y contraste | `04-escenarios-calidad.md` |
| Experimento reproducible | `../experimentos/medicion-escenario-01/` |
| Verificación literal del checklist | `05-checklist-semanas-1-4.md` |
| Guion de exposición para tres personas | `06-guion-exposicion-3-personas.md` |
| Guía única de ejecución | `GUIA-EJECUCION.md` |
| Matriz detallada de trazabilidad C4 | `MATRIZ-TRAZABILIDAD-C4.md` |
| C4 C1 contexto as-is aprobado | `05-c4-contexto.md` |
| C4 C2 contenedores as-is aprobado | `06-c4-contenedores.md` |
| C4 C3 backend y Android | `07-c4-componentes-backend.md`, `08-c4-componentes-android.md` |
| **Decisión de estilo arquitectónico S7** | **`08-decision-estilo-arquitectonico.md`** |
| Trazabilidad C4, correcciones y frontera medida | `09-c4-trazabilidad-localizacion.md` |
| Matriz de evidencia semanas 5–8 | `10-checklist-semanas-5-8.md` |
| Declaración y crítica de uso de IA | `11-declaracion-uso-ia.md` |
| Defensa individual C4 | `12-defensa-individual-c4.md` |
| Formato de revisión par | `13-revision-par-c4.md` |
| Plan de contribuciones y PR S5–S8 | `14-plan-pr-semanas-5-8.md` |
| Walking skeleton de mensajería | `../docs/architecture/walking-skeleton.md` |
| Modos de fallo y recuperación | `../docs/architecture/failure-modes-walking-skeleton.md` |

Las vistas C4 `05` a `08-c4-componentes-android.md` provienen del paquete aprobado
por el profesor. La procedencia y las huellas SHA-256 se registran en el
documento `09`.

El artefacto **`08-decision-estilo-arquitectonico.md`** pertenece a S7 y se
mantiene en el dossier porque es una evidencia entregable del curso. No debe
confundirse con `08-c4-componentes-android.md`, que corresponde al paquete C4.

La evidencia técnica de apoyo para S7–S8 está en
`../experimentos/localizacion-s7/`, `../docs/adr/` y `../docs/architecture/`,
pero los artefactos documentales entregables se referencian desde este dossier.

## Contribución individual y pull requests

Los commits históricos hechos directamente sobre `main` no se presentan como
pull requests retroactivos. A partir de este dossier se usa una rama por cambio
y revisión mediante PR.

| Integrante | Rama | PR fusionado | Estado |
| --- | --- | --- | --- |
| `plantedseeker` | `dossier/mensajeria-s1-s4` | [PR #1](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/1) | Fusionado el 2026-08-20 |
| Santiago Jaramillo Sánchez (`Sanw0lk`) | `evidence/s7-localization` | [PR #6](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/6) | Fusionado el 2026-09-04 |
| Juan Carlos Barragán Arévalo (`juan147157`) | `architecture/s8-adr-ci` | [PR #10](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/10) | Fusionado el 2026-09-11 |
| `plantedseeker` | `docs/s7-s8-alineacion` | [PR #11](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/11) | Fusionado el 2026-09-11 |

El PR #1 contiene el contexto, los drivers, los escenarios, el instrumento y la
línea base. Los PR posteriores registran contribuciones reales de S7–S8. Cada
dueño de cuenta debe revisar y autorizar personalmente su contribución. No se
fabrican autores, revisiones ni veredictos.
