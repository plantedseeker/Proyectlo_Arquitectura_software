# Comparación de alternativas arquitectónicas — S7

## Alcance

Se comparan dos decisiones distintas para no mezclar escalado de despliegue con
paginación de datos:

1. ubicación y límites del módulo de mensajería;
2. lectura del historial de mensajes.

## Datos medidos disponibles

- S4: mediana de p95 de **9,109 ms** al pedir los últimos 50 mensajes de una
  conversación de 100.000, con 10 VU y 40 solicitudes por corrida válida.
- S4: 100 % de comprobaciones y 0 % de fallos en 120 solicitudes válidas.
- S4: k6, API y PostgreSQL compartieron el mismo equipo físico.
- S7: autorización 0,081 ms; `OFFSET 0` 0,120 ms; `OFFSET 50000`
  102,050 ms; cursor equivalente 0,123 ms. El offset profundo costó 829,675× el
  cursor y usó escaneo secuencial/trabajo temporal; reciente y cursor usaron
  `idx_message_chat_recent`.

## Interpretaciones sustentadas

- La página reciente cumple el umbral S4 en la configuración medida.
- S4 no justifica distribuir mensajería ni agregar infraestructura.
- S4 no permite concluir cómo se comportan páginas profundas.
- El C4 y el código muestran una frontera recuperable
  `controller → service → JDBC`, por lo que es posible protegerla en CI.

## Supuestos por validar

- El equipo espera mantener un despliegue local en esta etapa.
- Android todavía consume únicamente la página reciente de mensajes.
- Si Android incorpora navegación profunda, el comportamiento de MSG-LOC-01 se
  mantendrá bajo carga HTTP y datos de producción; esto todavía debe validarse.

## Decisión 1 — ubicación de mensajería

| Criterio | Monolito sin regla | Monolito modular + regla | Microservicio + broker |
| --- | --- | --- | --- |
| Complejidad | Baja hoy, creciente | Baja/media y explícita | Alta |
| Seguridad | Riesgo de dispersión | Flujo de autorización localizable | Autorización entre servicios |
| Operación | Un despliegue | Un despliegue + prueba CI | Servicio, broker, contratos y observabilidad |
| Costo inicial | Mínimo | Bajo | Alto |
| Escalado independiente | No | No | Sí |
| Reversibilidad | Media si se acumula deuda | Alta | Media/baja tras separar datos |
| Evidencia actual a favor | Ninguna para omitir límites | C4, código y S4 | Ninguna presión medida |
| Resultado propuesto | Descartar | **Seleccionar** | Diferir |

## Decisión 2 — paginación

| Criterio | Historial completo | `LIMIT/OFFSET` indexado | Cursor `(sent_at,id)` |
| --- | --- | --- | --- |
| Complejidad de contrato | Baja | Baja | Media |
| Costo con profundidad | Crece con todo el historial | Puede crecer al descartar filas | Esperado casi constante; medir |
| Consistencia con mensajes nuevos | Débil | Puede desplazar páginas | Más estable |
| Compatibilidad actual | No | **Total** | Requiere evolución |
| Seguridad/abuso | Alto consumo | Limitable | Limitable + validar cursor |
| Operación | Misma BD | Misma BD e índice actual | Misma BD e índice actual |
| Reversibilidad | Alta, con ruptura al cambiar | Alta si se agrega cursor | Alta durante coexistencia |
| Resultado propuesto | Descartar | **Mantener para página reciente** | Activar si S7 + necesidad lo justifican |

## Criterio de decisión

Se elige la alternativa menos compleja que satisfaga el escenario medido y
proteja una evolución reversible. Tener una tecnología más distribuida no es
una mejora por sí misma; debe responder a una presión demostrada.

## Trazabilidad

- [`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md)
- [`ADR-002`](../adr/ADR-002-paginacion-historial-mensajes.md)
- [`Experimento S7`](../../experimentos/localizacion-s7/README.md)
- [`Crítica de propuesta IA`](propuesta-ia-critica.md)
