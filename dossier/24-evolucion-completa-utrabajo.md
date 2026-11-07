# 24 — Evolución completa de UTrabajo: desde el sistema inicial hasta el estado actual

> **Proyecto:** UTrabajo  
> **Curso:** Arquitectura de Software  
> **Categoría trabajada:** Mensajería y mesa de ayuda  
> **Propósito:** contar de forma auditada y navegable **cómo empezó UTrabajo, qué cambió semana a semana, qué decisiones se tomaron, qué errores aparecieron, cómo se corrigieron y en qué estado quedó el proyecto al cierre documentado**.
>
> **Corte histórico que este documento narra:** estado de `main` después del merge de PR #33, commit `5489e21fdcd88c9d4639bfaf5fb9d66bdf72d7eb`, con **UTrabajo CI #77 = success**.  
> Este SHA se usa como **corte histórico de referencia**; este mismo documento puede recibir commits posteriores de documentación y por eso no debe interpretarse como un SHA autorreferencial del archivo.

---

# 1. La historia completa en una sola frase

UTrabajo **no nació en la materia**: llegó al curso como una aplicación Android existente que conectaba estudiantes y empresas; a lo largo del semestre se convirtió en un sistema arquitectónicamente documentado, medido, trazable y protegido mediante C4, prerregistros, experimentos reproducibles, ADR, fitness functions, análisis de seguridad, diseño de despliegue y observabilidad, terminando con un frontend renovado y un dossier final que enlaza código, errores, recuperaciones, PR, CI y resultados primarios.

---

# 2. Antes y después — vista ejecutiva

| Aspecto | Al inicio del curso | Estado al corte histórico PR #33 |
| --- | --- | --- |
| Producto | app Android existente | app Android funcional con experiencia visual renovada |
| Usuarios | estudiantes y empresas | mismos actores, flujos y responsabilidades formalizados |
| Backend | API Spring Boot/Kotlin | API documentada por C4, límites y ADR |
| Persistencia | PostgreSQL | PostgreSQL 16.14 + Flyway + índices + mediciones |
| Arquitectura | cliente–servidor existente, aún no razonada formalmente | monolito modular defendido con evidencia |
| Mensajería | funcionalidad del producto | fenómeno arquitectónico principal medido y localizado |
| Calidad | intuiciones | escenarios verificables con p95, seguridad, integridad y modificabilidad |
| Rendimiento | sin línea base formal | S4, S7, Spike 1, S11 y caché con resultados versionados |
| Documentación | README/código | dossier C4 + ADR-001…006 + resúmenes + informe final |
| Decisiones | implícitas | decisiones explícitas, reversibles y enlazadas a evidencia |
| Integración | dependencia implícita Mensajería–Ofertas | bounded contexts + contrato mínimo + Spike 1 + ADR-003 |
| Escalabilidad | posibilidad abstracta | carga 10→200 VU y comparación de distribución |
| Microservicios | opción posible | conscientemente diferidos por falta de evidencia |
| Resiliencia | sin análisis estructurado | matriz de fallos + matriz de tácticas + prueba de caché |
| Seguridad | controles en código | threat model, OWASP, trust boundaries y riesgos residuales |
| Operación | local | diseño conceptual de despliegue administrado + observabilidad |
| CI | pruebas | pruebas + ArchUnit + fitness ADR-002/ADR-004 |
| Evidencia de fallo | no centralizada | errores, diagnósticos, recuperación, CI rojo/verde y hashes |
| Estado final | sistema funcional | sistema funcional + evidencia arquitectónica + presentación final navegable |

---

# 3. Punto cero — UTrabajo antes de la arquitectura del curso

## 3.1 UTrabajo ya existía

El primer documento del dossier deja explícito que UTrabajo era **un proyecto Android existente del equipo**, no un proyecto generado desde cero para la asignatura.

Evidencia:

- [`dossier/01-contexto-sistema.md`](01-contexto-sistema.md)

Descripción funcional inicial:

> UTrabajo conecta estudiantes y empresas mediante ofertas, postulaciones y conversaciones.

Actores:

- estudiante;
- empresa;
- equipo de desarrollo;
- profesor/auditor.

Arquitectura material inicial:

~~~text
Android (Kotlin + Compose)
        │ HTTP/JSON + Bearer
        ▼
API REST (Spring Boot + Kotlin)
        │ JDBC + Flyway
        ▼
PostgreSQL 16
~~~

Esa estructura básica **se conserva durante todo el semestre**. El trabajo arquitectónico posterior no reemplazó arbitrariamente el producto: lo hizo explícito, medible y controlable.

## 3.2 Qué todavía no existía formalmente

Al comienzo no estaban consolidados como evidencia del curso:

- escenario de calidad falsable;
- línea base auditada;
- C4 completo;
- comparación de estilos;
- ADR;
- bounded contexts;
- contrato de integración interno;
- análisis de carga aumentado;
- fitness functions completas;
- threat model;
- diseño de observabilidad;
- informe maestro de cierre.

El curso convirtió decisiones implícitas en decisiones **explicables y verificables**.

---

# 4. Primera exploración — antes de fijar Mensajería como fenómeno principal

Antes de consolidar la categoría definitiva se conservó una exploración histórica sobre ofertas.

Evidencias:

- [`docs/experiment/00-decision-gates.md`](../docs/experiment/00-decision-gates.md)
- [`01-preregistration.md`](../docs/experiment/01-preregistration.md)
- [`02-protocol.md`](../docs/experiment/02-protocol.md)
- [`03-evidence-matrix.md`](../docs/experiment/03-evidence-matrix.md)

El primer prerregistro histórico estudió:

~~~text
GET /api/jobs?limit=100&offset=0
10.000 ofertas sintéticas
90 % activas
10 solicitudes concurrentes
p95 <= 500 ms
~~~

Commit de sellado de esa hipótesis:

~~~text
03c61d0320cbf8658c0500857ad92154ee2d9da1
~~~

Este experimento **no fue borrado** cuando la categoría pasó a Mensajería y mesa de ayuda.

La decisión correcta fue conservarlo como antecedente y crear una medición nueva alineada con Mensajería.

Esto establece desde el inicio una práctica que se repite durante todo el curso:

> **no reescribir el pasado para hacer que coincida con la decisión posterior.**

---

# 5. Semanas 1–4 — convertir el sistema existente en un sistema medible

## Semana 1 — adoptar, delimitar y poder ejecutar

### Pregunta

> ¿Qué sistema tenemos realmente y cuál es su frontera?

Se formalizó:

- contexto;
- actores;
- tecnología;
- restricciones;
- topología local;
- límites del alcance.

Evidencia principal:

[`01-contexto-sistema.md`](01-contexto-sistema.md)

