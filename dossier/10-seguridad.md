# Semana 13 — Seguridad, amenazas y controles arquitectónicos

> Evidencia principal exigida por la rúbrica: **matriz de riesgos de seguridad y controles arquitectónicos**.
>
> Alcance analizado: sistema UTrabajo **As-Is** del repositorio, incluyendo Android, API Spring Boot, PostgreSQL, almacenamiento de archivos, endpoints experimentales y CI. No se asume un despliegue público que todavía no existe.

## 1. Referencia y método

Se usa **OWASP Top 10:2025** como referencia de concienciación y clasificación, no como una lista para copiar mecánicamente.

Fuente oficial consultada: https://top10.owasp.org/2025/

Categorías 2025:

1. A01 — Broken Access Control.
2. A02 — Security Misconfiguration.
3. A03 — Software Supply Chain Failures.
4. A04 — Cryptographic Failures.
5. A05 — Injection.
6. A06 — Insecure Design.
7. A07 — Authentication Failures.
8. A08 — Software or Data Integrity Failures.
9. A09 — Security Logging and Alerting Failures.
10. A10 — Mishandling of Exceptional Conditions.

Cada amenaza se contrasta con **código real**, configuración y evidencia previa. Que una categoría aparezca en OWASP no significa que sea automáticamente prioritaria para UTrabajo.

### Escala

- **Probabilidad:** 1 rara, 2 baja, 3 posible, 4 probable, 5 muy probable.
- **Impacto:** 1 menor, 2 limitado, 3 relevante, 4 grave, 5 crítico.
- **Riesgo = Probabilidad × Impacto.**
- **P0:** 16–25.
- **P1:** 9–15.
- **P2:** 1–8.

La puntuación es una priorización de ingeniería del equipo, no una medición estadística de incidentes.

## 2. Activos que deben protegerse

| Activo | Sensibilidad / consecuencia |
| --- | --- |
| Credenciales y sesiones | toma de cuenta si se exponen |
| Mensajes de chats | confidencialidad entre estudiante y empresa |
| CV y documentos personales | datos personales/documentales |
| Documentos empresariales | RUT, Cámara de Comercio, representante |
| Ofertas y postulaciones | integridad del proceso de contratación |
| Base PostgreSQL | concentra usuarios, chats, ofertas, postulaciones y sesiones |
| Configuración/secrets | acceso a DB e integración interna |
| Pipeline CI y dependencias | integridad del software entregado |
| Evidencia experimental | trazabilidad académica y decisiones arquitectónicas |

## 3. Límites de confianza

~~~text
┌──────────────────────┐
│ Android / dispositivo│
└──────────┬───────────┘
           │ HTTP/Bearer hoy en local
=========== TB-1: cliente → API =================
           ▼
┌───────────────────────────────────────────────┐
│ API Spring Boot                               │
│ SecurityConfig → TokenAuthenticationFilter    │
│ Controllers → Services/Auth                   │
└──────┬──────────────────────┬─────────────────┘
       │ JDBC                 │ archivos
======= TB-2: API → DB =====  === TB-3: API → storage ===
       ▼                      ▼
