# UTrabajo

UTrabajo es una aplicación móvil Android que conecta estudiantes con empresas en Colombia. Permite registrar cuentas, completar perfiles, publicar y consultar ofertas, postularse, adjuntar documentos y conversar por chat.

## Arquitectura local

```text
Android (Kotlin + Compose)
        │ HTTP/JSON + sesión Bearer
        ▼
API REST (Spring Boot + Kotlin, puerto 8080)
        │ JDBC + Flyway
        ▼
PostgreSQL 16 (puerto 5432)
```

Android nunca contiene credenciales de PostgreSQL ni se conecta directamente a la base. La API valida roles y propiedad de los recursos. Los archivos se almacenan en un volumen local; esta separación permite migrar más adelante a HTTPS, PostgreSQL administrado y almacenamiento de objetos sin reescribir las pantallas.

## Tecnologías

- Android SDK 36, mínimo API 28; Kotlin, Jetpack Compose y Retrofit
- API Kotlin/JDK 21 con Spring Boot, Spring Security, JDBC y Flyway
- PostgreSQL 16.14
- Docker Compose para ejecución local
- Pruebas JUnit de dominio e integración

## Arranque rápido

Requisitos: Docker Desktop con Compose y Android Studio con JDK 17 y Android SDK 36.

1. Desde la raíz, levantar base y API:

   ```bash
   docker compose up --build
   ```

2. Esperar a que la salud responda `UP`:

   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. Abrir la raíz en Android Studio, sincronizar Gradle e iniciar la variante `debug` en un emulador. El cliente usa `http://10.0.2.2:8080/`, que desde el emulador apunta al equipo anfitrión.

Cuentas sintéticas creadas automáticamente:

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Estudiante | `estudiante@utrabajo.local` | `UTrabajo1!` |
| Empresa | `empresa@utrabajo.local` | `UTrabajo1!` |

Para detener:

```bash
docker compose down
```

Los volúmenes conservan datos. Para eliminarlos deliberadamente y reiniciar la base: `docker compose down -v`.

## Walking skeleton

El recorrido mínimo de mensajería comprueba Docker, PostgreSQL, Flyway, salud de
la API, autenticación, oferta, chat, envío/lectura del mensaje, persistencia SQL
y rechazo de acceso sin token:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1
```

Después se completa un checkpoint manual en el emulador buscando el mismo
`message_marker` que aparece en el JSON de resultados. El alcance, los criterios
y los modos de fallo están en
[`docs/architecture/walking-skeleton.md`](docs/architecture/walking-skeleton.md)
y
[`docs/architecture/failure-modes-walking-skeleton.md`](docs/architecture/failure-modes-walking-skeleton.md).

## Configuración

La configuración local predeterminada vive en `docker-compose.yml`. `.env.example` documenta las variables. No guardar contraseñas reales en Git.

Para cambiar la URL de la API al compilar Android:

```bash
./gradlew assembleDebug -PAPI_BASE_URL=https://api.ejemplo.com/
```

La migración futura a Internet debe usar HTTPS, secretos externos, CORS/reverse proxy, almacenamiento de objetos y una base administrada; no basta con abrir el puerto local.

## Verificación

Android, en Linux/macOS:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Android, en PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Backend (requiere PostgreSQL disponible con las variables `DB_*`):

```bash
./backend/gradlew -p backend test
```

Instrumento de rendimiento:

```bash
python -m unittest discover -s experiments/postgresql/tests -v
```

GitHub Actions ejecuta las pruebas de Android y una integración real del backend contra PostgreSQL 16.

## Experimento S1–S4

El dossier exigido por el checklist está en `dossier/` y el experimento principal
en `experimentos/medicion-escenario-01/`. La categoría confirmada es
**Mensajería y mesa de ayuda**.

La evidencia histórica de la primera exploración permanece en `docs/experiment/`:

- `00-decision-gates.md`: decisiones y transición al experimento de mensajería.
- `01-preregistration.md`: hipótesis, métrica, semilla y presión externa.
- `02-protocol.md`: semilla y medición reproducibles.
- `03-evidence-matrix.md`: estado honesto requisito por requisito.

La línea base histórica está conservada en `docs/experiment/results/baseline.json`.
Después de la observación de auditoría se generó una segunda evidencia sin
sobrescribirla: `baseline-s4-audit.json`, con mediana, identificación de la
máquina, energía y topología. Estos resultados de ofertas se conservan como
históricos; el experimento evaluado por categoría es ahora el de mensajería.

Para ejecutar la medición de mensajería con k6:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

La línea base registrada el 2026-08-20 produjo p95 válidos de 9,451; 8,265 y
9,109 ms, con mediana **9,109 ms** frente al umbral prerregistrado de 500 ms.
k6, la API y PostgreSQL 16.14 compartieron Docker Desktop y el mismo equipo
físico durante la medición. El conteo SQL verificó 1.000 conversaciones y
289.000 mensajes con distribución extrema 900/90/9/1.

## C4 y decisiones S5–S8

La secuencia completa para levantar el sistema, comprobar el walking skeleton y
repetir la línea base está en [`dossier/GUIA-EJECUCION.md`](dossier/GUIA-EJECUCION.md).

Las vistas C4 as-is aprobadas, su audiencia y la trazabilidad a código están en
`dossier/05-c4-contexto.md` a `dossier/09-c4-trazabilidad-localizacion.md`.

Para localizar el costo SQL de mensajería y comparar página reciente, offset
profundo y cursor:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-s7-localization.ps1
```

El método separa datos, interpretaciones y supuestos en
`experimentos/localizacion-s7/README.md`. Las decisiones propuestas y sus
alternativas están en `docs/adr/`; el mini-comité y la evidencia de la primera
restricción arquitectónica están en `docs/architecture/`.

