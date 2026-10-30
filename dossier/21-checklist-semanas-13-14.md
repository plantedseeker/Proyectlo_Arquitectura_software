# Checklist verificable — Semanas 13 y 14

## Semana 13 — seguridad

| Requisito del profesor | Estado | Evidencia |
| --- | --- | --- |
| Modelar amenazas sobre el sistema base con OWASP Top Ten | **Cumple documental** | `dossier/10-seguridad.md`, OWASP Top 10:2025 |
| Elevar checklist de seguridad al sistema completo | **Cumple** | sección 8 de `10-seguridad.md` |
| Diseñar controles arquitectónicos y límites de confianza | **Cumple** | controles C-S13-01..06 + [diagrama SVG](../docs/security/trust-boundaries-s13.svg) ([PNG](../docs/security/trust-boundaries-s13.png), [fuente](../docs/security/trust-boundaries-s13.puml)) |
| Priorizar amenazas aplicables | **Cumple** | matriz T01–T12 con P/I/riesgo |
| Descartar amenazas genéricas de IA | **Cumple** | `docs/security/auditoria-amenazas-ia-s13.md` |
| Abrir PR con `10-seguridad.md` | **Cumple** | PR S13 dedicado |
| No afirmar seguridad de producción | **Cumple** | alcance y riesgo residual explícitos |

## Semana 14 — despliegue y observabilidad

| Requisito del profesor | Estado | Evidencia |
| --- | --- | --- |
| Diseñar despliegue conceptual | **Cumple documental** | [documento S14](12-despliegue-observabilidad.md) + [diagrama SVG](../docs/architecture/deployment-s14.svg) ([PNG](../docs/architecture/deployment-s14.png), [fuente](../docs/architecture/deployment-s14.puml)); explícitamente no desplegado |
| Comparar costo relativo de dos alternativas | **Cumple** | A=1 D0; B=2–3 D0 |
| Definir métricas | **Cumple** | RED, JVM, pool/DB, seguridad y dominio |
| Definir logs | **Cumple** | formato estructurado + campos prohibidos |
| Definir trazas | **Cumple** | HTTP→Security→Controller→Service→JDBC→DB |
| Definir alertas críticas | **Cumple** | health, 5xx, p95, pool, storage, auth, backup |
| Vincular observabilidad con fallos M6 | **Cumple** | tabla F03/F05/F06/F08/F10/F11/F12/F14 |
| Fitness function ejecutándose en CI | **Cumple técnico** | `check_distribution_decision.py` + tests + workflow |
| Ver la fitness function romper una violación | **Evidencia S14** | PR/run rojo deliberado y corrección verde documentados en `evidencia-fitness-ci-s14.md` |
| Redactar ADR 5 | **Cumple documental** | `docs/adr/ADR-005-despliegue-observabilidad.md` |
| Matriz de riesgos operativos | **Cumple** | `docs/architecture/matriz-riesgos-operativos-s14.md` |
| `12-despliegue-observabilidad.md` | **Cumple** | evidencia principal S14 |

## Evidencia y rúbrica

| Evidencia grande | Archivo principal |
| --- | --- |
| Matriz de seguridad y controles | `dossier/10-seguridad.md` |
| Despliegue + costo relativo | `dossier/12-despliegue-observabilidad.md` |
| Observabilidad ligada a fallos | `dossier/12-despliegue-observabilidad.md` |
| ADR 5 + riesgos operativos | `docs/adr/ADR-005-despliegue-observabilidad.md` + matriz S14 |
| Fitness function en CI | workflow + evidencia rojo/verde |

## Numeración ADR

- ADR-001 — estilo/monolito modular.
- ADR-002 — límites/dependencias.
- ADR-003 — integración entre contextos.
- ADR-004 — distribución de Mensajería.
- **ADR-005 — despliegue y observabilidad S14.**
- ADR-006 — paginación del historial, renumerada sin cambiar su contenido técnico.

## Pendientes humanos

- revisión del PR por integrantes;
- ratificación/ajuste de ADR-005 si el profesor lo solicita;
- defensa oral.

No se inventan participantes, votos ni una infraestructura desplegada que todavía no existe.
