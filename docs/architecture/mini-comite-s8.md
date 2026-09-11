# Preparación del mini-comité técnico — S8

## Decisiones a defender

- ADR-001: monolito modular con límite automático entre controladores y JDBC.
- ADR-002: `LIMIT/OFFSET` indexado para la página reciente y migración gradual a
  cursor solo ante evidencia/necesidad de historial profundo.

## Evidencia que debe abrirse

1. `dossier/07-c4-componentes-backend.md`: recorrido interno.
2. `experimentos/medicion-escenario-01/resultados/resultado.json`: dato S4.
3. `experimentos/localizacion-s7/resultados/localizacion.json`: dato causal S7.
4. `docs/architecture/alternativas-s7.md`: comparación justa.
5. `scripts/check_architecture.py`: protección ejecutable.
6. URL de la corrida roja deliberada y URL verde posterior.

## Guion por rol

### CTO — valor y evolución

- Pregunta: ¿por qué no extraer mensajería ahora?
- Respuesta defendible: no existe presión medida de escalado independiente; el
  monolito modular satisface el alcance local con menor costo y deja una ruta
  reversible.
- Pregunta: ¿qué obligaría a redecidir?
- Respuesta: SLA incumplido, disponibilidad exclusiva, equipos/despliegues
  separados o carga asíncrona demostrada.

### Seguridad — autorización y superficie

- Pregunta: ¿cómo se evita leer chats ajenos?
- Respuesta: seguridad Bearer más `requireChatParticipant` antes de consultar
  mensajes; mostrar el método y sus pruebas.
- Pregunta: ¿qué protege la restricción?
- Respuesta: impide acceso JDBC directo desde controladores, preservando el
  punto de autorización/negocio. No afirma cubrir todos los riesgos.

### Finanzas/operación — costo total

- Pregunta: ¿qué cuesta cada opción?
- Respuesta: el monolito modular añade una prueba; un microservicio añade
  despliegue, observabilidad, red, secretos, broker y operación recurrente.
- Pregunta: ¿por qué no Redis?
- Respuesta: no hay patrón medido que pague caché ni estrategia de invalidación.

## Minuta que debe completar el equipo después del comité

No completar anticipadamente.

- Fecha y hora:
- Participantes y roles:
- Evidencia efectivamente revisada:
- Objeciones del CTO:
- Objeciones de seguridad:
- Objeciones de finanzas/operación:
- Respuestas o cambios acordados:
- Veredicto ADR-001: `CONFIRMADA / AJUSTADA / RECONSIDERADA`
- Veredicto ADR-002: `CONFIRMADA / AJUSTADA / RECONSIDERADA`
- Responsable y fecha de cada acción:
- Enlace al PR/acta:

## Regla de integridad

La existencia de este guion demuestra preparación, no realización. El estado de
los ADR solo se cambia después de que las personas indicadas hagan el ejercicio
y registren su veredicto real.
