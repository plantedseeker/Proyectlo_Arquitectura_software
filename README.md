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

La evidencia está en `docs/experiment/`:

- `00-decision-gates.md`: categoría, persistencia y alcance confirmados.
- `01-preregistration.md`: hipótesis, métrica, semilla y presión externa.
- `02-protocol.md`: semilla y medición reproducibles.
- `03-evidence-matrix.md`: estado honesto requisito por requisito.

La línea base aún no debe ejecutarse: primero hay que guardar el prerregistro en un commit y conservar su hash. Después, el protocolo genera cuatro corridas, descarta la primera y compara las tres válidas con la hipótesis.
