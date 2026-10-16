# Mini-comité 2 — distribución y resiliencia

Estado: **formato preparado; sesión y veredicto pendientes**.

Este documento se completa durante o después del mini-comité. No se registran
participantes, restricciones sorpresa ni aprobaciones antes de que ocurran.

## Datos de la sesión

- Fecha y hora:
- Participantes reales:
- Moderador:
- Restricción nueva introducida por el profesor, si existe:
- Enlace al PR/revisión:

## Evidencia mínima que todos deben revisar

1. [`Resultado de crecimiento`](../../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md)
2. [`Matriz de fallos`](../../experimentos/crecimiento-mensajeria/03-matriz-fallos.md)
3. [`Matriz de resiliencia`](../../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md)
4. [`Alternativas y costo`](alternativas-distribucion-s12.md)
5. [`Auditoría de IA`](auditoria-ia-microservicios-s12.md)
6. [`ADR-004 propuesto`](../adr/ADR-004-distribucion-mensajeria.md)
7. [`Fitness function`](fitness-function-distribucion-s12.md)

## Preguntas de decisión

- ¿La evidencia demuestra un límite que exija distribución?
- ¿Qué alternativa satisface la necesidad con menor costo reversible?
- ¿Qué modo de fallo real justifica cada táctica propuesta?
- ¿El costo relativo está expresado como estimación y no como medición?
- ¿Qué restricción nueva cambiaría la decisión?
- ¿La fitness function protege la decisión sin prohibir una re-decisión?

## Matriz de discusión

| Criterio | Monolito modular | Réplicas completas | Microservicio síncrono | Eventos |
| --- | --- | --- | --- | --- |
| Evidencia a favor | | | | |
| Riesgo principal | | | | |
| Costo aceptable | | | | |
| Condición para elegirla | | | | |

## Veredicto humano

Marcar una sola opción y explicar la razón:

- [ ] `RATIFICADA`: se conserva ADR-004 sin cambios sustanciales.
- [ ] `AJUSTADA`: se conserva la dirección, con cambios explícitos.
- [ ] `RECONSIDERADA`: se elige otra alternativa y se actualiza ADR-004.

Razón basada en evidencia:

```text
Pendiente del mini-comité 2.
```

## Re-decisión ante restricción nueva

Restricción:

```text
Pendiente.
```

Impacto sobre rendimiento, disponibilidad, operación y costo:

```text
Pendiente.
```

Nueva decisión y cambio de fitness function, si aplica:

```text
Pendiente.
```

## Registro posterior

- Cambio solicitado al ADR:
- Responsable real del cambio:
- Revisor real:
- Commit/PR:
- Fitness function ejecutada:
- Resultado de CI:

Hasta completar estas secciones, ADR-004 permanece como **propuesta**.
