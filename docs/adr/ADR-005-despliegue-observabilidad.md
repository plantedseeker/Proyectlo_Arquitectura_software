# ADR-005 — Despliegue administrado mínimo y observabilidad operativa

- Fecha: 2026-10-23
- Estado: **Propuesta técnica; pendiente de revisión humana**
- Categoría: despliegue, operación y observabilidad
- Protege: seguridad, disponibilidad, operabilidad, rendimiento y reversibilidad

## Contexto

UTrabajo sigue siendo un monolito modular con PostgreSQL. S11 no encontró degradación sostenida hasta 200 VU y S12 propuso no extraer Mensajería. S13 identifica como prioridades antes de Internet: TLS/secrets, documentos sensibles privados y logging/alerting.

El sistema local actual no es un diseño de producción: usa HTTP, defaults demo y almacenamiento local. Se requiere definir un primer despliegue conceptual sin inventar una necesidad de microservicios.

## Alternativas

### A — monolito administrado mínimo

- una instancia API;
- reverse proxy/TLS;
- PostgreSQL administrado;
- object storage privado;
- secrets externos;
- observabilidad centralizada.

Costo relativo: **1 D0**.

### B — monolito redundante

- balanceador;
- dos o más instancias API;
- PostgreSQL administrado;
- object storage compartido;
- observabilidad multi-instancia.

Costo relativo: **2–3 D0**.

## Decisión propuesta

Adoptar **A como primer despliegue conceptual**.

No implementar todavía B, microservicios, broker ni Kubernetes. Mantener B como siguiente evolución reversible si:

- existe SLO de disponibilidad que una sola API no pueda cumplir;
- la API se convierte en límite atribuible;
- una medición comparable supera p95 500 ms por capacidad de proceso;
- la recuperación manual de una instancia no cumple el RTO acordado.

## Observabilidad mínima obligatoria

### Métricas

- request rate;
- 5xx rate;
- p95/p99;
- JVM heap/GC/CPU;
- Hikari active/max/pending/timeouts;
- DB health/latency;
- 401/403/login failures;
- storage usage/errors.

### Logs

Estructurados y correlacionables con requestId/traceId. Prohibido registrar tokens, contraseñas, contenido de chat o documentos.

### Trazas

HTTP → seguridad → controller → service/auth → JDBC → PostgreSQL; spans de almacenamiento cuando aplique.

### Alertas

- health DOWN >2 min;
- 5xx >5 % durante 5 min;
- p95 >500 ms durante 5 min;
- DB pool >90 % o pending sostenido;
- storage >85 %;
- anomalías de autenticación/autorización;
- backup fuera de política.

Los umbrales de seguridad/capacidad sin baseline se consideran iniciales y deben recalibrarse.

## Riesgos operativos

La matriz completa está en `docs/architecture/matriz-riesgos-operativos-s14.md`. Los riesgos principales son DB/backup, configuración/secrets, almacenamiento, ausencia de alertas y una sola instancia de API.

## Fitness function

ADR-004 sigue protegido por `scripts/check_distribution_decision.py` con umbral **cero violaciones**.

S14 conserva como evidencia una ejecución CI roja deliberada al activar por defecto la caché experimental en una rama de prueba. La violación se corrige antes de fusionar y el mismo PR debe volver a verde.

Esta prueba demuestra que la restricción no es solo documental.

## Consecuencias positivas

- despliegue coherente con la evidencia de S11/S12;
- cierra las prioridades de seguridad necesarias antes de Internet;
- introduce observabilidad antes de aumentar distribución;
- conserva una ruta simple hacia réplicas;
- no agrega coordinación de microservicios sin necesidad.

## Consecuencias negativas

- una sola API sigue siendo punto único de fallo;
- observabilidad y servicios administrados aumentan costo frente al laboratorio local;
- object storage y secret manager agregan dependencias operativas;
- el plan no demuestra alta disponibilidad hasta ejecutar un despliegue real.

## Reversibilidad

A → B requiere agregar load balancer/réplica y externalizar cualquier estado local. No cambia el contrato Android ni exige separar bounded contexts.

## Evidencia

- `dossier/12-despliegue-observabilidad.md`
- `dossier/10-seguridad.md`
- `docs/architecture/matriz-riesgos-operativos-s14.md`
- `docs/architecture/alternativas-distribucion-s12.md`
- `experimentos/crecimiento-mensajeria/`
- `docs/architecture/failure-modes-walking-skeleton.md`
- `docs/architecture/evidencia-fitness-ci-s14.md`

## Confirmación requerida

Esta decisión puede ser ratificada, ajustada o reconsiderada por el equipo/profesor. No se registra un despliegue real que todavía no existe.