┌───────────────┐       ┌───────────────────┐
│ PostgreSQL 16 │       │ volumen uploads   │
└───────────────┘       └─────────┬─────────┘
                                  │ /uploads/**
                         ========== TB-4 ==========
                                  ▼
                           consumidor HTTP

CI / dependencias / imágenes
=========== TB-5: supply chain ===================

/internal/integration/**
=========== TB-6: interfaz experimental =========
credencial X-Integration-Token
~~~

El diagrama fuente está en `docs/security/trust-boundaries-s13.puml`.

## 4. Controles existentes verificados

| Control | Evidencia real |
| --- | --- |
| Contraseñas con BCrypt costo 12 | `SecurityConfig.passwordEncoder()` |
| Token de sesión aleatorio | `SecureRandom` de 32 bytes en `AuthService` |
| Token almacenado como hash SHA-256 | `AuthService.tokenHash` y `auth_session.token_hash` |
| Sesión con expiración | `expires_at`; default 168 h |
| API stateless | `SessionCreationPolicy.STATELESS` |
| Autorización por participante | `requireChatParticipant` antes de leer/escribir mensajes |
| Roles y propiedad de recursos | `requireRole`, filtros por `company_id/student_id` |
| SQL parametrizado | `JdbcClient.param(...)` en consultas del backend |
| Columna dinámica restringida | allowlist `photo_path/cv_path` en `updateStoredFile` |
| Tipos de archivo limitados | allowlist MIME en `FileStorageService` |
| Ruta de almacenamiento normalizada | `directory.startsWith(root)` |
| Tamaño multipart limitado | 10 MB archivo / 20 MB request |
| Errores conocidos traducidos | `ApiExceptionHandler` |
| Restricciones DB | PK/FK/UNIQUE/CHECK y Flyway |
| Frontera controller → persistencia | ArchUnit + `check_architecture.py` en CI |
| Topología distribuida no accidental | `check_distribution_decision.py` en CI |

## 5. Matriz de amenazas priorizadas

| ID | OWASP | Amenaza aplicable | P | I | Riesgo | Estado/control actual | Control arquitectónico propuesto |
| --- | --- | --- | ---: | ---: | ---: | --- | --- |
| **T01** | A01 | Usuario intenta leer/escribir un chat ajeno | 4 | 5 | **20 P0** | `requireChatParticipant` + prueba negativa existente | conservar autorización dentro del caso de uso; nunca confiar en `chatId` del cliente |
| **T02** | A01/A02 | CV/documentos empresariales quedan accesibles por `/uploads/**` sin autenticación | 4 | 5 | **20 P0** | ruta pública; nombres UUID reducen descubrimiento pero no son autorización | retirar documentos sensibles de la ruta pública; descarga autenticada o URL firmada; avatar puede tener política distinta |
| **T03** | A02/A04 | Configuración local se publica como si fuera producción: HTTP, credenciales demo, seed e integration token por defecto | 4 | 5 | **20 P0** si se publica | alcance local declarado; F14 ya lo identifica | perfil de producción separado: HTTPS, secrets externos, `SEED_DEMO=false`, token sin default, CORS/reverse proxy restrictivo |
| **T04** | A09 | Incidentes de seguridad no se detectan por falta de logs/alertas específicas | 4 | 4 | **16 P0** | health/info; no existe plan integral de alertas de seguridad | S14 define eventos auditables, métricas y alertas para 401/403/5xx/login fallido |
| **T05** | A07 | Fuerza bruta / credential stuffing contra login | 3 | 4 | **12 P1** | mensaje de error no revela si el correo existe; BCrypt | rate limiting/backoff por identidad+origen; alerta de intentos fallidos; no bloquear globalmente el sistema |
| **T06** | A04/A07 | Robo/reutilización del Bearer token durante transporte o por sesión larga | 3 | 4 | **12 P1** fuera del entorno local | token aleatorio, hash en DB y expiración de 168 h | HTTPS obligatorio fuera de local; política de duración/revocación basada en riesgo; evitar registrar tokens |
| **T07** | A06/A02 | Archivo permitido por MIME pero con contenido malicioso o formato engañoso | 3 | 4 | **12 P1** | allowlist MIME, UUID y límites de tamaño | validar firma/magic bytes; escaneo al desplegar públicamente; servir con Content-Disposition/Content-Type seguros |
| **T08** | A03/A08 | Dependencia, Action o imagen comprometida altera build/runtime | 2 | 5 | **10 P1** | versiones de librerías declaradas y CI reproducible | escaneo de dependencias, actualización controlada, pinning cuando proceda y revisión de imágenes/base |
| **T09** | A05 | Inyección SQL a través de entrada controlada por usuario | 2 | 5 | **10 P1** por impacto, probabilidad reducida | consultas parametrizadas; allowlist para columna dinámica | mantener SQL fuera de controllers, parámetros obligatorios y pruebas de entrada |
| **T10** | A10 | Excepción no prevista produce 5xx repetido, oculta causa o expone detalle | 3 | 3 | **9 P1** | handler para excepciones conocidas; Spring maneja resto | error ID/traceId, log interno sin exponer stack al cliente, alerta por tasa 5xx |
| **T11** | A08 | Manipulación accidental/no revisada de migraciones, evidencia o artefactos | 2 | 4 | **8 P2** | Git, Flyway, CI, SHA-256 de evidencia | revisión de PR, backups y política de migración forward-only |
| **T12** | A06 | Polling de chat amplifica consumo y puede convertirse en abuso/DoS funcional | 3 | 2 | **6 P2** bajo carga medida | límites de página y evidencia S11 sin saturación hasta 200 VU | medir tráfico sostenido, rate limit/backoff; reconsiderar transporte solo con evidencia |

## 6. OWASP Top 10: aplicabilidad completa

| Categoría OWASP 2025 | Aplicabilidad UTrabajo | Tratamiento |
| --- | --- | --- |
| A01 Broken Access Control | **Alta** | T01/T02; prioridad principal |
| A02 Security Misconfiguration | **Alta si sale de local** | T02/T03 |
| A03 Software Supply Chain Failures | **Media** | T08 |
| A04 Cryptographic Failures | **Media; alta en Internet sin TLS** | T03/T06 |
| A05 Injection | **Aplicable pero mitigada** | T09; consultas parametrizadas |
| A06 Insecure Design | **Aplicable** | T07/T12 y revisión de límites |
| A07 Authentication Failures | **Aplicable** | T05/T06 |
| A08 Software or Data Integrity Failures | **Aplicable con riesgo menor actual** | T08/T11 |
| A09 Security Logging and Alerting Failures | **Alta** | T04; se desarrolla en S14 |
| A10 Mishandling of Exceptional Conditions | **Aplicable** | T10 |

## 7. Amenazas genéricas propuestas por IA y descartadas/modificadas

| Propuesta genérica | Decisión | Justificación |
| --- | --- | --- |
| “SSRF crítico” | **Descartada actualmente** | el backend no recibe URLs arbitrarias para realizar fetch del lado servidor |
| “XXE” | **Descartada actualmente** | no existe procesamiento XML en el flujo analizado |
| “XSS de plantilla server-side” | **Descartada como amenaza principal** | el backend entrega JSON y el cliente principal es Android Compose; revisar UI si se incorpora WebView/HTML |
| “CSRF crítico porque CSRF está deshabilitado” | **Modificada** | la API actual es stateless con Bearer header, no autenticación automática por cookie; reabrir si cambia el mecanismo |
| “SQL injection generalizada” | **Modificada** | la categoría aplica, pero el código usa parámetros; se conserva como riesgo de regresión, no como vulnerabilidad demostrada |
| “Microservicios para aislar seguridad” | **Rechazada** | aumenta secretos/red/superficie sin evidencia de que la distribución resuelva los riesgos prioritarios |
| “Cifrar toda columna de PostgreSQL” | **Rechazada como control genérico** | no se adopta sin modelo de claves y clasificación de datos; TLS/secrets/autorización tienen prioridad anterior |
| “WAF obligatorio ya” | **Diferida** | no existe despliegue público; primero se define el borde de Internet de S14 |

Registro ampliado: `docs/security/auditoria-amenazas-ia-s13.md`.

## 8. Checklist de seguridad elevado al sistema completo

| Área | Estado actual | Acción S13/S14 |
| --- | --- | --- |
| Identidad | BCrypt + token aleatorio/hash | analizar rate limit y duración |
| Autorización | roles + participante del chat | mantener pruebas negativas; revisar archivos |
| Entrada/SQL | validation + JdbcClient parametrizado | conservar allowlists |
| Archivos | tipo/tamaño/ruta controlados | **cerrar acceso público a documentos sensibles antes de Internet** |
| Sesión | expiración y logout | HTTPS y política de revocación |
| Secretos | valores locales/demo | separar perfil de producción |
| Transporte | HTTP local | HTTPS obligatorio en despliegue público |
| Base | restricciones + Flyway | backups/restauración y secret management |
| Errores | handler conocido | traceId/log/alerta para 5xx |
| Logs/alertas | incompleto | plan de observabilidad S14 |
| Dependencias/CI | CI + versiones | escaneo/pinning progresivo |
| Disponibilidad/abuso | límite/paginación; S11 medido | backoff/rate limit solo ante señal |
| Evidencia | Git + hashes + CI | conservar trazabilidad |

## 9. Controles arquitectónicos y reglas

### C-S13-01 — autorización de recursos

Toda operación de chat debe resolver el principal en servidor y verificar participación antes de acceder al historial.

### C-S13-02 — documentos sensibles no públicos

Un despliegue público no puede exponer CV, RUT, Cámara de Comercio ni documentos del representante mediante una ruta anónima permanente.

### C-S13-03 — perfil local no equivale a producción

No se puede promocionar la configuración local sin sustituir HTTP, credenciales demo, seed y secrets por controles de producción.

### C-S13-04 — persistencia parametrizada

Controllers no contienen SQL/JDBC y la persistencia dinámica debe usar parámetros/allowlists.

### C-S13-05 — eventos de seguridad observables

Fallos de autenticación, denegaciones de autorización y 5xx deben producir señales observables sin registrar secretos o contenido sensible.

### C-S13-06 — re-decisión explícita

Si un control cambia topología o introduce broker/cache/servicio, debe reabrirse el ADR correspondiente y pasar las fitness functions.

## 10. Riesgo residual y conclusión

Los controles actuales son adecuados para **un entorno académico/local con datos sintéticos**, especialmente en autenticación, autorización de chat, SQL parametrizado y aislamiento de capas. No son suficientes para afirmar seguridad de producción en Internet.

Las prioridades antes de un despliegue público son:

1. proteger descargas/documentos sensibles;
2. separar configuración de producción y exigir HTTPS/secrets externos;
3. incorporar logging/alerting de seguridad;
4. proteger autenticación contra abuso;
5. añadir controles de supply chain progresivos.

La conclusión no es “OWASP cumplido”. La conclusión es que el repositorio tiene controles verificables y un conjunto priorizado de riesgos residuales que alimenta S14.