## Módulo 5 — dominio e integración (S9–S10)

El recorrido completo y autónomo para presentar el módulo está en el
[`resumen ejecutivo de M5`](dossier/18-resumen-ejecutivo-modulo-5.md).

Artefactos principales:

- [`09-api-events-integration.md`](dossier/09-api-events-integration.md) — artefacto integrador S9 con el nombre literal del checklist;
- [`subdominios y bounded contexts`](docs/dominio/subdominios.md) y [`Context Map`](docs/dominio/context-map.md);
- [`contrato OpenAPI`](docs/integracion/contrato-api.yaml) y [`comparación síncrono/asíncrono`](docs/integracion/sincrono-vs-asincrono.md);
- [`Spike 1`](experimentos/spike-01-integracion/README.md), [`resultado oficial`](experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json) y [`veredicto`](experimentos/spike-01-integracion/veredicto.md);
- [`ADR-003`](docs/adr/ADR-003-integracion-contextos.md) y [`checklist S9–S10`](dossier/17-checklist-modulo-5.md).

La ejecución oficial del Spike 1 obtuvo p95 de 4,781; 4,360 y 4,710 ms en
las tres corridas válidas, con todos los controles funcionales aprobados y 0
fallos HTTP inesperados. La hipótesis quedó respaldada bajo las condiciones
registradas y el alcance quedó ajustado antes de medir al payload mínimo
`jobId` + `companyId`.

Para reproducirlo con Docker Desktop disponible y árbol Git limpio:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-spike-01-integracion.ps1
```

## Crecimiento y distribución (S11–S12)

El recorrido autónomo para presentar estas dos semanas está en el
[`resumen ejecutivo S11–S12`](dossier/19-resumen-ejecutivo-s11-s12.md) y su
[`checklist verificable`](dossier/20-checklist-semanas-11-12.md).

S11 aumentó M2 por escalones hasta 200 VU sin encontrar degradación sostenida.
La comparación de caché fue parcialmente favorable en latencia, pero redujo el
throughput mediano; por ello permanece deshabilitada. S12 compara cuatro
alternativas de distribución, explicita costo relativo y propone conservar el
monolito modular en [`ADR-004`](docs/adr/ADR-004-distribucion-mensajeria.md),
pendiente de mini-comité 2.

Artefactos principales:

- [`experimento de crecimiento`](experimentos/crecimiento-mensajeria/README.md);
- [`gráficas reproducibles`](experimentos/crecimiento-mensajeria/graficas/README.md) y [`trazabilidad del prerregistro`](docs/architecture/trazabilidad-prerregistro-s11.md);
- [`matrices y resultado antes/después`](experimentos/crecimiento-mensajeria/06-resultado-tactica-cache.md);
- [`alternativas y costo`](docs/architecture/alternativas-distribucion-s12.md);
- [`auditoría de microservicios propuestos por IA`](docs/architecture/auditoria-ia-microservicios-s12.md);
- [`segunda fitness function`](docs/architecture/fitness-function-distribucion-s12.md);
- [`formato de mini-comité 2`](docs/architecture/mini-comite-s12.md).

Verificación local de decisiones:

```powershell
.\backend\gradlew.bat -p backend test --tests "*DatabaseIsolationArchitectureTests"
python .\scripts\check_architecture.py
python .\scripts\check_distribution_decision.py
python -m unittest discover -s .\scripts\tests -v
```


## Seguridad S13

El análisis integral de amenazas y controles está en
[`dossier/10-seguridad.md`](dossier/10-seguridad.md). Usa OWASP Top 10:2025
como referencia, pero filtra cada categoría contra el sistema real.

Artefactos principales:

- [matriz de amenazas y controles](dossier/10-seguridad.md);
- [límites de confianza renderizados](docs/security/trust-boundaries-s13.svg) ([PNG](docs/security/trust-boundaries-s13.png), [fuente PlantUML](docs/security/trust-boundaries-s13.puml));
- [auditoría crítica de amenazas propuestas por IA](docs/security/auditoria-amenazas-ia-s13.md).

El análisis distingue controles ya implementados de controles requeridos antes
de un despliegue público. No afirma seguridad de producción para la topología
local actual.


## Seguridad, despliegue y observabilidad (S13–S14)

El recorrido principal está en
[`dossier/22-resumen-ejecutivo-s13-s14.md`](dossier/22-resumen-ejecutivo-s13-s14.md)
y el estado requisito por requisito en
[`dossier/21-checklist-semanas-13-14.md`](dossier/21-checklist-semanas-13-14.md).

Evidencias principales:

- [seguridad S13](dossier/10-seguridad.md);
- [despliegue y observabilidad S14](dossier/12-despliegue-observabilidad.md);
- [diagrama renderizado del despliegue conceptual](docs/architecture/deployment-s14.svg) ([PNG](docs/architecture/deployment-s14.png), [fuente PlantUML](docs/architecture/deployment-s14.puml));
- [matriz de riesgos operativos](docs/architecture/matriz-riesgos-operativos-s14.md);
- [ADR-005](docs/adr/ADR-005-despliegue-observabilidad.md);
- [ADR-006 de paginación](docs/adr/ADR-006-paginacion-historial-mensajes.md);
- [evidencia de fitness function en CI](docs/architecture/evidencia-fitness-ci-s14.md).

S13 usa OWASP Top 10:2025 como referencia y filtra amenazas contra el sistema
real. S14 mantiene el monolito modular, compara conceptualmente un despliegue
administrado mínimo con una alternativa redundante y define métricas, logs,
trazas y alertas. Ninguna de esas topologías se presenta como infraestructura
productiva ya desplegada.
