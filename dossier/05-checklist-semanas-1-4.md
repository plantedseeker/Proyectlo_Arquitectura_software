# Verificación del checklist — semanas 1 a 4

Fecha de corte: 2026-08-20. Esta matriz distingue evidencia del repositorio de
acciones que dependen de una persona o de GitHub.

| Semana | Requisito | Estado | Evidencia o pendiente |
| --- | --- | --- | --- |
| 1 | Adoptar y ejecutar sistema base | Cumple | UTrabajo Android + API + PostgreSQL 16; falta transcribir la letra A/B/C de la guía |
| 1 | Instrucciones reproducibles | Cumple | `README.md` y scripts PowerShell |
| 1 | Stakeholders y restricciones | Cumple | `02-stakeholders-drivers.md` |
| 1 | Repositorio con flujo PR | En proceso | Rama `dossier/mensajeria-s1-s4`; falta fusionar el PR |
| 1 | PR con `01-contexto-sistema.md` | En proceso | Archivo creado en la rama del dossier |
| 2 | Sistema y pruebas para checkpoint | Cumple | Docker local y workflow `UTrabajo CI` |
| 2 | Inventario inicial de riesgos | Cumple | R-01 a R-10 en `02-stakeholders-drivers.md` |
| 2 | Drivers priorizados | Cumple | Tabla de cinco drivers con justificación |
| 2 | Clasificar riesgos de IA | Cumple | Válido, genérico, irrelevante y falso con juicio del equipo |
| 2 | Un PR fusionado por integrante | Pendiente humano | No se fabrican autores; cada integrante debe fusionar un PR real |
| 3 | Estructura de escenarios | Cumple | Fuente, estímulo, artefacto, entorno, respuesta y medida |
| 3 | Matriz priorizada de atributos | Cumple | Impacto, urgencia, puntaje y justificación |
| 3 | RNF como escenarios | Cumple | MSG-PERF-01, MSG-SEC-01, MSG-REL-01 y MSG-MOD-01 |
| 3 | Escenarios IA reformulados | Cumple | Registro propuesta/problema/reformulación |
| 3 | PR con `03-atributos-calidad.md` | En proceso | Archivo creado en la rama del dossier |
| 4 | k6, JMeter o Locust | Cumple | k6 fijado como `grafana/k6:0.54.0` en Compose |
| 4 | Línea base y condiciones | Cumple | Mediana p95 9,109 ms; máquina, energía, versiones, carga y topología registradas en `/experimentos/medicion-escenario-01/resultados` |
| 4 | Método e invalidaciones | Cumple | `04-escenarios-calidad.md`, nueve causas explícitas |
| 4 | Contraste escenario/dato | Cumple | Las corridas válidas dieron 9,451; 8,265; 9,109 ms; H1 respaldada frente a 500 ms |
| 4 | PR con `04` y `/experimentos` | En proceso | Ambos están en la rama del dossier |

## Pendientes que el repositorio no puede inventar

1. Adjuntar captura o mensaje original con la categoría confirmada por el profesor.
2. Transcribir la letra A/B/C y el significado definido en la guía del curso.
3. Confirmar cuántos integrantes humanos tiene el equipo.
4. Obtener un PR fusionado y atribuible por cada integrante.
