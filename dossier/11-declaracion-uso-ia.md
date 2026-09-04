# Declaración de uso de inteligencia artificial — módulo C4/S7/S8

Fecha: 2026-09-04.

## Herramienta y propósito

Se utilizó Codex como asistente para:

- contrastar el C4 aprobado con los archivos reales del repositorio;
- proponer trazabilidad y localizar la frontera del experimento de mensajería;
- preparar un instrumento reproducible de `EXPLAIN (ANALYZE, BUFFERS)`;
- redactar alternativas, borradores de ADR y una prueba de límites;
- detectar afirmaciones que requerían medición o participación humana.

## Contexto entregado a la IA

El equipo proporcionó el repositorio, el paquete C4 aprobado, la categoría
confirmada “Mensajería y mesa de ayuda”, los checklists S5–S8 y la observación
del auditor. La IA consultó el código y la evidencia versionada; no recibió datos
personales de usuarios reales para construir la semilla.

## Resultados aceptados, modificados y rechazados

| Resultado de IA | Tratamiento del equipo/documento |
| --- | --- |
| Trazar k6 → API → servicio/JDBC → PostgreSQL | Aceptado porque coincide con código y protocolo |
| Aclarar que JDBC/Flyway no son contenedores independientes | Aceptado tras contraste con despliegue |
| Usar EXPLAIN para comparar página reciente, profunda y cursor | Aceptado y ejecutado como MSG-LOC-01; la captura final registra el commit que contiene el instrumento |
| Extraer un microservicio de mensajería | Rechazado por ahora: no hay presión medida suficiente |
| Adoptar cursor inmediatamente | Modificado: migración compatible solo con evidencia y necesidad real |
| Registrar revisión par/comité como cumplidos | Rechazado: requieren personas y hechos verificables |

El análisis completo está en
[`docs/architecture/propuesta-ia-critica.md`](../docs/architecture/propuesta-ia-critica.md).

## Verificación humana requerida

Antes de fusionar, cada integrante debe:

1. abrir los archivos que le corresponden y contrastar enlaces/código;
2. ejecutar o revisar las pruebas relacionadas;
3. corregir cualquier decisión que no pueda defender;
4. aprobar personalmente su commit y PR desde su propia cuenta;
5. explicar en la defensa qué parte aceptó, modificó o rechazó.

## Límites

La IA no confirma la aprobación del profesor, no sustituye la revisión de otro
equipo, no participa como miembro del mini-comité y no puede atribuir autoría a
una persona que no revisó su contribución. Tampoco convierte una interpretación
en medición: los resultados S7 y CI se registran solo después de ejecutarlos.

## Confirmación de integrantes

- Camilo Andrés Romero Palencia — revisión/fecha: `PENDIENTE`
- Santiago Jaramillo Sánchez — revisión/fecha: `PENDIENTE`
- Juan Carlos Barragán Arévalo — revisión/fecha: `PENDIENTE`
