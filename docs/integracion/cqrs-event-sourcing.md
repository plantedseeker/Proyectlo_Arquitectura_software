# Evaluación de CQRS y Event Sourcing — Módulo 5

## CQRS

### Problema que podría resolver

CQRS separaría modelos de escritura y lectura si ambos evolucionaran con necesidades significativamente distintas. En UTrabajo existen lecturas optimizadas —por ejemplo historial paginado y resumen de chats— pero actualmente comandos y consultas siguen siendo manejables dentro del mismo backend y PostgreSQL.

### Beneficios potenciales

- modelos de lectura especializados;
- proyecciones orientadas a consultas;
- posibilidad de escalar lectura/escritura de forma diferente si alguna vez se demuestra esa presión.

### Costos

- modelos adicionales;
- sincronización de proyecciones;
- más superficie de pruebas;
- consistencia eventual posible;
- recuperación/reconstrucción de proyecciones;
- más complejidad operativa y observabilidad.

### Evaluación actual

**No adoptar CQRS completo en esta etapa.**

La evidencia S4/S7 identifica un costo de paginación profunda, pero ese problema tiene una solución local y reversible mediante estrategia de consulta/índice/cursor. No demuestra que el modelo de lectura y escritura de todo Mensajería deba separarse.

El Spike 1 podrá aportar nueva evidencia sobre una frontera concreta, pero no se interpretará automáticamente como justificación de CQRS.

## Event Sourcing

### Beneficio potencial

Reconstruir el estado actual reproduciendo un historial completo de eventos inmutables y conservar auditoría temporal detallada.

### Costos

- almacenamiento y retención de eventos;
- evolución/versionamiento de esquemas;
- reconstrucción del estado;
- snapshots si el historial crece;
- manejo de eventos incompatibles;
- mayor complejidad operacional y de pruebas.

### Evaluación actual

**No adoptar Event Sourcing en esta etapa.**

UTrabajo ya conserva las entidades necesarias en PostgreSQL y no existe un requisito medido de reconstrucción completa del estado a partir de eventos.

## Consistencia eventual

No se acepta de forma uniforme para todo el sistema.

| Dato/decisión | Tratamiento actual recomendado |
| --- | --- |
| Autorización de participante de chat | consistencia fuerte |
| Persistencia de un mensaje enviado | consistencia fuerte |
| Unicidad de postulación | consistencia fuerte |
| Vigencia de oferta al aplicar | preferir estado actual mientras sea barato |
| Proyección/preview derivado para lectura | podría tolerar consistencia eventual si aparece beneficio medido |

## Conclusión previa al spike

Los patrones no se incorporan por sofisticación. La opción de **no adoptar** CQRS/Event Sourcing permanece válida y deberá reabrirse solo ante evidencia que compense su costo.
