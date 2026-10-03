# Catálogo y filtro de eventos candidatos — S9

## Regla usada

Un evento de dominio representa **un hecho relevante que ya ocurrió** y que otro contexto podría necesitar conocer. No se convierte cada CRUD, click o detalle técnico en un evento.

## Eventos aceptados como candidatos

| Evento | Productor lógico | Consumidores potenciales | Razón |
| --- | --- | --- | --- |
| OfertaPublicada | Ofertas | Postulaciones, Mensajería | cambia la existencia de una oportunidad disponible |
| OfertaActualizada | Ofertas | Postulaciones, Mensajería | puede cambiar información de referencia consumida por otros contextos |
| OfertaRetirada | Ofertas | Postulaciones, Mensajería | otros contextos no deberían tratarla como una oportunidad vigente |
| PostulacionCreada | Postulaciones | futuras notificaciones/analítica | hecho real de negocio ya persistido |
| PostulacionCancelada | Postulaciones | futuras notificaciones/analítica | hecho real que cambia la relación estudiante-oferta |
| ChatCreado | Mensajería | futura analítica/soporte | hecho de dominio verificable |
| MensajeEnviado | Mensajería | futura notificación/analítica | hecho relevante una vez persistido el mensaje |

Que un evento sea candidato **no significa que UTrabajo deba publicarlo ahora**.

## Propuestas rechazadas

| Propuesta | Clasificación | Razón |
| --- | --- | --- |
| BotonEnviarMensajePresionado | Rechazada | interacción de UI, no hecho de dominio |
| PantallaChatAbierta | Rechazada | detalle de presentación |
| ApiConsultada | Rechazada | detalle técnico de transporte |
| JdbcActualizado | Rechazada | mecanismo de persistencia, no lenguaje del dominio |
| LatestMessageUpdated | Redundante | es una consecuencia técnica de MensajeEnviado, no necesita identidad de evento propia |
| StudentHired | Inventada actualmente | el sistema no implementa contratación |
| MessageRead | Inventada actualmente | no existe estado de lectura en el modelo |
| NotificationSent | Inventada actualmente | no existe subsistema de notificaciones |
| CompanyVerified | Inventada actualmente | no existe flujo/estado de verificación empresarial |
| JobMatched | Inventada actualmente | no existe motor de matching |

## Eventos relevantes para la frontera del Spike 1

La alternativa asíncrona Mensajería ← Ofertas necesitaría como mínimo hechos que permitan construir una proyección coherente de la oferta. Los candidatos son:

- OfertaPublicada;
- OfertaActualizada;
- OfertaRetirada.

Antes de adoptarlos habría que especificar:

- identificador estable;
- versión de esquema;
- instante del hecho;
- semántica de duplicados;
- estrategia de reintentos;
- publicación confiable respecto al commit de PostgreSQL.

Esos mecanismos quedan fuera del Spike 1 porque el objetivo es primero comprobar si la opción síncrona ya satisface la necesidad con menor complejidad.
