# Matriz de trazabilidad C4 — UTrabajo

**Proyecto:** UTrabajo  
**Tipo de arquitectura:** As-Is  
**Propósito:** demostrar que los contenedores, componentes y relaciones representados en las vistas C4 corresponden con elementos reales y verificables del repositorio.

> Esta matriz debe leerse como una auditoría del modelo C4. Cada fila identifica una caja o relación arquitectónica y señala la evidencia concreta que permite comprobarla en el código, configuración o artefactos del sistema.

## Estados utilizados

- **Verificado:** el elemento o relación existe y puede comprobarse directamente en el repositorio.
- **Corregido:** el modelo inicial requirió un ajuste después de contrastarlo con el código.
- **Eliminado:** el elemento fue retirado del modelo porque no existe en la arquitectura As-Is.

---

## Matriz de trazabilidad

| ID | Nivel C4 | Elemento C4 | Responsabilidad declarada | Archivo / módulo real | Clase, símbolo o configuración verificable | Relación arquitectónica verificada | Estado | Observación / corrección |
|---|---|---|---|---|---|---|---|---|
| C2-01 | C2 — Contenedores | Aplicación Android | Interfaz de estudiante y empresa; navegación, sesión local y consumo de API | [`app/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/app) | `com.android.application`, `MainActivity`, Jetpack Compose, Retrofit | Aplicación Android → API REST mediante HTTP/JSON | **Verificado** | El cliente móvil no accede directamente a PostgreSQL. |
| C2-02 | C2 — Contenedores | API REST | Autenticación, perfiles, ofertas, postulaciones, chat, reglas de acceso y coordinación de persistencia | [`backend/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/backend) | `UTrabajoApiApplication`, controladores Spring MVC, servicios | Android → API REST; API → PostgreSQL; API → almacenamiento local | **Verificado** | Es el límite principal entre el cliente móvil y los recursos del servidor. |
| C2-03 | C2 — Contenedores | PostgreSQL 16 | Persistencia de usuarios, perfiles, ofertas, postulaciones, chats, mensajes y sesiones | [`docker-compose.yml`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/docker-compose.yml), [`db/migration/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/backend/src/main/resources/db/migration) | servicio `db`, PostgreSQL 16, migraciones Flyway | API REST → PostgreSQL mediante JDBC | **Verificado** | La base de datos se ejecuta como servicio independiente en Docker Compose. |
| C2-04 | C2 — Contenedores | Almacenamiento local de archivos | Guardar avatares, CV y documentos empresariales | [`FileStorageService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/FileStorageService.kt) | `FileStorageService` | API REST → sistema de archivos local | **Verificado** | El acceso a archivos está administrado por la API. |
| C3-A01 | C3 — Android | Pantallas / capa de presentación | Mostrar interfaz, navegación y acciones del usuario | [`presentation/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/app/src/main/java/com/tab/utrabajo/presentation) | pantallas Compose y navegación | Pantallas → `UTrabajoRepository` | **Verificado** | La UI consume datos a través del repositorio, no directamente desde PostgreSQL. |
| C3-A02 | C3 — Android | MainActivity | Punto de entrada principal de la aplicación Android | [`MainActivity.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/MainActivity.kt) | `MainActivity` | Inicio de aplicación → navegación/presentación | **Verificado** | Inicializa la aplicación y la composición principal. |
| C3-A03 | C3 — Android | UTrabajoRepository | Gestionar sesión local, llamadas a API y coordinación de datos del cliente | [`UTrabajoRepository.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/UTrabajoRepository.kt) | `UTrabajoRepository` | Presentación → Repository → `ApiService` | **Verificado** | Actúa como intermediario entre la UI y Retrofit. |
| C3-A04 | C3 — Android | ApiService / Retrofit | Definir los endpoints HTTP consumidos por Android | [`ApiService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/ApiService.kt) | `ApiService` | Android → API REST mediante Retrofit/HTTP | **Verificado** | Contiene contratos para autenticación, ofertas, postulaciones, perfiles y mensajería. |
| C3-A05 | C3 — Android | Sesión local | Conservar token y datos básicos de sesión del usuario | [`UTrabajoRepository.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/UTrabajoRepository.kt) | `SharedPreferences`, `utrabajo_session` | Repository → almacenamiento local de sesión | **Verificado** | El token Bearer se conserva localmente y se añade a solicitudes autenticadas. |
| C3-B01 | C3 — Backend | TokenAuthenticationFilter | Extraer el token Bearer de la solicitud y autenticar al usuario | [`TokenAuthenticationFilter.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/TokenAuthenticationFilter.kt) | `TokenAuthenticationFilter` | Solicitud HTTP → filtro de autenticación → seguridad Spring | **Verificado** | La petición autenticada atraviesa la cadena de seguridad antes de la lógica protegida. |
| C3-B02 | C3 — Backend | SecurityConfig | Configurar seguridad, rutas públicas/protegidas y comportamiento stateless | [`SecurityConfig.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/config/SecurityConfig.kt) | `SecurityConfig`, `SecurityFilterChain` | Filtro/autenticación → autorización de endpoints | **Verificado** | Configura Spring Security y las reglas de acceso de la API. |
| C3-B03 | C3 — Backend | AuthController | Registro, login, consulta de sesión y logout | [`AuthController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/AuthController.kt) | `AuthController` | HTTP `/api/auth/**` → `AuthService` | **Verificado** | Expone las operaciones de autenticación. |
| C3-B04 | C3 — Backend | ProfileController | Gestionar perfil, CV, avatar y documentos de empresa | [`ProfileController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/ProfileController.kt) | `ProfileController` | Controller → servicios de dominio / `FileStorageService` | **Verificado** | Conecta operaciones de perfil con datos y almacenamiento de archivos. |
| C3-B05 | C3 — Backend | JobController | Consultar, crear, actualizar y eliminar ofertas laborales | [`JobController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/JobController.kt) | `JobController` | Controller → `UTrabajoService` | **Verificado** | Gestiona los endpoints de ofertas. |
| C3-B06 | C3 — Backend | ApplicationController | Gestionar postulaciones de estudiantes | [`ApplicationController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/ApplicationController.kt) | `ApplicationController` | Controller → `UTrabajoService` | **Verificado** | Delega las reglas de postulación al servicio. |
| C3-B07 | C3 — Backend | ChatController | Listar chats, consultar mensajes y enviar mensajes | [`ChatController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt) | `ChatController`, endpoint `/api/chats/{chatId}/messages` | Controller → `UTrabajoService` | **Verificado** | Es el punto de entrada del flujo de mensajería medido con k6. |
| C3-B08 | C3 — Backend | AuthService | Validar credenciales, crear sesiones y resolver usuarios autenticados | [`AuthService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/AuthService.kt) | `AuthService` | `AuthController` / filtro → `AuthService` → PostgreSQL | **Verificado** | Usa persistencia para usuarios y sesiones. |
| C3-B09 | C3 — Backend | UTrabajoService | Aplicar reglas principales de negocio para perfiles, ofertas, postulaciones y mensajería | [`UTrabajoService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt) | `UTrabajoService`, `messages()`, `sendMessage()`, `requireChatParticipant()` | Controllers → Service → `JdbcClient` | **Verificado** | Contiene validaciones de rol, participación en chat y acceso a persistencia. |
| C3-B10 | C3 — Backend | FileStorageService | Validar y almacenar archivos | [`FileStorageService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/FileStorageService.kt) | `FileStorageService` | `ProfileController` / API → sistema de archivos | **Verificado** | Administra almacenamiento local bajo control del backend. |
| C3-B11 | C3 — Backend | JdbcClient / JDBC | Ejecutar consultas SQL desde los servicios hacia PostgreSQL | [`UTrabajoService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt), [`AuthService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/AuthService.kt) | `JdbcClient` | Servicio → JDBC → PostgreSQL | **Corregido** | JDBC es una tecnología/mecanismo de acceso a datos, no un contenedor independiente. |
| C3-B12 | C3 — Backend | Flyway | Gestionar la evolución del esquema de base de datos | [`db/migration/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/backend/src/main/resources/db/migration) | migraciones `V1__...`, `V2__...` | Flyway → esquema PostgreSQL | **Corregido** | Flyway es una herramienta de migración, no un contenedor de la arquitectura. |
| REL-01 | C2/C3 | Android → API REST | Permitir que el cliente consuma funcionalidades del backend | [`ApiService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/ApiService.kt), [`UTrabajoRepository.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/UTrabajoRepository.kt) | Retrofit, base URL, interceptor Bearer | Cliente Android → HTTP/JSON → API REST | **Verificado** | La relación está implementada en Retrofit y el repositorio del cliente. |
| REL-02 | C3 | ChatController → UTrabajoService | Delegar la lógica de mensajería | [`ChatController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt), [`UTrabajoService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt) | llamadas desde controller a métodos del service | Endpoint de mensajes → servicio de dominio | **Verificado** | La relación puede demostrarse directamente en código. |
| REL-03 | C3 | UTrabajoService → PostgreSQL | Consultar y modificar datos de negocio | [`UTrabajoService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt) | `JdbcClient`, consultas SQL | Service → JDBC → PostgreSQL | **Verificado** | Incluye consultas de chats y mensajes. |
| REL-04 | C3 | AuthService → PostgreSQL | Persistir y consultar usuarios y sesiones | [`AuthService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/AuthService.kt) | `JdbcClient`, consultas SQL | AuthService → PostgreSQL | **Verificado** | Implementa autenticación y sesiones persistidas. |
| REL-05 | C3 | API → almacenamiento local | Guardar archivos recibidos por la API | [`FileStorageService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/FileStorageService.kt), [`application.yml`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/resources/application.yml) | directorio de uploads / configuración de almacenamiento | API → filesystem local | **Verificado** | No existe un servicio externo de objetos en la arquitectura actual. |
| COR-01 | C2 | JDBC como supuesto contenedor | Acceso a datos | Código backend | `JdbcClient` | API → PostgreSQL | **Corregido** | Se corrigió el modelo: JDBC es tecnología de acceso y no una unidad ejecutable independiente. |
| COR-02 | C2 | Flyway como supuesto contenedor | Migraciones del esquema | [`db/migration/`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/backend/src/main/resources/db/migration) | migraciones SQL | Flyway → PostgreSQL | **Corregido** | Se corrigió el modelo: Flyway es herramienta de migración, no contenedor. |
| COR-03 | C3 | Android → Controller directo | Consumo de endpoints protegidos | [`TokenAuthenticationFilter.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/TokenAuthenticationFilter.kt), [`SecurityConfig.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/config/SecurityConfig.kt) | filtro + cadena de seguridad | Android → seguridad → controller | **Corregido** | La relación se ajustó para no sugerir que las solicitudes protegidas saltan la seguridad. |
| COR-04 | C2/C3 | k6 como parte del producto | Generar carga para el experimento | [`carga-mensajeria.js`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/experimentos/medicion-escenario-01/carga-mensajeria.js) | script k6 | k6 → API durante experimentos | **Corregido** | k6 es una herramienta temporal de medición, no un contenedor del producto UTrabajo. |
| COR-05 | C2/C3 | Redis / broker / WebSocket / microservicio de mensajería | Arquitectura futura hipotética | No existe evidencia en el código As-Is | — | — | **Eliminado** | No se representa porque no existe actualmente en el sistema real. |

---

## Recorrido verificable de mensajería

El flujo más importante para demostrar la trazabilidad en una sustentación es:

```text
Aplicación Android
        ↓
UTrabajoRepository
        ↓
ApiService / Retrofit
        ↓ HTTP/JSON + Bearer
TokenAuthenticationFilter / SecurityConfig
        ↓
ChatController
        ↓
UTrabajoService
        ↓
JdbcClient / JDBC
        ↓
PostgreSQL
```

Evidencias principales:

1. [`UTrabajoRepository.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/UTrabajoRepository.kt)
2. [`ApiService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/app/src/main/java/com/tab/utrabajo/data/ApiService.kt)
3. [`TokenAuthenticationFilter.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/auth/TokenAuthenticationFilter.kt)
4. [`SecurityConfig.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/config/SecurityConfig.kt)
5. [`ChatController.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt)
6. [`UTrabajoService.kt`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt)
7. [`docker-compose.yml`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/blob/main/docker-compose.yml)
8. [`Migraciones PostgreSQL`](https://github.com/plantedseeker/Proyectlo_Arquitectura_software/tree/main/backend/src/main/resources/db/migration)

---

## Conclusión de la auditoría

La revisión de trazabilidad confirma que las cajas principales representadas en los niveles C2 y C3 tienen una correspondencia verificable con módulos, clases, configuraciones o artefactos reales del repositorio.

La auditoría también produjo correcciones importantes:

- JDBC y Flyway se reconocen como tecnologías, no como contenedores.
- El acceso a controladores protegidos se representa pasando por la seguridad del backend.
- k6 se clasifica como herramienta temporal de medición y no como parte del producto.
- No se incorporan Redis, WebSocket, brokers ni microservicios porque no existen en la arquitectura As-Is actual.

De esta manera, la matriz permite pasar directamente del modelo C4 al repositorio y demostrar dónde existe cada elemento y cómo se comprueban sus relaciones.
