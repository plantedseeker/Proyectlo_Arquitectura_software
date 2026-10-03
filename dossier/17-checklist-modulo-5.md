# Checklist verificable — Módulo 5 (Semanas 9–10)

Base: checklist de trabajo y diapositivas oficiales de Módulo 5.

## Semana 9

| Requisito | Estado | Evidencia |
| --- | --- | --- |
| Mapear subdominios y bounded contexts | **Cumple documental** | docs/dominio/subdominios.md |
| Justificar responsabilidades y exclusiones | **Cumple documental** | docs/dominio/responsabilidades-contextos.md |
| Context Map con relaciones explicadas | **Cumple documental** | docs/dominio/context-map.md y context-map.puml |
| Diseñar contrato de integración | **Cumple documental** | docs/integracion/contrato-api.yaml |
| Comparar síncrono vs asíncrono | **Cumple documental** | docs/integracion/sincrono-vs-asincrono.md |
| Filtrar eventos reales/redundantes/inventados | **Cumple documental** | docs/integracion/eventos-candidatos.md |
| Evaluar CQRS y Event Sourcing | **Cumple documental** | docs/integracion/cqrs-event-sourcing.md |
| Registrar uso crítico de IA | **Cumple documental** | docs/integracion/registro-critico-ia.md |
| Escribir hipótesis, alcance y criterios antes del código | **Cumple temporalmente** | experimentos/spike-01-integracion/00-preregistro.md; commit dedicado anterior al spike |
| Abrir PR con 09-api-events-integration.md | **Pendiente de abrir PR** | dossier/09-api-events-integration.md |

## Semana 10

| Requisito | Estado antes de ejecutar | Evidencia esperada |
| --- | --- | --- |
| Spike 1 en rama separada | Pendiente | spike/s10-integracion-mensajeria-ofertas |
| Alcance acotado en tiempo | Preregistrado | 00-preregistro.md |
| Registrar ejecución/condiciones | Pendiente | condiciones + commit medido |
| Registrar resultados | Pendiente | experimentos/spike-01-integracion/resultados/ |
| Emitir veredicto explícito | Pendiente | veredicto.md |
| ADR-003 referenciando spike | Pendiente y reservado | docs/adr/ADR-003-integracion-contextos.md |
| Evaluar aplicabilidad real de CQRS/consistencia eventual | Evaluación preliminar hecha; cierre pendiente | cqrs-event-sourcing.md + ADR-003 |
| Mantener trazabilidad Git | En curso | preregistro < código < resultados < veredicto < ADR |

## Control de numeración ADR

La secuencia oficial del Módulo 5 reserva **ADR-003** para la decisión de integración respaldada por Spike 1.

La decisión previa de paginación se conserva sin pérdida de contenido como:

- ADR-004 — Estrategia de paginación del historial de mensajes.

## Criterios de cierre

M5 no se considera cerrado hasta poder comprobar:

1. cada frontera del Context Map tiene responsabilidad y relación explicable;
2. la flecha Mensajería → Ofertas tiene contrato concreto y comparación sync/async;
3. fecha(preregistro) < fecha(código spike) < fecha(resultados) < fecha(veredicto);
4. ADR-003 cita Context Map, alternativas, commit medido y veredicto;
5. la conclusión sobre CQRS/Event Sourcing se expresa como utilidad vs costo, no como moda.
