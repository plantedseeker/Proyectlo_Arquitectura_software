# Integración S9 — síncrono vs asíncrono

## Pregunta arquitectónica

Mensajería necesita conocer información mínima de Ofertas para asociar una conversación a una oportunidad laboral. El código actual consulta directamente la tabla job_offer desde UTrabajoService.

La pregunta del Módulo 5 no es si REST o eventos son mejores en general, sino:

> ¿Qué mecanismo protege mejor esta frontera bajo las necesidades y evidencia actuales de UTrabajo?

## Alternativa A — contrato síncrono

### Mecanismo

Mensajería solicita en el momento de crear el chat un contexto mínimo de la oferta:

~~~text
Mensajería
   ↓ solicitud
Ofertas
   ↓ jobId, companyId
Mensajería
~~~

El Spike 1 probó una representación HTTP explícita de este contrato.

### Beneficios esperados

- La decisión usa el estado actual del proveedor.
- No requiere mantener una copia local del estado de la oferta.
- El fallo es visible en la misma operación.
- El modelo mental es pequeño para el tamaño actual del proyecto.
- La reversibilidad es alta: el contrato puede sustituirse más adelante sin cambiar el dominio de Mensajería.

### Costos y riesgos

- Acoplamiento temporal: si el proveedor no responde, la creación de chat no puede obtener ese dato.
- Si se distribuyera físicamente, introduciría red, timeouts y manejo de fallos.
- El consumidor depende de la disponibilidad del contrato en tiempo real.

## Alternativa B — integración asíncrona mediante eventos

### Mecanismo

Ofertas publica hechos relevantes, por ejemplo:

- OfertaPublicada;
- OfertaActualizada;
- OfertaRetirada.

Mensajería mantiene una proyección mínima con jobId, companyId, active y title.

### Beneficios potenciales

- Menor acoplamiento temporal entre los contextos.
- Mensajería podría operar con su proyección aunque el proveedor estuviera temporalmente indisponible.
- Facilita un futuro procesamiento asíncrono si aparece presión real.

### Costos y riesgos

- Consistencia eventual: la proyección puede estar retrasada respecto a Ofertas.
- Deben manejarse duplicados, reintentos, orden e idempotencia.
- Aparece estado replicado que debe reconciliarse.
- Requiere definir publicación confiable; un evento emitido antes del commit o perdido después del commit puede dejar proyecciones incorrectas.
- Un broker o outbox sería complejidad adicional no justificada todavía por evidencia.

## Alternativa seleccionada y resultado

Se seleccionó **A — contrato síncrono** como hipótesis de Spike 1.

La ejecución válida del 2026-10-16 obtuvo p95 de 4,781 ms, 4,360 ms y
4,710 ms en las tres corridas válidas, con 0 fallos HTTP inesperados y todos
los checks funcionales aprobados. La hipótesis quedó **respaldada** bajo las
condiciones registradas y el alcance quedó **ajustado** antes de medir al
payload mínimo `jobId` + `companyId`.

ADR-003 propone la alternativa síncrona para la necesidad actual. La decisión
es sobre la frontera lógica: mientras ambos contextos estén en el mismo
monolito, no se recomienda una llamada HTTP del proceso hacia sí mismo.

La trazabilidad incluye:

1. Context Map;
2. contrato;
3. preregistro;
4. condiciones medidas;
5. resultado;
6. veredicto;
7. ADR-003.

Evidencia: `experimentos/spike-01-integracion/veredicto.md` y
`docs/adr/ADR-003-integracion-contextos.md`.

## Condiciones que harían revisar la opción síncrona

- Mensajería necesitara seguir operando cuando Ofertas no está disponible.
- Aparecieran despliegues independientes o equipos con ciclos separados.
- El volumen de consultas entre contextos generara presión medible.
- Se adoptara procesamiento asíncrono por necesidades funcionales reales.
- El spike refutara la hipótesis definida.
