# ADR-001 — Mantener mensajería dentro del monolito modular

- Fecha: 2026-09-04
- Estado: **Propuesta completa; pendiente de ratificación por el equipo y mini-comité**
- Categoría: Mensajería y mesa de ayuda
- Protege: modularidad, seguridad, mantenibilidad y costo operacional

## Contexto

UTrabajo se ejecuta localmente como cliente Android, API Spring Boot y
PostgreSQL 16. La mensajería comparte autenticación, usuarios, empresas y
ofertas con el resto del backend. La línea base S4 de lectura de los últimos 50
mensajes obtuvo una mediana de p95 de 9,109 ms bajo condiciones locales; ese
dato no evidencia por sí solo necesidad de distribución.

El C3 muestra el trayecto `controlador → servicio → JDBC → PostgreSQL`. La
arquitectura actual permite mantener mensajería dentro del mismo despliegue y
proteger sus límites internos sin introducir infraestructura distribuida.

## Decisión propuesta

Mantener mensajería dentro de la API Spring Boot como **monolito modular**.
El sistema conserva un único despliegue backend y una base PostgreSQL compartida.
No se extrae por ahora un microservicio de mensajería ni se agrega broker, Redis
o una plataforma de orquestación.

Los límites concretos entre módulos y las dependencias permitidas/prohibidas se
formalizan por separado en [`ADR-002`](ADR-002-limites-modulos-dependencias.md).
La estrategia de paginación se mantiene como una decisión independiente en
[`ADR-003`](ADR-003-paginacion-historial-mensajes.md).

## Alternativas consideradas

### A. Monolito sin límites ejecutables

- **Beneficio:** ningún trabajo inicial.
- **Costo:** aumenta el riesgo de SQL en controladores y acoplamiento difícil de detectar en revisión manual.
- **Seguridad:** autorización y acceso a datos pueden dispersarse.
- **Operación:** un despliegue sencillo, pero mantenimiento progresivamente más riesgoso.
- **Decisión:** descartada.

### B. Monolito modular con límites comprobados — seleccionada

- **Beneficio:** conserva un solo despliegue y permite proteger dependencias con reglas de bajo costo.
- **Costo:** mantener la documentación y las restricciones cuando el diseño cambie legítimamente.
- **Seguridad:** favorece que autenticación y autorización permanezcan en capas conocidas.
- **Operación:** mismo Docker Compose y PostgreSQL actuales.
- **Reversibilidad:** alta; los límites pueden evolucionar mediante otro ADR sin cambiar primero el despliegue.

### C. Extraer mensajería a microservicio con broker

- **Beneficio potencial:** despliegue/escalado independiente y preparación para eventos o tiempo real.
- **Costo:** segundo servicio, contratos, autenticación distribuida, observabilidad, consistencia, reintentos, broker y más CI/CD.
- **Seguridad:** amplía superficie de ataque y exige autorización entre servicios.
- **Operación:** mayor consumo y más puntos de falla; no existe evidencia S4 que pague esa complejidad.
- **Reversibilidad:** media/baja después de separar datos y contratos.
- **Decisión:** diferida hasta que aparezca presión verificable.

## Consecuencias

### Positivas

- Frontera simple y coherente con C2/C3 y el despliegue local.
- Menor costo operativo que un microservicio prematuro.
- Facilita localizar autorización y persistencia.
- Permite reforzar límites internos sin cambiar topología.

### Negativas

- La API sigue compartiendo proceso y despliegue con otros dominios.
- `UTrabajoService` permanece grande y requerirá división interna gradual.
- Un fallo del proceso backend afecta mensajería y demás casos de uso.
- El monolito modular no ofrece escalado independiente por dominio.

## Reversibilidad y disparadores de revisión

La decisión se revisará si mediciones en condiciones comparables demuestran
presión de escalado o disponibilidad exclusiva de mensajería, si se adopta
tiempo real con un volumen que requiera procesamiento asíncrono, o si equipos
independientes necesitan ciclos de despliegue separados.

Antes de extraer un servicio se aislarán interfaces y se migrarán datos por
etapas. Mientras tanto, cualquier cambio importante de estilo debe quedar
registrado en un PR y en un nuevo ADR o actualización explícita de este.

## Evidencia

- [`Decisión de estilo S7`](../../dossier/08-decision-estilo-arquitectonico.md)
- [`Drivers priorizados`](../../dossier/02-stakeholders-drivers.md)
- [`C3 backend`](../../dossier/07-c4-componentes-backend.md)
- [`Trazabilidad del flujo`](../../dossier/09-c4-trazabilidad-localizacion.md)
- [`Resultado S4`](../../experimentos/medicion-escenario-01/resultados/resultado.json)
- [`ADR-002`](ADR-002-limites-modulos-dependencias.md) — dependencias permitidas/prohibidas y restricción CI.
- [`ADR-003`](ADR-003-paginacion-historial-mensajes.md) — estrategia de paginación.

## Confirmación requerida

El equipo debe cambiar el estado a `Aceptada`, `Ajustada` o `Reconsiderada`
después de revisar la evidencia y registrar el mini-comité. La documentación
preparada no sustituye esa decisión humana.
