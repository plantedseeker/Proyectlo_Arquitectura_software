# Registro crítico de IA — Módulo 5

## Propósito

Registrar qué propuestas surgieron durante el análisis asistido, cuáles se conservaron y cuáles se rechazaron al contrastarlas con el código y el dominio real.

## Propuestas aceptadas para análisis

### OfertaPublicada / OfertaActualizada / OfertaRetirada

**Aceptadas como eventos candidatos**, porque describen hechos reales del contexto Ofertas y podrían ser relevantes para consumidores como Mensajería o Postulaciones.

No se afirma que ya estén implementados ni que deban publicarse.

### MensajeEnviado

**Aceptado como evento candidato**, porque representa un mensaje que ya fue persistido y puede ser relevante para capacidades futuras.

No se introduce un broker ni notificaciones solo por esta propuesta.

### Contrato síncrono Mensajería → Ofertas

**Aceptado como alternativa a experimentar**, porque hoy existe una dependencia real: createOrGetChat necesita company_id de job_offer.

La aceptación para el spike no equivale a aceptación arquitectónica definitiva.

## Propuestas rechazadas

- BotonEnviarMensajePresionado: interacción de UI.
- PantallaChatAbierta: estado de presentación.
- JdbcActualizado: mecanismo técnico.
- MessageRead: funcionalidad inexistente.
- NotificationSent: subsistema inexistente.
- StudentHired: flujo inexistente.
- JobMatched: motor inexistente.
- Introducir Kafka/RabbitMQ de inmediato: no existe presión medida que pague el costo.

## Decisión humana pendiente

El equipo debe revisar el preregistro, la ejecución y el veredicto antes de ratificar ADR-003. La IA no determina si el sistema adopta REST, eventos, CQRS o Event Sourcing.
