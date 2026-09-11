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
| C4 componentes del contenedor crítico | Cumple | [`07-c4-componentes-backend.md`](07-c4-componentes-backend.md); se complementa con Android en `08` |
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
| Mínimo dos alternativas y costos | Cumple documental | Comparación y ADR 001/002 |
| Complejidad, seguridad, operación y reversibilidad | Cumple documental | Matrices de alternativas y ADR |
| Empezar ADR 1 y ADR 2 | Cumple documental | `docs/adr/ADR-001-*` y `ADR-002-*` |
| Criticar propuesta generada por IA | Cumple documental | `docs/architecture/propuesta-ia-critica.md` |
| Registrar aceptado/modificado/rechazado de IA | Cumple documental | Tabla de decisiones en la crítica |

## Semana 8 — ADR, comité y restricción ejecutable

| Requisito | Estado | Evidencia o acción restante |
| --- | --- | --- |
| Comparación de estilos terminada | Cumple documental | `docs/architecture/alternativas-s7.md` |
| Diseño modular y límites | Cumple documental | `docs/architecture/modulos-y-limites.md` |
| ADR 1 y ADR 2 completos | Cumple documental | Contexto, decisión, alternativas, costos, consecuencias y reversibilidad |
| Crítica de propuesta IA | Cumple documental | `docs/architecture/propuesta-ia-critica.md` |
| Preparación de mini-comité | Preparado | `docs/architecture/mini-comite-s8.md` |
| Veredicto posterior al comité | Pendiente humano | Registrar solo después del comité: confirmada, ajustada o reconsiderada |
| Primera restricción ejecutable | Cumple técnico local | `scripts/check_architecture.py` prohíbe JDBC/SQL en controladores |
| Restricción automática en CI | Preparado | El workflow ejecuta todas las pruebas de backend en push y PR; falta la primera corrida con la regla |
| Violación deliberada y CI rojo | Pendiente de GitHub | Crear PR temporal, capturar URL de la corrida roja y retirar la violación |
| Restricción referencia su ADR | Cumple documental/técnico | Prueba y evidencia enlazan ADR-001 |

## Lo que no debe presentarse como terminado

Hasta que ocurra realmente, no se marcarán como cumplidos la revisión por otro
equipo, la defensa oral, el veredicto del mini-comité ni la corrida roja de CI.
Los formatos sirven para producir evidencia, no para sustituirla.
