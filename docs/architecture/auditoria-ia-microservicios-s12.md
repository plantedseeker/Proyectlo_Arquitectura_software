# Auditoría crítica de una estrategia de microservicios propuesta por IA — S12

Estado: **auditoría técnica; la IA no decide la arquitectura**.

## Solicitud planteada a la IA

> Proponga una estrategia de microservicios para UTrabajo que permita escalar
> Mensajería y reducir el acoplamiento con Ofertas. Incluya componentes,
> comunicación, datos, resiliencia y despliegue.

## Propuesta candidata generada

La propuesta candidata recomendó:

1. extraer Mensajería y Ofertas como servicios independientes;
2. asignar una base por servicio;
3. publicar eventos de ofertas mediante broker y mantener una proyección en
   Mensajería;
4. incorporar API gateway, service discovery, timeout, retry, circuit breaker,
   caché distribuida, trazas y colas de mensajes fallidos;
5. desplegar y escalar Mensajería independientemente.

La propuesta es plausible como catálogo de patrones. No constituye evidencia
de que UTrabajo los necesite.

## Contraste con datos propios

| Propuesta de IA | Dato del repositorio | Auditoría | Resultado |
| --- | --- | --- | --- |
| Extraer Mensajería para escalar | no hubo degradación sostenida hasta 200 VU; el throughput siguió creciendo | no existe límite aislado que pague la extracción | Rechazada ahora |
| Base por servicio | el escenario actual usa una transacción/base y no midió contención de propiedad de datos | duplicaría migración, respaldo y consistencia sin necesidad observada | Rechazada ahora |
| Broker y proyección de Ofertas | Spike 1 síncrono cumplió p95 y casos negativos; no se exigió continuidad ante caída | introduce consistencia eventual, duplicados y reconciliación | Diferida |
| Retry automático | el crecimiento y la comparación tuvieron 0 fallos HTTP | podría multiplicar carga sin fallo transitorio demostrado | Rechazada |
| Timeout/circuit breaker | no hubo espera fuera del umbral ni dependencia remota caída | sería una táctica sin señal causal en la topología actual | Diferida |
| Caché distribuida | la caché local obtuvo resultado parcial y el escenario favorecía una clave caliente | distribuirla aumenta coherencia y operación sin beneficio material probado | Rechazada ahora |
| Métricas, trazas y contratos | los experimentos mostraron el valor de contexto, hashes y límites explícitos | útil aun sin microservicios | Aceptada como práctica |
| Escalado independiente | no hay evidencia de que Mensajería sea el único límite ni equipo/despliegue independiente | mantener como disparador futuro, no como supuesto actual | Supuesto por validar |

## Qué se acepta

- mantener contratos pequeños entre bounded contexts;
- medir Mensajería separadamente y registrar atribución de recursos;
- conservar observabilidad, trazabilidad y pruebas de fallos;
- diseñar decisiones reversibles y disparadores de revisión.

## Qué se rechaza

- equiparar bounded context con microservicio;
- crear broker, bases duplicadas o caché distribuida por anticipación;
- aplicar retry, timeout o circuit breaker sin un modo de fallo observado;
- presentar escalabilidad teórica como beneficio medido.

## Supuestos pendientes

- una carga sostenida o de múltiples chats podría comportarse distinto;
- podría aparecer una necesidad de disponibilidad independiente;
- el equipo o la organización podrían requerir despliegues separados;
- costos monetarios reales requieren proveedor, región, SLO y patrón de uso.

## Decisión recomendada a partir de la auditoría

Conservar el monolito modular y sus límites ejecutables. La estrategia de
microservicios añade complejidad innecesaria bajo la evidencia actual. La
propuesta se reabre únicamente ante disparadores explícitos de capacidad,
disponibilidad o autonomía de despliegue.

Esta conclusión alimenta ADR-004, pero debe ser ratificada humanamente en el
mini-comité 2.
