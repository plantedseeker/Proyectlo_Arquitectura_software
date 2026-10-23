# Semana 14 — Despliegue conceptual y observabilidad

> Evidencia principal exigida por la rúbrica: **diagrama de despliegue con costo relativo + plan de observabilidad vinculado a los escenarios de fallo del Módulo 6**.

## 1. Punto de partida

La decisión ADR-004 conserva Mensajería dentro del monolito modular porque S11 no encontró saturación sostenida hasta 200 VU. S14 no contradice esa decisión: pregunta **cómo desplegar y observar ese monolito si sale del entorno local**.

No se afirma que estas topologías estén desplegadas hoy. Son diseños conceptuales comparables.

## 2. Alternativa A — monolito administrado mínimo

~~~text
Android
  │
  │ HTTPS
  ▼
DNS / TLS / Reverse proxy
  │
  ▼
Spring Boot API — 1 instancia
  │            │
  │ JDBC/TLS   └── Object storage privado
  ▼
PostgreSQL administrado
  │
  └── backup + restore
~~~

### Características

- una instancia de API;
- PostgreSQL administrado;
- almacenamiento de objetos privado para CV/documentos;
- TLS en el borde;
- secrets externos;
- logs/métricas/trazas centralizados;
- health checks y alertas.

### Costo relativo

Se define **D0** como el costo operativo total de esta alternativa conceptual.

~~~text
A = 1 D0
~~~

No representa precio de proveedor. Incluye cómputo, DB, almacenamiento, TLS, observabilidad, backup y tiempo operativo.

### Ventajas

