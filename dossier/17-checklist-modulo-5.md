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
| Abrir PR con 09-api-events-integration.md | **Cumple** | dossier/09-api-events-integration.md; PR #16 fusionado |

## Semana 10

| Requisito | Estado | Evidencia |
| --- | --- | --- |
| Spike 1 en rama separada | **Cumple** | `spike/s10-integracion-mensajeria-ofertas` |
| Alcance acotado en tiempo | **Cumple** | 00-preregistro.md + contexto `within_180_minute_timebox: true` |
| Registrar ejecución/condiciones | **Cumple** | contexto.json; commit medido `1e8bc87` |
| Registrar resultados | **Cumple** | resultados/20261016T211031Z/; commit `b2d59f1` |
| Emitir veredicto explícito | **Cumple** | veredicto.md; commit `4072eba` |
| ADR-003 referenciando spike | **Cumple documental; pendiente ratificación** | docs/adr/ADR-003-integracion-contextos.md |
| Evaluar aplicabilidad real de CQRS/consistencia eventual | **Cumple** | cqrs-event-sourcing.md + ADR-003 |
| Mantener trazabilidad Git | **Cumple** | preregistro < código < resultados < veredicto < ADR |

## Control de numeración ADR

La secuencia oficial del Módulo 5 reserva **ADR-003** para la decisión de integración respaldada por Spike 1.

La decisión previa de paginación se conserva sin pérdida de contenido como:

- ADR-006 — Estrategia de paginación del historial de mensajes.

## Criterios de cierre

M5 queda documental y técnicamente preparado para revisión porque puede comprobarse:

1. cada frontera del Context Map tiene responsabilidad y relación explicable;
2. la flecha Mensajería → Ofertas tiene contrato concreto y comparación sync/async;
3. fecha(preregistro) < fecha(código spike) < fecha(resultados) < fecha(veredicto);
4. ADR-003 cita Context Map, alternativas, commit medido y veredicto;
5. la conclusión sobre CQRS/Event Sourcing se expresa como utilidad vs costo, no como moda.

La ratificación del ADR y la revisión del PR siguen siendo actos humanos; no se
declaran automáticamente por completar los artefactos.
