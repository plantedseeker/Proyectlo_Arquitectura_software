# Matriz de evidencia — semanas 5 a 8

Fecha de corte documental: 2026-09-11, America/Bogota.

Estados usados:

- **Cumple:** existe evidencia versionable y verificable.
- **Preparado:** el artefacto o guion existe, pero falta el acto humano o la
  ejecución indicada.
- **Pendiente:** todavía no existe evidencia suficiente.

## Semanas 5 y 6 — C4 y localización

| Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- |
| C4 contexto as-is | Cumple | [`05-c4-contexto.md`](05-c4-contexto.md), proveniente del paquete aprobado |
| C4 contenedores as-is | Cumple | [`06-c4-contenedores.md`](06-c4-contenedores.md) |
| C4 componentes del contenedor crítico | Cumple | [`07-c4-componentes-backend.md`](07-c4-componentes-backend.md); se complementa con Android en `08-c4-componentes-android.md` |
| Trazado C4 → código real | Cumple | Tabla de [`09-c4-trazabilidad-localizacion.md`](09-c4-trazabilidad-localizacion.md) |
| Audiencia y propósito por vista | Cumple | Sección específica en `05`–`09` |
| Correcciones/eliminaciones frente al código | Cumple | Registro de correcciones en `09` |
| Fenómeno medido ubicado en frontera C4 | Cumple | k6 → API → servicio/JDBC → PostgreSQL en `07` y `09` |
| Reparto del tiempo/costo | Cumple con alcance declarado | S4 mide HTTP; S7 localiza autorización y consulta SQL sin fingir reparto de seguridad/serialización |
| Revisión de diagrama de otro equipo | Pendiente humano | Usar `13-revision-par-c4.md`; registrar equipo, fecha y observaciones reales |
| Declaración de uso de IA | Cumple documental | `11-declaracion-uso-ia.md`; cada integrante debe confirmar su participación |
| Línea base reproducible en vivo | Cumple | `scripts/run-messaging-baseline.ps1` y evidencia S4 versionada |
| Defensa individual 5–7 minutos | Preparado | `12-defensa-individual-c4.md`; la exposición real no puede probarse desde Git |

## Semana 7 — causa y alternativas

| Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- |
| Instrumento de localización profundo | Cumple; repetir tras commit | Resultado real versionable; la captura final debe referir al commit que contiene el instrumento |
| Método y condiciones | Cumple documental | README y script del experimento S7 |
| Separar dato de interpretación/supuesto | Cumple documental | Secciones separadas en la evidencia S7 y comparación |
| Comparar estilos/alternativas reales | Cumple documental | `docs/architecture/alternativas-s7.md` |
| **Comparar alternativas contra drivers priorizados** | **Cumple documental** | `08-decision-estilo-arquitectonico.md` y `docs/architecture/alternativas-s7.md` trazan explícitamente los cinco drivers de `02-stakeholders-drivers.md` |
| Mínimo dos alternativas y costos | Cumple documental | Comparación y ADR-001/002/003 |
| Complejidad, seguridad, operación y reversibilidad | Cumple documental | Matrices de alternativas y ADR |
| **Mapa modular objetivo** | **Cumple documental** | `docs/architecture/modulos-y-limites.md` separa “Base As-Is verificada” de “Mapa modular objetivo S7–S8” |
| **Decisión de estilo consolidada** | **Cumple documental** | **`08-decision-estilo-arquitectonico.md` en el dossier** |
| Empezar ADR 1 y ADR 2 | Cumple documental | ADR-001 estilo y ADR-002 límites/dependencias; paginación se conserva separada como ADR-003 |
| Criticar propuesta generada por IA | Cumple documental | `docs/architecture/propuesta-ia-critica.md` |
| Registrar aceptado/modificado/rechazado de IA | Cumple documental | Tabla de decisiones en la crítica |
| PR con decisión de estilo | Cumple | [PR #11](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/11), CI en verde y fusionado a `main` |

## Semana 8 — ADR, comité y restricción ejecutable

| Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- |
| Comparación de estilos terminada | Cumple documental | `docs/architecture/alternativas-s7.md` |
| Diseño modular y límites | Cumple documental | `docs/architecture/modulos-y-limites.md` |
| ADR 1 — decisión de estilo | Cumple documental | `docs/adr/ADR-001-limites-modulo-mensajeria.md` |
| ADR 2 — límites y dependencias permitidas | Cumple documental | `docs/adr/ADR-002-limites-modulos-dependencias.md` |
| ADR 3 — paginación | Cumple documental | `docs/adr/ADR-003-paginacion-historial-mensajes.md`; conserva la decisión técnica antes numerada ADR-002 |
| Implicaciones de seguridad | Cumple documental | ADR-001/002 y `08-decision-estilo-arquitectonico.md` |
| Crítica de propuesta IA | Cumple documental | `docs/architecture/propuesta-ia-critica.md` |
| Preparación de mini-comité | Preparado | `docs/architecture/mini-comite-s8.md` |
| Soporte máximo 5 diapositivas | Cumple documental | `docs/architecture/mini-comite-s8-5-diapositivas.md` |
| Veredicto posterior al comité | Pendiente humano | Registrar solo después del comité: confirmada, ajustada o reconsiderada |
| Primera restricción ejecutable | Cumple técnico local | `scripts/check_architecture.py` prohíbe JDBC/SQL en controladores |
| Restricción automática en CI | Cumple configuración | `.github/workflows/ci.yml` ejecuta `python scripts/check_architecture.py` en PR y push a `main` |
| Violación deliberada y CI rojo | Pendiente de GitHub | Crear PR temporal, capturar URL de la corrida roja y retirar la violación |
| Restricción referencia su ADR | Cumple documental/técnico | ADR-002 y documentos relacionados enlazan la regla ejecutable |

## Lo que no debe presentarse como terminado

Hasta que ocurra realmente, no se marcarán como cumplidos la revisión por otro
equipo, la defensa oral, el veredicto del mini-comité ni la corrida roja de CI.
Los formatos sirven para producir evidencia, no para sustituirla.
