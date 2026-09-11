# Guía de defensa individual C4 — 5 a 7 minutos

Cada integrante debe poder explicar una vista sin leer y responder cómo se
conecta con código y evidencia. Esta guía prepara el ejercicio; no demuestra que
ya se realizó.

## Ruta de navegación común

Desde la raíz del repositorio:

1. `dossier/05-c4-contexto.md` — personas y sistema.
2. `dossier/06-c4-contenedores.md` — Android, API, PostgreSQL y archivos.
3. `dossier/07-c4-componentes-backend.md` — recorrido interno de mensajería.
4. `dossier/09-c4-trazabilidad-localizacion.md` — caja → archivo y frontera.
5. `experimentos/medicion-escenario-01/resultados/resultado.json` — dato S4.

## Estructura de 6 minutos

### 0:00–0:40 — Sistema y categoría

“UTrabajo conecta estudiantes y empresas mediante ofertas, postulaciones y
chat. El profesor confirmó la categoría Mensajería y mesa de ayuda. El fenómeno
medido fue leer los últimos 50 mensajes de la conversación extrema.”

### 0:40–1:40 — C1

Mostrar actores y explicar que C1 es caja negra. No mencionar Spring o
PostgreSQL como si fueran actores. Identificar la audiencia: stakeholders y
auditor.

### 1:40–2:45 — C2

Mostrar cuatro contenedores reales. Aclarar que JDBC y Flyway son tecnologías,
no despliegues independientes. Explicar HTTP/Bearer y acceso SQL.

### 2:45–4:10 — C3 y código

Seguir una solicitud:

```text
k6/Android → seguridad → ChatController
→ UTrabajoService → JdbcClient → PostgreSQL
```

Abrir `ChatController.kt`, método `messages`; luego `UTrabajoService.kt`, método
`messages` y `requireChatParticipant`; terminar en el índice Flyway V2.

### 4:10–5:15 — Medición y límite

Decir el dato exacto: p95 válidos 9,451; 8,265; 9,109 ms y mediana 9,109 ms.
k6, la API y PostgreSQL compartieron equipo. Android e Internet no participaron.
S4 mide total HTTP; no reparte el costo interno.

### 5:15–6:00 — Decisión y siguiente evidencia

Explicar que S7 usa EXPLAIN para comparar `OFFSET 0`, `OFFSET 50000` y cursor.
Relacionar el resultado de paginación con ADR-003, el estilo monolito modular con
ADR-001 y los límites/dependencias con ADR-002.

## Preguntas probables

**¿Por qué mensajería y no catálogo?** Porque la operación y la semilla observan
conversaciones y una distribución extrema de mensajes; no una búsqueda textual
de ofertas.

**¿Por qué PostgreSQL es una caja pero JDBC no?** PostgreSQL se despliega y
ejecuta independientemente; JDBC es el mecanismo interno con el que la API lo
consulta.

**¿Los 9,109 ms prueban rendimiento móvil?** No. Prueban el endpoint local en la
topología declarada.

**¿Ya demostraron que offset profundo es más costoso?** Sí, MSG-LOC-01 registró
0,120 ms en `OFFSET 0`, 102,050 ms en `OFFSET 50000` y 0,123 ms con cursor bajo
las condiciones declaradas. No debe extrapolarse a producción sin nuevas
mediciones.

**¿Por qué no microservicio?** No existe presión medida que compense costo,
seguridad y operación distribuidos; la alternativa queda reversible.

**¿Qué protege ADR-002?** La dirección de dependencias. En particular, los
controladores no deben acceder directamente a JDBC/SQL; la regla se ejecuta en
CI mediante `scripts/check_architecture.py`.

## Asignación sugerida para tres personas

- Persona 1: C1/C2, procedencia y correcciones del C4.
- Persona 2: C3 backend, trazabilidad y localización S4/S7.
- Persona 3: alternativas, ADR y restricción de arquitectura/CI.

Aunque exista reparto, cada persona debe poder recorrer las cinco evidencias
comunes y responder los límites del dato.