Restricciones declaradas:

- entorno local;
- herramientas gratuitas;
- PostgreSQL 16;
- datos sintéticos;
- Android existente;
- API separada de la base;
- sin nube obligatoria.

### Primer PR grande del dossier

[PR #1 — Dossier S1–S4](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/1)

Merge:

~~~text
1e1d8da3f39bd01a3e01edae63bc2ca6e60055f7
~~~

Ese PR consolidó el primer bloque evaluable del curso.

---

## Semana 2 — stakeholders, drivers y riesgos

Se dejó de pensar únicamente en “funciones” y se priorizó **qué calidad debía proteger la arquitectura**.

Evidencia:

[`02-stakeholders-drivers.md`](02-stakeholders-drivers.md)

Drivers priorizados:

1. rendimiento al abrir conversaciones extremas;
2. confidencialidad;
3. orden e integridad;
4. modificabilidad;
5. reproducibilidad.

También se hizo crítica temprana de recomendaciones de IA.

Ejemplos:

| Sugerencia | Juicio |
| --- | --- |
| conversación enorme puede agotar memoria/latencia | válida |
| distribución uniforme ocultaría extremos | válida |
| usuario puede leer chat ajeno | válida |
| “la base puede caerse” | demasiado genérica |
| Kubernetes multirregión para semana 4 | irrelevante |
| Android usa contraseña PostgreSQL | falsa |
| usar datos reales sería mejor | descartada por privacidad |

La IA comenzó a usarse como **fuente de hipótesis**, no como autoridad.

---

## Semana 3 — calidad convertida en escenarios verificables

Evidencia:

[`03-atributos-calidad.md`](03-atributos-calidad.md)

Atributos priorizados:

| Orden | Atributo | Puntaje |
| ---: | --- | ---: |
| 1 | Rendimiento | 25 |
| 2 | Seguridad/confidencialidad | 20 |
| 3 | Confiabilidad/integridad | 16 |
| 4 | Modificabilidad | 12 |
| 5 | Disponibilidad | 9 |

Escenario principal:

~~~text
MSG-PERF-01
conversación = 100.000 mensajes
consulta = últimos 50
10 VU
40 solicitudes/corrida
4 corridas
corrida 1 = calentamiento
p95 <= 500 ms
0 fallos
~~~

El salto conceptual fue importante:

~~~text
“el chat debe ser rápido”
        ↓
100.000 mensajes + 50 recientes + p95 + carga + umbral
~~~

---

## Semana 4 — primera línea base real de Mensajería

Prerregistro:

[`04-escenarios-calidad.md`](04-escenarios-calidad.md)

Commit de sellado:

~~~text
e7e84d98b866cc0455fff8e45fb6bf2bdd00b983
~~~

Commit medido:

~~~text
c7c0edd66f49ed28fb694cf9c1970bab03c4b640
~~~

Condiciones:

- Acer Predator PH16-71;
- i9-13900HX;
- 95,7 GiB RAM;
- 32 CPU lógicas;
- conectado a corriente;
- Docker Desktop;
- PostgreSQL 16.14;
- k6 0.54.0;
- API + DB + generador en el mismo equipo.

Semilla registrada:

~~~text
1.000 conversaciones
289.000 mensajes

900 × 10 mensajes
 90 × 1.000
  9 × 10.000
  1 × 100.000
~~~

Resultados:

| Corrida | Estado | p95 |
| ---: | --- | ---: |
| 1 | warm-up | 83,553 ms |
| 2 | válida | 9,451 ms |
| 3 | válida | 8,265 ms |
| 4 | válida | 9,109 ms |

Mediana válida:

~~~text
9,109 ms
~~~

Umbral preregistrado:

~~~text
500 ms
~~~

Checks:

~~~text
100 %
~~~

Fallos HTTP:

~~~text
0 %
~~~

Conclusión legítima:

> H1 quedó respaldada **bajo las condiciones locales registradas**.

No se afirmó que ese p95 fuera un SLO de Internet.

Fuente primaria:

[`experimentos/medicion-escenario-01/resultados/`](../experimentos/medicion-escenario-01/resultados/)

Checklist de cierre:

[`05-checklist-semanas-1-4.md`](05-checklist-semanas-1-4.md)

---

# 6. Semanas 5–6 — dejar de describir “una app” y modelar una arquitectura real

El siguiente salto fue C4.

## C1 — UTrabajo como caja negra

[`05-c4-contexto.md`](05-c4-contexto.md)

Actores y objetivos quedan separados de tecnología.

## C2 — contenedores

[`06-c4-contenedores.md`](06-c4-contenedores.md)

Se distinguen:

- Android;
- API REST;
- PostgreSQL;
- almacenamiento local.

También se aclara que JDBC y Flyway son tecnologías/mecanismos, no contenedores desplegables.

## C3 backend

[`07-c4-componentes-backend.md`](07-c4-componentes-backend.md)

Trayecto medido:

~~~text
k6
 → TokenAuthenticationFilter / SecurityConfig
 → ChatController
 → UTrabajoService.requireChatParticipant
 → UTrabajoService.messages
 → JdbcClient
 → PostgreSQL
 → JSON
~~~

## C3 Android

[`08-c4-componentes-android.md`](08-c4-componentes-android.md)

Trayecto cliente:

~~~text
Compose
 → UTrabajoRepository
 → ApiService / Retrofit
 → API REST
~~~

La vista deja explícito:

> Android no accede directamente a PostgreSQL.

---

# 7. Walking skeleton — probar el recorrido vertical real

Se automatizó un recorrido mínimo que une:

- Docker;
- PostgreSQL;
- Flyway;
- health de API;
- autenticación;
- oferta;
- chat;
- mensaje;
- persistencia SQL;
- rechazo sin token.

Evidencias:

- [`docs/architecture/walking-skeleton.md`](../docs/architecture/walking-skeleton.md)
- [`failure-modes-walking-skeleton.md`](../docs/architecture/failure-modes-walking-skeleton.md)
- [`scripts/run-walking-skeleton.ps1`](../scripts/run-walking-skeleton.ps1)

El sistema empezó a tener no solo arquitectura dibujada, sino una **ruta ejecutable de extremo a extremo**.

Los modos de fallo documentaron, entre otros:

- Docker detenido;
- puertos ocupados;
- PostgreSQL caído;
- Flyway inválido;
- volumen;
- API caída;
- semilla;
- token;
- URL Android;
- acceso a chat ajeno;
- offset profundo;
- polling;
- configuración local publicada en Internet.

Ese catálogo más tarde alimentó directamente la observabilidad S14.

---

# 8. Semana 7 — localizar dónde duele realmente

S4 había medido la operación HTTP completa.

S7 preguntó:

> ¿dónde está el costo dentro de PostgreSQL?

Instrumento:

[`experimentos/localizacion-s7/README.md`](../experimentos/localizacion-s7/README.md)

Se comparó:

- autorización;
- página reciente;
- offset profundo;
- cursor equivalente.

Resultado observado:

| Operación | Tiempo |
| --- | ---: |
| autorización participante | 0,081 ms |
| OFFSET 0 | 0,120 ms |
| OFFSET 50000 | 102,050 ms |
| cursor equivalente | 0,123 ms |

El offset profundo costó:

~~~text
829,675×
~~~

el cursor equivalente en esa observación.

Lo importante no fue solo el número.

La evidencia mostró que:

- página reciente usa índice;
- offset profundo recorre mucho más trabajo;
- cursor conserva acceso indexado;
- esto justifica una evolución de paginación;
- **no** justifica automáticamente microservicios.

PR:

[PR #6 — localizar costo y comparar alternativas](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/6)

---

# 9. Semanas 7–8 — primera gran decisión de estilo

Documento canónico:

[`08-decision-estilo-arquitectonico.md`](08-decision-estilo-arquitectonico.md)

Alternativas comparadas:

1. monolito sin límites;
2. monolito modular con regla;
3. microservicio + broker.

Decisión:

> **mantener monolito modular.**

La razón fue evidencia, no preferencia:

- S4 ya cumplía el escenario;
- S7 localizó un problema de paginación, no de distribución;
- seguridad es más fácil de centralizar;
- un despliegue es más reproducible;
- microservicio agrega red, secretos y operación sin una presión que lo pague.

PR de decisión:

[PR #11](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/11)

Ajustes de trazabilidad:

[PR #12](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/12)

---

# 10. ADR-001, ADR-002 y ADR-006 — formalizar lo aprendido

## ADR-001

[`ADR-001-limites-modulo-mensajeria.md`](../docs/adr/ADR-001-limites-modulo-mensajeria.md)

Decisión:

> Mensajería permanece dentro del monolito modular.

## ADR-002

[`ADR-002-limites-modulos-dependencias.md`](../docs/adr/ADR-002-limites-modulos-dependencias.md)

Regla central:

~~~text
controller
    ↓
service/auth
    ↓
JDBC
~~~

Prohibición:

> controller no depende directamente de JDBC/SQL.

## ADR-006

[`ADR-006-paginacion-historial-mensajes.md`](../docs/adr/ADR-006-paginacion-historial-mensajes.md)

Decisión:

- conservar offset para compatibilidad/página reciente;
- no prometer rendimiento profundo con offset;
- introducir cursor cuando exista necesidad de historial profundo.

---

# 11. Semana 8 — convertir arquitectura en una restricción ejecutable

La arquitectura dejó de depender exclusivamente de revisión humana.

Evidencia:

- [`scripts/check_architecture.py`](../scripts/check_architecture.py)
- [`DatabaseIsolationArchitectureTests.kt`](../backend/src/test/kotlin/com/tab/utrabajo/api/architecture/DatabaseIsolationArchitectureTests.kt)
- [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

Protecciones:

- controladores sin JDBC/SQL;
- persistencia restringida a paquetes permitidos;
- paquetes superiores sin ciclos.

PR:

[PR #10 — proteger límites del backend en CI](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/10)

### Pendiente honesto

La prueba negativa deliberada de ADR-002 quedó documentada como pendiente:

[`docs/architecture/evidencia-restriccion-ci.md`](../docs/architecture/evidencia-restriccion-ci.md)

Por tanto:

~~~text
regla en CI = sí
ArchUnit = sí
demostración roja histórica ADR-002 = pendiente
~~~

Eso se mantiene como pendiente real y no se inventa.

---

# 12. Cierre S5–S8 — convertir evidencia técnica en exposición reproducible

Guía:

[`15-guia-exposicion-integral-s5-s8.md`](15-guia-exposicion-integral-s5-s8.md)

PR:

[PR #13 — guía integral de exposición](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/13)

Se archivaron capturas reales en:

[`dossier/evidencias-exposicion/`](evidencias-exposicion/)

Ejemplos:

- puertos ocupados;
- walking skeleton;
- fallo controlado;
- recuperación;
- línea base;
- S7;
- restricción ADR-002.

Este punto marca una transición:

> el proyecto ya no solo podía ejecutarse; también podía **defenderse con evidencia navegable**.

---

# 13. Semana 9 — pasar de capas técnicas a límites de dominio

Hasta aquí la arquitectura estaba principalmente expresada en capas.

S9 introdujo **bounded contexts**.

Evidencias:

- [`docs/dominio/subdominios.md`](../docs/dominio/subdominios.md)
- [`responsabilidades-contextos.md`](../docs/dominio/responsabilidades-contextos.md)
- [`context-map.md`](../docs/dominio/context-map.md)

Contextos:

1. Identidad y Acceso;
2. Perfiles;
3. Ofertas;
4. Postulaciones;
5. Mensajería.

Importante:

> estos contextos son fronteras lógicas; no se convirtieron automáticamente en microservicios.

Frontera seleccionada:

~~~text
Mensajería
   │ consume
   ▼
Ofertas

datos mínimos:
jobId + companyId
~~~

PR:

[PR #16 — dominio, integración y preregistro S9](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/16)

---

# 14. Semana 9 — síncrono vs asíncrono

Se compararon tres caminos:

1. SQL/acceso compartido;
2. contrato síncrono;
3. eventos + proyección.

Documento:

[`docs/integracion/sincrono-vs-asincrono.md`](../docs/integracion/sincrono-vs-asincrono.md)

Contrato experimental:

[`contrato-api.yaml`](../docs/integracion/contrato-api.yaml)

~~~http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
~~~

Payload final:

~~~json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
~~~

Antes de medir se eliminaron del borrador:

- `active`;
- `title`.

Motivo:

> no eran datos mínimos necesarios para crear la conversación.

La corrección quedó registrada **antes de medir**:

[`01-enmienda-previa-a-medicion.md`](../experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md)

---

# 15. Semana 10 — Spike 1: preguntar antes de decidir

Prerregistro:

[`00-preregistro.md`](../experimentos/spike-01-integracion/00-preregistro.md)

Condiciones:

~~~text
10 VU
40 solicitudes
4 corridas
corrida 1 warm-up
p95 <= 500 ms
401 token inválido/ausente
404 oferta inexistente
0 fallos inesperados
~~~

Temporalidad:

~~~text
804d2d2  preregistro
de83109  enmienda
4b1c43d  implementación
1e8bc87  commit medido
b2d59f1  resultados
4072eba  veredicto
9a62b1d  cierre/ADR
~~~

Resultado:

| Corrida | Uso | p95 |
| ---: | --- | ---: |
| 1 | warm-up | 5,779 ms |
| 2 | válida | 4,781 ms |
| 3 | válida | 4,360 ms |
| 4 | válida | 4,710 ms |

Checks:

~~~text
100 %
~~~

Errores inesperados:

~~~text
0 %
~~~

Fuente primaria:

[`resultado.json`](../experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json)

Veredicto:

[`veredicto.md`](../experimentos/spike-01-integracion/veredicto.md)

~~~text
experimento = válido
hipótesis = respaldada
alcance = ajustado antes de medir
~~~

PR:

[PR #17 — Spike 1](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/17)

---

# 16. ADR-003 — una frontera explícita sin auto-microservicio

[`ADR-003-integracion-contextos.md`](../docs/adr/ADR-003-integracion-contextos.md)

Decisión propuesta:

> interacción síncrona mínima de Mensajería hacia Ofertas.

Matiz clave:

- el spike usó HTTP para hacer observable el contrato;
- mientras ambos contextos estén en el mismo proceso, la implementación preferida es un **puerto/interfaz interna**;
- no se propone que el monolito se llame a sí mismo por red.

No se adoptaron:

- Kafka;
- RabbitMQ;
- CQRS completo;
- Event Sourcing;
- proyección replicada.

No porque sean “malos”, sino porque la evidencia no mostraba una necesidad que pagara su complejidad.

---

# 17. Errores reales del Spike 1 — y por qué importan

La historia del proyecto no oculta fallos de preparación.

Directorio:

[`experimentos/spike-01-integracion/evidencias/`](../experimentos/spike-01-integracion/evidencias/)

Secuencia documentada:

~~~text
Docker no disponible
        ↓
Android SDK no configurado
        ↓
stacktrace
        ↓
build fallido
        ↓
lint fallido
        ↓
lint/build corregidos
        ↓
falso NativeCommandError por stderr de Compose
        ↓
runner corregido
        ↓
Spike 1 exitoso
        ↓
resumen de corridas
~~~

Capturas:

- [Docker no disponible](../experimentos/spike-01-integracion/evidencias/02-docker-no-disponible-backend-exitoso.png)
- [SDK no configurado](../experimentos/spike-01-integracion/evidencias/03-android-sdk-no-configurado.png)
- [stacktrace](../experimentos/spike-01-integracion/evidencias/04-android-sdk-stacktrace.png)
- [build fallido](../experimentos/spike-01-integracion/evidencias/07-android-build-fallido-final.png)
- [lint/build exitosos](../experimentos/spike-01-integracion/evidencias/08-android-lint-y-build-exitosos.png)
- [falso error del runner](../experimentos/spike-01-integracion/evidencias/09-spike-falso-error-stderr-compose.png)
- [ejecución exitosa](../experimentos/spike-01-integracion/evidencias/10-spike-ejecucion-exitosa.png)
- [resumen](../experimentos/spike-01-integracion/evidencias/11-spike-resumen-corridas.png)

La enseñanza metodológica:

> una preparación fallida no se mezcló con las corridas válidas.

---

# 18. Semana 11 — aumentar presión sin cambiar de pregunta

S11 retomó el escenario de Mensajería y aumentó concurrencia.

PR:

[PR #20 — crecimiento de Mensajería](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20)

Escalones:

~~~text
10 VU
50 VU
100 VU
150 VU
200 VU
~~~

p95 medianos:

| VU | p95 |
| ---: | ---: |
| 10 | 8,963 ms |
| 50 | 18,768 ms |
| 100 | 39,258 ms |
| 150 | 41,060 ms |
| 200 | 58,235 ms |

Fuente:

[`resultado.json`](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json)

Resultado conceptual:

~~~text
latencia ↑
throughput ↑
errores HTTP = 0
páginas válidas = 100 %
p95 << 500 ms
~~~

Conclusión permitida:

> se observó crecimiento de latencia, pero **no degradación sostenida ni cuello de botella causal demostrado** hasta 200 VU.

No se afirmó:

> PostgreSQL se saturó.

---

# 19. Semana 11 — fallos hipotéticos vs fallos observados

Matriz:

[`03-matriz-fallos.md`](../experimentos/crecimiento-mensajeria/03-matriz-fallos.md)

Se evaluaron hipótesis como:

- pool JDBC;
- PostgreSQL;
- API;
- k6;
- máquina compartida;
- errores HTTP;
- respuestas inválidas.

Regla:

> **una métrica que crece no identifica automáticamente el componente culpable.**

Esto evitó confundir correlación con causalidad.

---

# 20. Semana 11 — resiliencia basada en señal, no checklist

Matriz:

[`04-matriz-resiliencia.md`](../experimentos/crecimiento-mensajeria/04-matriz-resiliencia.md)

Se consideraron:

- caché;
- timeout;
- retry;
- circuit breaker;
- bulkhead;
- pool JDBC;
- escalado;
- SSE/WebSocket.

Como no hubo errores transitorios:

- retry no se presentó como solución demostrada;
- circuit breaker no se justificó;
- timeout no resolvía el fenómeno observado.

La táctica elegida para medir fue **caché local** porque existían lecturas repetidas.

---

# 21. Caché antes/después — una mejora parcial que NO se convirtió en dogma

Configuración:

~~~text
TTL = 1 s
máximo = 1.000
solo offset=0
autorización antes de cache
invalidación al enviar
default = OFF
~~~

Resultados:

| Métrica | Antes | Después | Cambio |
| --- | ---: | ---: | ---: |
| p95 | 58,235 ms | 55,377 ms | -4,908 % |
| throughput | 2.303,017 req/s | 2.260,038 req/s | -1,866 % |
| errores | 0 | 0 | = |
| páginas válidas | 100 % | 100 % | = |

Hits:

~~~text
3.196
~~~

Veredicto:

~~~text
partially_supported
~~~

Documento:

[`06-resultado-tactica-cache.md`](../experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md)

Decisión:

> **no habilitar caché por defecto.**

Este punto es importante para la historia porque demuestra que el proyecto no convierte cualquier mejora numérica en una decisión automática.

Gráfica:

[`02-cache-antes-despues.svg`](../experimentos/crecimiento-mensajeria/graficas/02-cache-antes-despues.svg)

---

# 22. Semana 12 — de “¿escala?” a “¿vale la pena distribuir?”

Documento:

[`alternativas-distribucion-s12.md`](../docs/architecture/alternativas-distribucion-s12.md)

Alternativas:

1. monolito modular;
2. réplicas del monolito;
3. microservicio síncrono;
4. eventos + proyecciones.

Costos relativos:

| Alternativa | Costo |
| --- | ---: |
| monolito modular | 1 C0 |
| réplicas | 2–3 C0 |
| microservicio síncrono | 4–8 C0 |
| eventos + proyecciones | 8–20 C0 |

C0:

> unidad comparativa, no precio en dinero.

La pregunta cambió de:

> “¿podemos hacer microservicios?”

a:

> “¿qué evidencia pagaría realmente ese costo?”

---

# 23. ADR-004 — mantener Mensajería en el monolito modular

[`ADR-004-distribucion-mensajeria.md`](../docs/adr/ADR-004-distribucion-mensajeria.md)

Razones:

- 200 VU sin degradación sostenida;
- 0 errores;
- p95 bajo umbral;
- sin requisito de disponibilidad independiente;
- caché solo parcialmente favorable;
- distribución introduce nuevas clases de fallo.

Estado:

> propuesta respaldada por S11; mini-comité 2 todavía pendiente.

PR:

[PR #25 — alineación Módulo 6](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25)

---

# 24. Segunda fitness function — proteger ADR-004 automáticamente

Código:

[`scripts/check_distribution_decision.py`](../scripts/check_distribution_decision.py)

Workflow:

[`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

Protege:

- topología Compose;
- ausencia de Kafka/RabbitMQ/Redis/Pulsar/NATS no aprobados;
- caché experimental apagada por defecto.

Regla:

~~~text
0 violaciones
~~~

Condición:

~~~text
>= 1 violación
    ↓
exit code != 0
    ↓
backend CI falla
~~~

---

# 25. Semana 13 — pasar de controles dispersos a threat model

PR:

[PR #26 — seguridad, OWASP y límites de confianza](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/26)

Documento:

[`10-seguridad.md`](10-seguridad.md)

Se inventariaron activos:

- credenciales;
- sesiones;
- chats;
- CV;
- documentos empresariales;
- ofertas;
- postulaciones;
- DB;
- configuración;
- CI.

Trust boundaries:

- [SVG](../docs/security/trust-boundaries-s13.svg)
- [PNG](../docs/security/trust-boundaries-s13.png)
- [PlantUML](../docs/security/trust-boundaries-s13.puml)

---

# 26. Semana 13 — seguridad basada en el sistema real

Controles comprobados:

- BCrypt;
- SecureRandom;
- SHA-256 del token;
- expiración;
- API stateless;
- autorización de participante;
- roles;
- SQL parametrizado;
- validaciones de archivos;
- restricciones DB;
- CI.

Riesgos reales importantes:

### Acceso a chat ajeno

Control:

~~~text
requireChatParticipant
~~~

### Documentos en `/uploads/**`

Hallazgo:

> UUID no equivale a autorización.

### Configuración local

Problemas si se publicara directamente:

- HTTP;
- seed demo;
- credenciales demo;
- integration token local.

### Ceguera operativa

Health/info no bastan para saber:

- quién falla login;
- dónde crecen 403;
- dónde aparecen 5xx;
- si hay ataque o abuso.

Ese último riesgo alimentó S14.

---

# 27. Semana 13 — IA filtrada contra superficie real

Documento:

[`auditoria-amenazas-ia-s13.md`](../docs/security/auditoria-amenazas-ia-s13.md)

Amenazas descartadas o matizadas:

- XXE → no hay XML;
- SSRF crítico → no hay fetch arbitrario de URL;
- XSS server-side principal → API JSON + Compose;
- CSRF crítico solo por disabled → API stateless Bearer;
- microservicios por “seguridad” → no resuelven automáticamente P0.

Conclusión:

> OWASP se utilizó como marco, no como checklist copiado.

---

# 28. Semana 14 — imaginar producción sin fingir que ya existe

PR:

[PR #27 — despliegue, observabilidad, ADR-005 y fitness CI](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/27)

Documento:

[`12-despliegue-observabilidad.md`](12-despliegue-observabilidad.md)

Alternativa A:

~~~text
Android
  ↓ HTTPS
TLS / Reverse proxy
  ↓
1 × Spring Boot
  ├─ PostgreSQL administrado
  ├─ object storage privado
  ├─ secret manager
  └─ metrics/logs/traces
~~~

Costo:

~~~text
1 D0
~~~

Alternativa B:

~~~text
Load balancer
  ├─ API 1
  └─ API 2+
      ↓
PostgreSQL administrado
+ storage + observabilidad
~~~

Costo:

~~~text
2–3 D0
~~~

D0:

> unidad relativa de despliegue; no dinero.

---

# 29. Observabilidad — convertir fallos conocidos en señales

Métricas:

- rate;
- errors;
- p95/p99;
- JVM;
- Hikari;
- DB;
- 401/403;
- login failures;
- storage.

Logs:

~~~text
timestamp
level
service
instance
requestId
traceId
spanId
route
status
duration
error.type
~~~

Datos que **no** deben ir a logs:

- tokens;
- contraseñas;
- contenido de chat;
- CV;
- documentos.

Trazas:

~~~text
HTTP
 → Security
 → Controller
 → Service/Auth
 → JDBC
 → PostgreSQL
~~~

Alertas iniciales:

~~~text
health DOWN >2 min
5xx >5 % / 5 min
p95 >500 ms / 5 min
pool >90 %
storage >85 %
backup vencido
login/403 anómalos
~~~

Matriz operativa:

[`matriz-riesgos-operativos-s14.md`](../docs/architecture/matriz-riesgos-operativos-s14.md)

---

# 30. ADR-005 — el destino conceptual actual

[`ADR-005-despliegue-observabilidad.md`](../docs/adr/ADR-005-despliegue-observabilidad.md)

Decisión propuesta:

> primer despliegue conceptual = monolito administrado mínimo.

No se introduce todavía:

- Kubernetes;
- broker;
- microservicios;
- redundancia sin SLO.

La redundancia queda como evolución si aparece evidencia.

---

# 31. La demostración más fuerte de automatización: ADR-004 rompe CI

Se introdujo deliberadamente:

~~~yaml
MESSAGE_CACHE_ENABLED=true
~~~

cuando la decisión exige:

~~~yaml
MESSAGE_CACHE_ENABLED=false
~~~

Run:

[GitHub Actions #60](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894)

Resultado técnico preciso:

> el job backend terminó en failure debido a `Enforce ADR-004 distribution decision` con exit code 1; el workflow global quedó posteriormente `Cancelled` por concurrencia.

Esto prueba la regla sin exagerar el estado global del workflow.

Corrección:

~~~yaml
MESSAGE_CACHE_ENABLED=false
~~~

Verde posterior:

[GitHub Actions #64](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40040820129)

Documento:

[`evidencia-fitness-ci-s14.md`](../docs/architecture/evidencia-fitness-ci-s14.md)

La violación no fue fusionada.

---

# 32. Uso crítico del Tutor — convertir recomendaciones en decisiones auditables

PR:

[PR #29 — uso crítico del Tutor M6](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/29)

Documento:

[`23-tarea-2-uso-critico-tutor-modulo-6.md`](23-tarea-2-uso-critico-tutor-modulo-6.md)

Ejemplos del método:

~~~text
Tutor propone interpretación
        ↓
se contrasta con JSON/código/CI
        ↓
se acepta / modifica / rechaza
        ↓
se documenta la razón
~~~

Casos:

- “PostgreSQL se saturó” → rechazado;
- retry → no probado porque no hubo fallos transitorios;
- caché → probada, pero no adoptada;
- microservicios → diferidos;
- amenazas genéricas → filtradas;
- comité no realizado → no inventado.

---

# 33. 30 de octubre — evolución del producto visible

Hasta este momento, la mayor parte del trabajo había fortalecido arquitectura y evidencia.

Luego se renovó el frontend Android.

PR:

[PR #31 — renovar experiencia visual del frontend](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/31)

Merge:

~~~text
ec45f99ae49980d075abc410db893aa0655f123a
~~~

Entre los cambios versionados aparecen:

- nuevo `UTrabajoDesign.kt`;
- identidad visual común;
- navegación inferior;
- headers;
- estados loading/empty/error;
- MaterialTheme;
- mejoras de navegación/onboarding;
- componentes visuales comunes.

Ejemplo de archivo:

[`UTrabajoDesign.kt`](../app/src/main/java/com/tab/utrabajo/presentation/components/UTrabajoDesign.kt)

Este PR representa algo importante para el relato final:

> el proyecto no terminó siendo solo un laboratorio arquitectónico; siguió siendo un producto móvil que también evolucionó visualmente.

---

# 34. 6 de noviembre — convertir todo el corte en un documento maestro

PR:

[PR #32 — informe final integral S9–S14](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/32)

Merge:

~~~text
3ff2d29ea316270f9804bde16f99c63d7306b3bc
~~~

Se creó:

[`dossier/informe-final.md`](informe-final.md)

El objetivo fue poder empezar en una sola página y bajar a:

- semana;
- ADR;
- medición;
- JSON;
- PR;
- CI;
- código;
- pregunta de defensa.

---

# 35. 6 de noviembre — hacer visible también el camino fallido

PR:

[PR #33 — evidencia visual, errores, soluciones y código](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/33)

Merge:

~~~text
5489e21fdcd88c9d4639bfaf5fb9d66bdf72d7eb
~~~

El informe final pasó a enlazar explícitamente:

- fallos de Docker;
- SDK;
- stacktrace;
- build/lint;
- recuperación;
- runner;
- resultados;
- gráficas;
- diagrams;
- código;
- CI rojo/verde.

Esto responde a una pregunta esencial de una defensa técnica:

> “¿Qué archivo, commit, CI o medición demuestra exactamente eso?”

---

# 36. Estado CI del corte histórico final

Commit:

~~~text
5489e21fdcd88c9d4639bfaf5fb9d66bdf72d7eb
~~~

Workflow:

[UTrabajo CI #77](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/42302330336)

Conclusión global:

~~~text
success
~~~

Backend:

- ADR-002 source rule → success;
- ADR-004 distribution rule → success;
- ArchUnit → success;
- backend tests/PostgreSQL → success;
- verification of experiment artifacts → success.

Android:

- experiment calculations → success;
- unit tests → success;
- lint → success;
- debug APK → success.

Este es el cierre técnico del estado narrado por este documento.

---

# 37. Cómo evolucionó la arquitectura

## Etapa inicial

~~~text
Android
  ↓
API
  ↓
PostgreSQL
~~~

Correcta como solución funcional, pero con varias decisiones todavía implícitas.

## Después de C4

~~~text
Compose
  ↓
Repository / Retrofit
  ↓
Security
  ↓
Controllers
  ↓
Service/Auth
  ↓
JDBC
  ↓
PostgreSQL
~~~

Ahora cada tramo tiene responsabilidad y código localizable.

## Después de S7–S8

~~~text
monolito modular
+
dependencias permitidas
+
fitness function
~~~

## Después de S9–S10

~~~text
Identidad
Perfiles
Ofertas
Postulaciones
Mensajería

Mensajería → Ofertas
contrato mínimo
~~~

## Después de S11–S12

~~~text
monolito modular
+
evidencia de crecimiento
+
ADR-004
+
fitness de distribución
~~~

## Después de S13–S14

~~~text
monolito modular
+
trust boundaries
+
riesgos
+
despliegue administrado conceptual
+
metrics/logs/traces/alerts
~~~

El producto no “cambió de arquitectura” cada semana.

La arquitectura fue **haciéndose más explícita, defendible y protegida**.

---

# 38. Cómo evolucionó la forma de medir

## Al principio

~~~text
“¿funciona?”
~~~

## S4

~~~text
¿cumple p95 <= 500 ms
para 50 mensajes
sobre 100.000
con 10 VU?
~~~

## S7

~~~text
¿dónde está el costo SQL?
OFFSET 0 vs OFFSET 50000 vs cursor
~~~

## S10

~~~text
¿un contrato síncrono mínimo satisface
la frontera Mensajería–Ofertas?
~~~

## S11

~~~text
¿qué ocurre cuando la concurrencia
sube 10 → 200 VU?
~~~

## Táctica

~~~text
¿la caché produce una mejora material
sin pagar demasiado costo?
~~~

## S14

~~~text
¿cómo sabremos en operación
que el sistema se está degradando?
~~~

El proceso pasó de **probar funcionamiento** a **medir decisiones arquitectónicas concretas**.

---

# 39. Cómo evolucionó la actitud frente a complejidad

Tecnologías/patrones considerados pero no adoptados automáticamente:

- microservicios;
- Kafka;
- RabbitMQ;
- Redis;
- CQRS completo;
- Event Sourcing;
- WebSocket;
- Kubernetes;
- retry;
- circuit breaker;
- caché por defecto.

Patrón de decisión:

~~~text
¿hay problema medido?
    │
    ├─ no → diferir complejidad
    │
    └─ sí
        ↓
¿qué alternativa mínima lo resuelve?
        ↓
medir
        ↓
decidir
~~~

Este es uno de los hilos más consistentes de todo el proyecto.

---

# 40. Cómo evolucionó la seguridad

## Inicial

Había controles reales:

- login;
- bearer token;
- roles;
- autorización;
- SQL parametrizado.

## Después

Se agregó una lectura de sistema:

- activos;
- límites de confianza;
- amenazas;
- controles existentes;
- riesgos residuales;
- controles requeridos antes de Internet.

La diferencia es:

~~~text
tener controles
≠
tener un modelo de seguridad
~~~

S13 construyó el segundo.

---

# 41. Cómo evolucionó CI

## Primera etapa

~~~text
tests Android
tests backend
~~~

## Después

~~~text
tests
+
lint
+
APK
+
PostgreSQL real
+
validación experimental
+
ADR-002 source check
+
ADR-002 ArchUnit
+
ADR-004 distribution check
~~~

CI pasó de verificar compilación/pruebas a **proteger decisiones de arquitectura**.

---

# 42. Cómo evolucionó la evidencia

## Al comienzo

- README;
- código;
- primeras pruebas.

## Luego

- prerregistros;
- resultados JSON;
- condiciones;
- hashes;
- C4;
- ADR;
- PR;
- CI;
- capturas;
- errores;
- recovery;
- matrices;
- gráficas.

## Al cierre

[`informe-final.md`](informe-final.md)

funciona como un **índice de evidencia**.

Y este documento agrega la dimensión temporal:

> **qué vino primero, por qué cambió y qué quedó después.**

---

# 43. Tabla cronológica principal

| Fecha | Hito | Evidencia |
| --- | --- | --- |
| 2026-08-20 | categoría y experimento de Mensajería consolidados | [decision gates](../docs/experiment/00-decision-gates.md) |
| 2026-08-20/21 | línea base S4 | [04-escenarios](04-escenarios-calidad.md) |
| 2026-08-21 | PR #1 S1–S4 | [PR #1](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/1) |
| 2026-09-04 | localización S7 | [experimento](../experimentos/localizacion-s7/README.md) |
| 2026-09-04 | PR #6 | [PR #6](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/6) |
| 2026-09-11 | regla ADR-002 en CI | [PR #10](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/10) |
| 2026-09-11 | decisión estilo/ADR | [PR #11](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/11) |
| 2026-09-11 | ajustes trazabilidad | [PR #12](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/12) |
| 2026-09-18 | guía de exposición S5–S8 | [PR #13](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/13) |
| 2026-10-03 | dominio e integración S9 | [PR #16](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/16) |
| 2026-10-16 | Spike 1 S10 | [PR #17](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/17) |
| 2026-10-16 | crecimiento S11 | [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20) |
| 2026-10-23 | distribución S12 | [PR #25](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25) |
| 2026-10-23 | seguridad S13 | [PR #26](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/26) |
| 2026-10-23 | despliegue/observabilidad S14 | [PR #27](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/27) |
| 2026-10-24 | uso crítico Tutor | [PR #29](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/29) |
| 2026-10-30 | renovación visual Android | [PR #31](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/31) |
| 2026-11-06 | informe final S9–S14 | [PR #32](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/32) |
| 2026-11-06 | evidencia visual/errores/código | [PR #33](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/33) |
| 2026-11-06 | corte histórico final narrado aquí | [CI #77](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/42302330336) |

---

# 44. Evolución de métricas — todos los números que cuentan la historia

## S4 — HTTP completo de Mensajería

~~~text
9,451
8,265
9,109 ms

mediana = 9,109 ms
~~~

## S7 — SQL localizado

~~~text
auth = 0,081 ms
offset 0 = 0,120 ms
offset 50000 = 102,050 ms
cursor = 0,123 ms
~~~

## S10 — frontera Mensajería–Ofertas

~~~text
4,781
4,360
4,710 ms

0 % fallos inesperados
100 % checks
~~~

## S11 — crecimiento

~~~text
10 VU  = 8,963 ms
50 VU  = 18,768 ms
100 VU = 39,258 ms
150 VU = 41,060 ms
200 VU = 58,235 ms
~~~

## Caché

~~~text
p95:        -4,908 %
throughput: -1,866 %
hits:       3.196
veredicto:  partially_supported
~~~

Estos números no miden exactamente la misma capa; por eso el proyecto los conserva con contexto en lugar de mezclarlos como una sola serie.

---

# 45. Evolución de decisiones ADR

| ADR | Pregunta | Respuesta actual |
| --- | --- | --- |
| ADR-001 | ¿qué estilo inmediato? | monolito modular |
| ADR-002 | ¿qué dependencias permitimos? | controllers delegan; no JDBC directo |
| ADR-003 | ¿cómo cruza Mensajería → Ofertas? | interacción síncrona mínima |
| ADR-004 | ¿extraer Mensajería? | no con evidencia actual |
| ADR-005 | ¿cómo desplegar conceptualmente? | monolito administrado observable |
| ADR-006 | ¿cómo manejar historial profundo? | cursor cuando sea necesario; offset compatible |

Las ADR no son documentos independientes.

Forman una cadena:

~~~text
estilo
 ↓
límites
 ↓
integración
 ↓
distribución
 ↓
despliegue/operación
 ↓
evolución de paginación
~~~

---

# 46. Lo que cambió y lo que deliberadamente NO cambió

## Cambió

- documentación;
- instrumentos;
- índices/paginación;
- límites;
- contratos;
- CI;
- diseño visual;
- seguridad documentada;
- estrategia de operación;
- capacidad de defensa.

## No cambió arbitrariamente

- Android como cliente principal;
- API Spring Boot/Kotlin;
- PostgreSQL;
- monolito como unidad de despliegue;
- uso de REST para el producto;
- necesidad de autorización servidor;
- ausencia de broker;
- ausencia de microservicios.

Esto muestra evolución controlada, no reescritura total del sistema.

---

# 47. Estado funcional actual documentado

Según [README](../README.md), UTrabajo permite:

- registrar cuentas;
- iniciar sesión;
- completar perfiles;
- publicar ofertas;
- consultar ofertas;
- postularse;
- adjuntar documentos;
- conversar por chat.

Cuentas demo:

| Rol | Usuario |
| --- | --- |
| Estudiante | `estudiante@utrabajo.local` |
| Empresa | `empresa@utrabajo.local` |

El README documenta la ejecución local mediante Docker Compose y Android Studio.

---

# 48. Estado arquitectónico actual documentado

~~~text
Android
  ↓
Spring Boot API
  ↓
PostgreSQL 16
  +
File storage
~~~

Estilo:

~~~text
monolito modular
~~~

Principios protegidos:

- autorización antes de acceso a chat;
- controller no entra directamente a JDBC;
- Mensajería no se distribuye sin revisión;
- caché experimental off por defecto;
- contrato de integración mínimo;
- evolución reversible.

---

# 49. Estado de seguridad actual

Lo que sí existe:

- autenticación;
- sesiones;
- autorización;
- controles de roles;
- SQL parametrizado;
- validación de archivos;
- tests;
- threat model.

Lo que debe ocurrir antes de Internet:

- TLS;
- secrets externos;
- proteger documentos sensibles;
- seed demo desactivado;
- storage privado;
- logging/alerting;
- calibración operativa.

Por eso el proyecto **no se declara production secure**.

---

# 50. Estado operacional actual

Existe:

- ejecución local;
- health;
- Docker;
- CI;
- scripts reproducibles;
- métricas históricas;
- diseño de observabilidad.

No existe como hecho probado:

- infraestructura cloud productiva;
- alta disponibilidad real;
- load balancer desplegado;
- cluster;
- Kubernetes;
- observabilidad productiva ya operando.

ADR-005 es conceptual.

---

# 51. Pendientes reales al corte histórico

Este documento también conserva lo que **no está cerrado**.

## 51.1 Semilla vs perfil normativo

La auditoría extrema observó una posible desalineación:

~~~text
repo:
1.000 conversaciones
1 % con >2.000

perfil auditado:
3.000 conversaciones
2 % con >2.000
~~~

Esto requiere confirmación normativa.

No invalida automáticamente las mediciones; limita la afirmación de conformidad exacta con el perfil.

## 51.2 Mezcla de extremos

k6 mide una conversación extrema fija.

Eso permite mantener constante el fenómeno, pero no demuestra una mezcla simultánea de toda la distribución.

## 51.3 ADR-002

La regla corre.

La prueba negativa roja histórica sigue pendiente.

## 51.4 Mini-comité ADR-004

Sigue pendiente.

No se inventa un veredicto.

## 51.5 Ratificaciones ADR

Varias decisiones permanecen formalmente como propuestas respaldadas/pendientes de acto humano.

## 51.6 Participación

[`16-evidencia-participacion.md`](16-evidencia-participacion.md) tiene corte histórico 18/09 y no representa por sí solo toda la actividad hasta noviembre.

## 51.7 S15–S16

El informe final original se titula S9–S14.

No debe usarse para afirmar contenido posterior que todavía no esté agregado.

## 51.8 Stack de referencia

El proyecto actual usa Spring Boot 4.1.0; si la versión 3.3.4 del catálogo fuera una restricción obligatoria y no una referencia, debe confirmarse con el docente.

---

# 52. Las evidencias más fuertes de todo el proyecto

Si durante la presentación solo hubiera tiempo para abrir diez cosas:

1. [`01-contexto-sistema.md`](01-contexto-sistema.md) — dónde empezó.
2. [`04-escenarios-calidad.md`](04-escenarios-calidad.md) — primera hipótesis/medición.
3. [C4 backend](07-c4-componentes-backend.md) — arquitectura real.
4. [S7 localización](../experimentos/localizacion-s7/README.md) — dónde duele.
5. [ADR-001](../docs/adr/ADR-001-limites-modulo-mensajeria.md) — estilo.
6. [Spike 1 resultado](../experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json) — integración.
7. [S11 resultado](../experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json) — crecimiento.
8. [Seguridad](10-seguridad.md) — threat model.
9. [Despliegue](12-despliegue-observabilidad.md) — destino conceptual.
10. [CI #77](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/42302330336) — estado técnico del corte final.

---

# 53. Defensa “cómo empezó y cómo terminó” — versión de 90 segundos

> UTrabajo no se creó desde cero para Arquitectura de Software. Ya era una aplicación Android que conectaba estudiantes y empresas mediante ofertas, postulaciones y chat. Al entrar al curso, la arquitectura material era Android, API Spring Boot y PostgreSQL, pero muchas decisiones todavía eran implícitas.
>
> En las primeras semanas convertimos los riesgos en escenarios medibles y creamos una línea base de Mensajería sobre una conversación de 100.000 mensajes. Después modelamos la arquitectura con C4 y automatizamos un walking skeleton real. En S7 localizamos que el costo profundo estaba en el uso de OFFSET, no en una necesidad de microservicios, y por eso elegimos monolito modular y protegimos límites con CI.
>
> Luego modelamos cinco bounded contexts y medimos la frontera Mensajería–Ofertas mediante un Spike 1 preregistrado. Más adelante aumentamos carga hasta 200 VU, probamos caché y como el beneficio fue parcial no la activamos. Esa evidencia llevó a ADR-004: no distribuir Mensajería todavía.
>
> Finalmente construimos un modelo de seguridad con OWASP y trust boundaries, diseñamos un despliegue administrado observable y demostramos que una violación de ADR-004 rompe el backend en CI. El producto además recibió una renovación visual Android.
>
> Al corte final, el sistema sigue siendo deliberadamente un monolito modular, pero ahora cada decisión importante tiene archivos, mediciones, PR, código o CI que la respaldan. El cambio más importante no fue agregar tecnologías: fue pasar de un sistema que funcionaba a un sistema que podemos medir, explicar, auditar y evolucionar con evidencia.

---

# 54. Defensa “cómo empezó y cómo terminó” — versión extendida

## Inicio

~~~text
producto funcional
+
arquitectura implícita
+
poca evidencia arquitectónica
~~~

## Proceso

~~~text
contexto
 → stakeholders
 → drivers
 → escenarios
 → línea base
 → C4
 → walking skeleton
 → localización
 → ADR
 → fitness
 → dominio
 → integración
 → spike
 → crecimiento
 → resiliencia
 → distribución
 → seguridad
 → despliegue
 → observabilidad
 → consolidación final
~~~

## Cierre

~~~text
producto funcional
+
arquitectura explícita
+
decisiones reversibles
+
mediciones
+
PR
+
CI
+
seguridad
+
operabilidad
+
evidencia navegable
~~~

---

# 55. La transformación principal

La evolución más importante puede resumirse así:

~~~text
AL PRINCIPIO
“esta es nuestra app”
        ↓

AL FINAL
“esta es nuestra app,
esta es su arquitectura,
esta es la evidencia de sus decisiones,
esto medimos,
esto no medimos,
esto rechazamos,
esto quedó pendiente,
y aquí está el archivo/commit/CI que prueba cada afirmación”
~~~

Ese es el resultado acumulado del semestre.

---

# 56. Enlaces de cierre

- [Repositorio](https://github.com/plantedseeker/Proyectlo_Arquitectura_software)
- [Dossier](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/dossier)
- [Informe final Corte 3](informe-final.md)
- [README del producto](../README.md)
- [PR #1](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/1)
- [PR #6](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/6)
- [PR #10](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/10)
- [PR #11](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/11)
- [PR #12](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/12)
- [PR #13](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/13)
- [PR #16](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/16)
- [PR #17](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/17)
- [PR #20](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/20)
- [PR #25](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/25)
- [PR #26](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/26)
- [PR #27](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/27)
- [PR #29](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/29)
- [PR #31](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/31)
- [PR #32](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/32)
- [PR #33](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/pull/33)
- [CI #77 — corte histórico verde](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/42302330336)

