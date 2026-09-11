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

El C3 muestra el trayecto `controlador → servicio → JDBC → PostgreSQL`. Sin una
regla automática, un cambio podría hacer que un controlador consulte JDBC
directamente y mezcle protocolo HTTP con persistencia.

## Decisión propuesta

Mantener mensajería dentro de la API Spring Boot como **monolito modular** y
preservar estas dependencias:

```text
Android/HTTP → controllers → services/auth → JdbcClient → PostgreSQL
```

Los controladores pueden depender de servicios y modelos, pero no de
`JdbcClient`, `java.sql` ni `javax.sql`. Esta regla se comprobará en S8 con una
prueba de arquitectura ejecutada por CI. La decisión no obliga todavía a dividir
`UTrabajoService`; sí protege la dirección mínima mientras evoluciona el diseño.

## Alternativas consideradas

### A. Monolito sin límites ejecutables

- **Beneficio:** ningún trabajo inicial.
- **Costo:** aumenta el riesgo de SQL en controladores y acoplamiento difícil de
  detectar en revisión manual.
- **Seguridad:** autorización y acceso a datos pueden dispersarse.
- **Operación:** un despliegue sencillo, pero mantenimiento progresivamente más
  riesgoso.
- **Decisión:** descartada.

### B. Monolito modular con límites comprobados — seleccionada

- **Beneficio:** conserva un solo despliegue y añade una barrera barata contra
  dependencias indebidas.
- **Costo:** mantener la prueba y refactorizar cualquier violación legítima a
  través de servicios.
- **Seguridad:** favorece que autenticación y autorización permanezcan en capas
  conocidas.
- **Operación:** mismo Docker Compose y PostgreSQL actuales.
- **Reversibilidad:** alta; retirar o ajustar la regla es un cambio de prueba y
  documentación, sujeto a otro ADR.

### C. Extraer mensajería a microservicio con broker

- **Beneficio potencial:** despliegue/escalado independiente y preparación para
  eventos o tiempo real.
- **Costo:** segundo servicio, contratos, autenticación distribuida,
  observabilidad, consistencia, reintentos, broker y más CI/CD.
- **Seguridad:** amplía superficie de ataque y exige autorización entre
  servicios.
- **Operación:** mayor consumo y más puntos de falla; no existe evidencia S4 que
  pague esa complejidad.
- **Reversibilidad:** media/baja después de separar datos y contratos.
- **Decisión:** diferida hasta que aparezca presión verificable.

## Consecuencias

### Positivas

- Frontera simple y coherente con C2/C3 y el despliegue local.
- Restricción visible, repetible y automática en cada PR.
- Menor costo operativo que un microservicio prematuro.
- Facilita localizar autorización y persistencia.

### Negativas

- La API sigue compartiendo proceso y despliegue con otros dominios.
- `UTrabajoService` permanece grande y requerirá división interna gradual.
- Un fallo del proceso backend afecta mensajería y demás casos de uso.
- La regla mínima no detecta todas las formas posibles de acoplamiento.

## Reversibilidad y disparadores de revisión

La decisión se revisará si mediciones en condiciones comparables demuestran
presión de escalado o disponibilidad exclusiva de mensajería, si se adopta
tiempo real con un volumen que requiera procesamiento asíncrono, o si equipos
independientes necesitan ciclos de despliegue separados.

Antes de extraer un servicio se aislarán interfaces y se migrarán datos por
etapas. Mientras tanto, la reversión consiste en modificar la prueba y este ADR
en un PR explícito; no se permite simplemente borrar la restricción para hacer
pasar CI.

## Evidencia

- [`C3 backend`](../../dossier/07-c4-componentes-backend.md)
- [`Trazabilidad del flujo`](../../dossier/09-c4-trazabilidad-localizacion.md)
- [`Resultado S4`](../../experimentos/medicion-escenario-01/resultados/resultado.json)
- Restricción ejecutable de S8: `scripts/check_architecture.py`, incorporada y
  enlazada al workflow; pendiente de conservar la URL de su primera corrida en
  GitHub.
- Evidencia de fallo deliberado de S8: pendiente de un PR de demostración real.

## Confirmación requerida

El equipo debe cambiar el estado a `Aceptada`, `Ajustada` o `Reconsiderada`
después de revisar la evidencia y registrar el mini-comité. Codex no sustituye
esa decisión humana.