- menor complejidad;
- coincide con ADR-004;
- elimina credenciales DB del cliente;
- permite retirar documentos sensibles de /uploads/** público;
- misma base y consistencia fuerte;
- camino reversible hacia réplica.

### Riesgos

- una sola API sigue siendo punto único de fallo;
- PostgreSQL sigue siendo dependencia crítica;
- la recuperación de API depende del reinicio/reemplazo de instancia.

## 3. Alternativa B — monolito redundante

~~~text
Android
  │ HTTPS
  ▼
Load balancer / TLS
  ├───────────────┐
  ▼               ▼
API replica 1   API replica 2
  └───────┬───────┘
          │ JDBC/TLS
          ▼
   PostgreSQL administrado
          │
          ├── backup/restore
          └── Object storage privado
~~~

### Características

- dos o más réplicas de la API;
- balanceador;
- almacenamiento de archivos externo/compartido;
- PostgreSQL administrado;
- health checks por réplica;
- misma estrategia de logs/métricas/trazas.

### Costo relativo

~~~text
B = 2–3 D0
~~~

La diferencia viene de:

- más instancias;
- balanceador;
- mayor volumen de telemetría;
- necesidad de almacenamiento compartido;
- despliegue y diagnóstico multi-instancia.

### Beneficio

Tolera la caída de una réplica de API y permite aumentar capacidad de aplicación sin extraer Mensajería.

### Límite

No resuelve una caída de PostgreSQL y no está justificada todavía por S11 como necesidad inmediata.

## 4. Comparación

| Dimensión | A. Monolito administrado | B. Monolito redundante |
| --- | --- | --- |
| Costo relativo | **1 D0** | **2–3 D0** |
| API SPOF | sí | no para una réplica |
| DB SPOF lógico | sí, salvo HA del proveedor | sí, salvo HA del proveedor |
| Complejidad | baja/media | media |
| Almacenamiento local | debe externalizarse | obligatorio externalizar |
| Observabilidad | una instancia | correlación multi-instancia |
| Escalabilidad API | vertical | horizontal |
| Coherencia con ADR-004 | total | total |
| Reversibilidad | alta | alta |

### Propuesta

**A** es la alternativa conceptual preferida para el primer despliegue público porque la evidencia actual no exige redundancia de API. **B** es el siguiente paso reversible si disponibilidad o capacidad de API lo justifican.

## 5. Métricas

### RED de API

| Métrica | Propósito | Umbral/uso inicial |
| --- | --- | --- |
| request rate | conocer demanda | tendencia/capacidad |
| 5xx rate | detectar fallo de servicio | **>5 % durante 5 min → crítica** |
| p95 HTTP | latencia percibida | **>500 ms durante 5 min → crítica** |
| p99 HTTP | cola larga | diagnóstico; sin SLO histórico todavía |

El umbral p95 de 500 ms reutiliza el SLO experimental ya usado en M2; no se inventa uno nuevo.

### Runtime/JVM

- heap usado / máximo;
- pausas y frecuencia de GC;
- CPU del proceso;
- threads activos.

### Persistencia

- conexiones Hikari activas/máximo;
- conexiones pendientes;
- timeouts de pool;
- latencia de operaciones DB;
- health de PostgreSQL.

### Seguridad

- intentos de login fallidos;
- respuestas 401;
- respuestas 403;
- rechazos de credencial de integración;
- uploads rechazados;
- 5xx por clase de error.

### Dominio

Las métricas de dominio sirven para contexto, no para alertar automáticamente sin línea base:

- mensajes enviados/min;
- chats creados/min;
- postulaciones/min.

## 6. Logs

Formato propuesto: estructurado JSON.

Campos mínimos:

~~~text
timestamp
level
service
instance
requestId
traceId
spanId
http.method
http.route
http.status
duration_ms
error.type
principal_role
~~~

### Nunca registrar

- Authorization/Bearer token;
- X-Integration-Token;
- contraseña;
- password_hash;
- cuerpo de chat;
- CV/documentos;
- números de documento completos.

Para eventos de seguridad puede registrarse un identificador interno/pseudónimo cuando sea necesario, evitando PII innecesaria.

## 7. Trazas

Trazas distribuidas no implican microservicios. Incluso con un monolito permiten correlacionar:

~~~text
HTTP request
  → Security filter
  → Controller
  → Service/Auth
  → JDBC
  → PostgreSQL
~~~

Spans sugeridos:

- `http.server`;
- `auth.resolve-session`;
- `chat.authorize-participant`;
- `chat.read-messages`;
- `db.query`;
- `file.store`.

Si se adopta la alternativa B, `instance` permite distinguir réplicas.

## 8. Alertas críticas

| Alerta | Condición inicial | Severidad | Acción |
| --- | --- | --- | --- |
| API no saludable | health DOWN >2 min | crítica | revisar instancia/deploy/logs |
| Error rate | 5xx >5 % durante 5 min | crítica | localizar endpoint/error/DB |
| Latencia | p95 >500 ms durante 5 min | crítica | comparar rate, pool y DB |
| Pool DB | uso >90 % durante 5 min o pending >0 sostenido | warning/crítica | identificar espera y queries |
| Login anómalo | >20 fallos/min por ventana/origen | warning seguridad | revisar abuso sin bloquear globalmente |
| 403 anómalos | incremento >5× respecto a baseline | warning seguridad | revisar acceso indebido/cliente roto |
| Storage | uso >85 % | warning | ampliar/depurar con retención |
| Backup | último backup fuera del RPO definido | crítica | restaurar pipeline de backup |
| Integración interna | rechazos de token repetidos | warning seguridad | rotación/configuración |

Los umbrales sin línea base histórica se etiquetan como **umbrales operativos iniciales** y deben recalibrarse; solo p95=500 ms viene de un criterio previo medido.

## 9. Fallos M6 → señal observable

| Fallo previo | Métrica | Log | Traza | Alerta |
| --- | --- | --- | --- | --- |
| F03 PostgreSQL no disponible | DB health/pool | JDBC connection error | API→DB | DB/API critical |
| F05 volumen lleno/corrupto | storage % / errores IO | write failure | file.store | storage >85 % / write failures |
| F06 API caída | health | startup/shutdown/exception | última traza disponible | health DOWN |
| F08 token inválido | 401 rate | auth rejected sin token | auth span | anomalía de 401 |
| F10 chat ajeno | 403 rate | authorization denied | chat.authorize | anomalía 403 |
| F11 offset profundo | p95 + DB duration | slow query | chat.read→DB | p95 >500 ms |
| F12 polling creciente | request rate + p95 | rate contextual | HTTP spans | capacidad/latencia |
| F14 config local publicada | config check/deploy gate | startup profile | — | bloquear deploy antes que alertar |

## 10. Fitness function en CI

La decisión de distribución sigue protegida por:

~~~text
scripts/check_distribution_decision.py
~~~

Regla:

- solo `db`, `api` y `k6` en Compose;
- no broker/Redis/Kafka/Pulsar/NATS;
- caché experimental OFF por defecto.

Umbral:

~~~text
0 violaciones
~~~

S14 añade evidencia de **CI rojo deliberado** en una rama/PR temporal y su corrección posterior. El registro está en:

`docs/architecture/evidencia-fitness-ci-s14.md`.

## 11. Relación con seguridad S13

El despliegue público debe cerrar las prioridades P0 de `10-seguridad.md`:

- HTTPS;
- secrets externos;
- seed demo apagado;
- documentos sensibles privados;
- logging/alerting de seguridad.

## 12. Conclusión

La propuesta no salta a Kubernetes ni microservicios. El primer despliegue conceptual recomendado es un **monolito modular administrado y observable**, con PostgreSQL administrado y almacenamiento privado. La redundancia de API queda como evolución reversible ante evidencia de disponibilidad o capacidad.

La decisión formal está en ADR-005.
