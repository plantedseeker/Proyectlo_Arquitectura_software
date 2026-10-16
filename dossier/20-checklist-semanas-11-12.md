# Checklist verificable — Semanas 11 y 12

## Semana 11

| Requisito del profesor | Estado | Evidencia |
| --- | --- | --- |
| Reejecutar M2 con carga aumentada | Cumple | cinco escalones y 20 corridas en `experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/` |
| Registrar dónde y cómo se degrada | Cumple con resultado negativo válido | la latencia crece primero, sin degradación sostenida; `02-analisis-crecimiento.md` |
| Matriz de fallos | Cumple | `03-matriz-fallos.md` |
| Matriz de resiliencia | Cumple | `04-matriz-resiliencia.md` |
| Aplicar táctica y medir antes/después | Cumple | caché local, cuatro corridas posteriores y `06-resultado-tactica-cache.md` |
| Resultados bajo `/experimentos` | Cumple | JSON, logs, contexto, capturas y SHA-256 |

## Semana 12

| Requisito del profesor | Estado | Evidencia |
| --- | --- | --- |
| Estimar costo de alternativas en órdenes de magnitud | Cumple como estimación explícita | `docs/architecture/alternativas-distribucion-s12.md` |
| Redactar ADR 4 con datos | Propuesta técnica completa; ratificación pendiente | `docs/adr/ADR-004-distribucion-mensajeria.md` |
| Fitness function: regla, umbral y fallo de CI | Cumple e implementada antes de S14 | `scripts/check_distribution_decision.py`, tests y workflow |
| Evaluar estrategia de microservicios de IA | Cumple | `docs/architecture/auditoria-ia-microservicios-s12.md` |
| Preparar defensa 2: método, datos y conclusión | Cumple en preparación; exposición pendiente | `dossier/19-resumen-ejecutivo-s11-s12.md` |
| Mini-comité 2 y veredicto real | Pendiente de actividad humana | `docs/architecture/mini-comite-s12.md` |

## Evidencia y rúbrica

| Evidencia de rúbrica | Cobertura |
| --- | --- |
| Resultado de crecimiento o táctica antes/después | se conservan ambos, con condiciones y huellas |
| Matriz de fallos y resiliencia | presentes y actualizadas después de medir |
| Distribución con costo explícito | cuatro alternativas y rangos `C0`/órdenes `10^0–10^1` |
| ADR 4 con veredicto de mini-comité | ADR listo; veredicto no se inventa y sigue pendiente |
| Fitness function definida | regla y umbral documentados; ejecución integrada en CI |
| Defensa de 7–10 minutos con re-decisión | guion cronometrado y procedimiento de restricción nueva |

## Pendientes que requieren personas

- [ ] cada integrante revisa el PR desde su cuenta;
- [ ] mini-comité 2 registra participantes y veredicto;
- [ ] ADR-004 se actualiza con ese veredicto;
- [ ] se registra cualquier restricción sorpresa del profesor;
- [ ] se realiza la defensa individual.

No marcar estas casillas con datos simulados.
