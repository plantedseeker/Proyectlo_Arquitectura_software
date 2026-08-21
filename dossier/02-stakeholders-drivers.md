# 02 — Stakeholders, drivers y riesgos

## Mapa de stakeholders

| Stakeholder | Interés o preocupación | Influencia | Evidencia o respuesta |
| --- | --- | --- | --- |
| Estudiante | Mensajes recientes rápidos, ordenados y privados | Alta | Escenarios MSG-PERF-01, MSG-SEC-01 y MSG-REL-01 |
| Empresa | Atender múltiples conversaciones sin mezclar candidatos | Alta | Listado reciente e aislamiento por participante |
| Profesor/auditor | Experimento trazable, medible y reproducible | Alta | Dossier, k6, semilla, cuatro corridas y PR |
| Equipo de desarrollo | Cambios pequeños que no rompan Android | Alta | Paginación compatible, migración Flyway y CI |
| Institución educativa | Evitar exposición de información académica/personal | Media | Datos sintéticos y autorización en API |
| Operación futura | Migrar a Internet sin rediseñar todas las pantallas | Media | API separada de PostgreSQL y decisiones reabribles |

## Restricciones

| Tipo | Restricción | Consecuencia arquitectónica |
| --- | --- | --- |
| Técnica | Android/Kotlin existente | Se conserva Retrofit y el contrato HTTP |
| Técnica | PostgreSQL 16 exigido | Mensajes, chats e índices viven en esquema relacional |
| Técnica | Ejecución local en semana 4 | Se documenta topología compartida y energía |
| Económica | Sin presupuesto de nube/licencias | Docker, k6 y herramientas abiertas |
| Organizacional | Evidencia mediante PR por integrante | Rama y PR obligatorios; no commits directos para el dossier |
| Organizacional | Fecha fija | Se prioriza lectura de últimos mensajes sobre WebSocket/push |
| Legal/ética | No usar chats reales | Semilla completamente sintética y determinista |

## Drivers preliminares priorizados

| Prioridad | Driver | Justificación | Decisión asociada |
| ---: | --- | --- | --- |
| 1 | Rendimiento al abrir conversaciones extremas | La categoría exige pocos y muchísimos mensajes | Página de últimos 50 e índice por chat/fecha |
| 2 | Confidencialidad entre conversaciones | Un mensaje no puede quedar visible a terceros | `requireChatParticipant` antes de leer/escribir |
| 3 | Orden e integridad de mensajes | El chat debe presentar una secuencia coherente | Orden estable por `sent_at` e `id`; PK y FK |
| 4 | Modificabilidad del cliente | El experimento no debe obligar a rehacer Android | Parámetros con valores predeterminados compatibles |
| 5 | Reproducibilidad | El auditor debe repetir condiciones y decisión | Docker Compose, k6 fijado, semilla y resultados JSON |

## Inventario inicial y evaluación crítica de riesgos propuestos por IA

Clasificación usada: **válido**, **genérico**, **irrelevante** o **falso**. El
registro conserva la sugerencia inicial y el juicio del equipo; aceptar una
salida de IA sin crítica no cuenta como evidencia.

| ID | Sugerencia inicial de IA | Clasificación | Evaluación crítica del equipo | Acción |
| --- | --- | --- | --- | --- |
| R-01 | Una conversación enorme puede devolver todos los mensajes y agotar memoria/latencia | Válido | El endpoint original no tenía límite | Paginar a 1–100 y medir los últimos 50 |
| R-02 | La distribución uniforme de mensajes ocultaría el fenómeno | Válido | La categoría exige extremos, no promedios | Semilla 900/90/9/1 con hasta 100.000 mensajes |
| R-03 | Un usuario podría leer un chat ajeno | Válido | El impacto es alto aunque existe control actual | Conservar y probar `requireChatParticipant` |
| R-04 | “El sistema puede ponerse lento” | Genérico | No identifica estímulo, escala ni medida | Reformulado como MSG-PERF-01 con p95 y carga |
| R-05 | “La base puede caerse” | Genérico | No hay condición ni probabilidad verificable | Vigilar salud; fuera del escenario principal |
| R-06 | Se necesita Kubernetes multirregión para la semana 4 | Irrelevante | Contradice el alcance local y el presupuesto | No implementar; reabrir solo al migrar a Internet |
| R-07 | Android se conecta directamente a PostgreSQL y expone su contraseña | Falso | Android usa la API y no contiene credenciales DB | Mantener separación y documentarla |
| R-08 | No existe autorización para mensajes | Falso | La API verifica que el usuario pertenezca al chat | Mantener prueba de acceso y revisar regresiones |
| R-09 | El polling puede multiplicar lecturas cuando crece el número de usuarios | Válido | La aplicación consulta periódicamente mensajes | Limitar página; evaluar push/WebSocket en evolución |
| R-10 | Usar datos reales haría más representativa la prueba | Irrelevante | Aumenta riesgo de privacidad sin ser necesario | Usar solo datos sintéticos reproducibles |

## Riesgos residuales

- La medición local no representa Internet ni un dispositivo móvil real.
- `offset` es suficiente para la primera página, pero páginas profundas podrían
  requerir cursor compuesto por fecha e identificador.
- La confirmación del profesor debe adjuntarse como evidencia externa.
- Los PR por otros integrantes dependen de contribuciones humanas reales.
