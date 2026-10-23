# Resumen ejecutivo — Semanas 13 y 14

## Tesis ejecutiva

UTrabajo ya tenía controles reales de autenticación, autorización, persistencia y CI, pero eso no equivalía a un modelo de seguridad ni a un plan operativo. S13 convierte esos controles en un **threat model priorizado** usando OWASP Top 10:2025 y distingue riesgos reales de amenazas genéricas. S14 toma esos riesgos, los fallos ya observados y la evidencia de rendimiento S11–S12 para diseñar un **despliegue conceptual observable**, con dos alternativas de costo, métricas/logs/trazas/alertas, una matriz de riesgos operativos y ADR-005.

La decisión evita dos errores:

1. declarar “seguro” un laboratorio local;
2. introducir Kubernetes/microservicios antes de tener una necesidad medida.

## Resultado en una página

| Pregunta | Respuesta respaldada |
| --- | --- |
| ¿Qué amenaza domina? | acceso indebido a datos privados/configuración local expuesta; además falta detección operativa |
| ¿El sistema ya tiene controles? | sí: BCrypt, tokens aleatorios+hash, expiración, roles, participante de chat, SQL parametrizado, límites de archivo y CI |
| ¿Qué riesgo nuevo se detectó? | documentos sensibles bajo `/uploads/**` anónimo y ausencia de logging/alerting de seguridad |
| ¿Qué propone S13? | priorizar T01–T12 y cerrar P0 antes de Internet |
| ¿Qué despliegue propone S14? | monolito modular administrado: TLS + API + PostgreSQL administrado + object storage privado + observabilidad |
| ¿Alternativa? | monolito redundante detrás de load balancer |
| ¿Costo? | A=1 D0; B=2–3 D0, estimación relativa, no precio |
| ¿SLO de latencia reutilizado? | p95 ≤500 ms |
| ¿Cómo sabremos que falla? | health, 5xx, p95, pool DB, storage, auth/403, backups |
| ¿Qué decisión formal? | ADR-005 adopta A conceptualmente y conserva B como evolución reversible |
| ¿La fitness function funciona? | sí; CI la ejecuta y S14 conserva una violación deliberada roja seguida de corrección verde |

# Semana 13 — Seguridad

## Método

1. inventariar activos;
2. dibujar límites de confianza;
3. mapear OWASP Top 10:2025;
4. contrastar cada amenaza con código/configuración;
5. puntuar probabilidad e impacto;
6. separar control existente, riesgo residual y control propuesto;
7. rechazar amenazas genéricas sin superficie real.

## Activos críticos

- credenciales/sesiones;
- chats;
- CV y documentos empresariales;
- ofertas/postulaciones;
- PostgreSQL;
- secrets/configuración;
- CI/dependencias.

## Límites de confianza

~~~text
Android
   │
=== TB1 ===
   ▼
Spring Security → Controllers → Services
   │                         │
=== TB2 ===                === TB3 ===
   ▼                         ▼
PostgreSQL               File storage
                             │
                         === TB4 ===
                             ▼
                          /uploads

CI/dependencias === TB5
Endpoints internos === TB6
~~~

## Amenazas que debes saber defender

### T01 — acceso a chat ajeno

OWASP A01. Es de impacto crítico, pero existe control real: `requireChatParticipant` y pruebas negativas.

**Mensaje:** “No asumimos que conocer el UUID autoriza; el servidor resuelve el principal y verifica participación.”

### T02 — documentos sensibles públicos

`SecurityConfig` permite `/uploads/**` de forma anónima. Los UUID reducen descubrimiento, pero **no sustituyen autorización**.

**Decisión:** antes de Internet, documentos sensibles deben descargarse autenticadamente o mediante URL firmada; avatar puede tener política distinta.

### T03 — configuración local publicada

HTTP, `SEED_DEMO=true`, credenciales locales e integration token demo son válidos en laboratorio, no en producción.

**Decisión:** perfil de producción con HTTPS, secrets externos y seed deshabilitado.

### T04 — ceguera de seguridad

Health/info no dicen quién está fallando autenticación, qué endpoint produce 5xx o si crecen los 403.

**Decisión:** S14 crea observabilidad de seguridad.

### T05/T06 — autenticación y token

Hay BCrypt, token aleatorio, hash y expiración. Falta rate limiting de login y fuera de local TLS es obligatorio.

## Amenazas genéricas rechazadas

- XXE: no se procesa XML.
- SSRF crítico: el backend no hace fetch de URLs arbitrarias del usuario.
- XSS server-side como principal: API JSON + Android Compose, no templates HTML.
- CSRF crítico solo por estar deshabilitado: API stateless con Bearer header; reabrir si se usan cookies.
- microservicios “por seguridad”: agregan secretos/red y no corrigen automáticamente las amenazas P0.

## Conclusión S13

El sistema tiene una base razonable para laboratorio, pero no está autorizado a declararse “production secure”. Las prioridades P0 son **control de acceso/documentos, configuración segura y capacidad de detectar incidentes**.

# Semana 14 — Despliegue y observabilidad

## Dos alternativas

### A — administrado mínimo — seleccionada

~~~text
Android
   │ HTTPS
   ▼
TLS / Reverse proxy
   ▼
1 × Spring Boot API
   ├── PostgreSQL administrado
   ├── Object storage privado
   ├── Secret manager
   └── Metrics + Logs + Traces
~~~

Costo: **1 D0**.

### B — administrado redundante

~~~text
Android
   │ HTTPS
   ▼
