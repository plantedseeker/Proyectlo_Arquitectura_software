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
   ↓ jobId, companyId, active, title
Mensajería
~~~

El Spike 1 probará una representación HTTP explícita de este contrato.

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

## Alternativa seleccionada para experimentar

Se selecciona **A — contrato síncrono** únicamente como hipótesis de Spike 1.

Esto NO es todavía la decisión final del ADR-003 de Módulo 5. La decisión final debe citar:

1. Context Map;
2. contrato;
3. preregistro;
4. condiciones medidas;
5. resultado;
6. veredicto.

## Condiciones que harían revisar la opción síncrona

- Mensajería necesitara seguir operando cuando Ofertas no está disponible.
- Aparecieran despliegues independientes o equipos con ciclos separados.
- El volumen de consultas entre contextos generara presión medible.
- Se adoptara procesamiento asíncrono por necesidades funcionales reales.
- El spike refutara la hipótesis definida.