Load balancer
 ┌───────┴───────┐
 ▼               ▼
API 1           API 2
 └───────┬───────┘
         ▼
 PostgreSQL administrado
 + object storage + observabilidad
~~~

Costo: **2–3 D0**.

B mejora disponibilidad de API y escalado horizontal, pero S11 no demostró que hoy haga falta.

## Observabilidad

### Métricas esenciales

~~~text
Rate
Errors
Duration p95/p99
JVM
Hikari pool
DB health/latency
401/403/login failures
Storage usage/errors
~~~

### Logs

Estructurados con:

~~~text
timestamp level service instance
requestId traceId spanId
route status duration error.type
~~~

Nunca tokens, contraseñas, chat bodies o documentos.

### Trazas

~~~text
HTTP
 ↓
Security
 ↓
Controller
 ↓
Service/Auth
 ↓
JDBC
 ↓
PostgreSQL
~~~

### Alertas que debes memorizar

~~~text
health DOWN >2 min        → crítica
5xx >5 % por 5 min       → crítica
p95 >500 ms por 5 min    → crítica
DB pool >90 % sostenido  → warning/crítica
storage >85 %            → warning
backup vencido           → crítica
login/403 anómalos       → seguridad
~~~

Solo p95=500 ms proviene del SLO previo. Los demás son umbrales operativos iniciales sujetos a calibración.

## Fallos previos convertidos en señales

| Fallo | Señal S14 |
| --- | --- |
| PostgreSQL cae | health + JDBC + pool |
| API cae | health DOWN |
| storage lleno | % uso + errores IO |
| token inválido | 401 |
| chat ajeno | 403 |
| offset profundo | p95 + DB duration |
| polling crece | request rate + p95 |
| config local publicada | gate de despliegue, no esperar una alerta |

## ADR-005

Decisión propuesta:

> El primer despliegue público de UTrabajo debe conservar el monolito modular, pero ejecutarse detrás de TLS, con PostgreSQL administrado, almacenamiento privado, secrets externos y observabilidad centralizada. La redundancia de API se incorpora solo ante un SLO o límite medido que la justifique.

Paginación conserva su decisión histórica como ADR-006.

# Fitness function — demostración S14

La regla de distribución ya formaba parte de CI:

~~~text
scripts/check_distribution_decision.py
threshold = 0 violations
~~~

Detecta, entre otros:

- nuevo servicio Compose no aprobado;
- Kafka/RabbitMQ/Redis/Pulsar/NATS;
- caché experimental habilitada por default.

S14 genera deliberadamente una violación **solo en rama/PR de evidencia**. El CI debe fallar en `Enforce ADR-004 distribution decision`. Después se restaura la configuración permitida y el mismo PR vuelve a verde antes de fusionarse.

Esto demuestra:

~~~text
regla escrita
    ↓
regla ejecutada
    ↓
violación
    ↓
CI ROJO
    ↓
corrección
    ↓
CI VERDE
~~~

No se fusiona la violación.

# Recorrido recomendado para el profesor

1. Abrir `dossier/10-seguridad.md`.
2. Mostrar activos + trust boundaries.
3. Mostrar T01–T04 y controles.
4. Mostrar amenazas IA descartadas.
5. Abrir `dossier/12-despliegue-observabilidad.md`.
6. Dibujar A vs B y explicar 1 D0 vs 2–3 D0.
7. Mostrar métricas/logs/trazas/alertas.
8. Abrir matriz operativa.
9. Abrir ADR-005.
10. Mostrar CI rojo/verde de la fitness function.

# Guion de defensa — 8 a 10 minutos

| Tiempo | Contenido |
| ---: | --- |
| 0:00–0:45 | tesis S13–S14 |
| 0:45–2:00 | activos y límites de confianza |
| 2:00–3:30 | amenazas prioritarias y controles |
| 3:30–4:15 | amenazas IA rechazadas |
| 4:15–5:30 | despliegue A vs B y costo |
| 5:30–7:00 | métricas, logs, trazas y alertas |
| 7:00–8:00 | riesgos operativos |
| 8:00–9:00 | ADR-005 |
| 9:00–10:00 | CI rojo→verde y conclusión |

## Explicación compacta

> En S13 dejamos de tratar seguridad como una lista genérica: modelamos activos y límites de confianza, contrastamos OWASP contra el código y priorizamos amenazas reales. Encontramos controles existentes fuertes en autenticación, autorización y SQL parametrizado, pero también riesgos que impiden publicar la configuración local tal cual, especialmente documentos sensibles públicos y falta de logging/alerting. En S14 convertimos esos riesgos y los fallos anteriores en un plan operativo: comparamos dos despliegues, elegimos como primer paso un monolito administrado observable, definimos señales y alertas y lo formalizamos en ADR-005. Finalmente demostramos que la fitness function realmente protege una decisión haciendo fallar CI con una violación deliberada y corrigiéndola antes de fusionar.

# Qué NO afirmar

- “Cumplimos OWASP completo”.
- “La aplicación ya está segura para Internet”.
- “Tenemos alta disponibilidad desplegada”.
- “Grafana k6 ya es toda la observabilidad de producción”.
- “La alternativa B es necesaria”.
- “Los umbrales de seguridad fueron medidos”; varios son iniciales.
- “La violación roja llegó a main”.

# Estado honesto de cierre

La evidencia documental/técnica de S13–S14 está preparada. La infraestructura pública sigue siendo conceptual y cualquier ratificación humana debe registrarse solo cuando ocurra.
