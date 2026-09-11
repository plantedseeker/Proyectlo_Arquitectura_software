# Guía única de ejecución — UTrabajo, walking skeleton y línea base

> **Objetivo:** permitir que cualquier lector clone el repositorio, levante
> UTrabajo, compruebe el recorrido vertical de mensajería y reproduzca su línea
> base sin tener que buscar instrucciones en distintos archivos.

Repositorio: https://github.com/plantedseeker/Proyectlo_Arquitectura_software

---

## 1. Qué se va a ejecutar

La arquitectura local de UTrabajo es:

```text
Aplicación Android
        │
        │ HTTP/JSON + Bearer
        ▼
API REST — Spring Boot + Kotlin
        │
        │ JDBC
        ▼
PostgreSQL 16
```

Para la medición de rendimiento se añade temporalmente **k6**, que genera solicitudes HTTP contra la API:

```text
k6
 │
 │ GET /api/chats/{chatId}/messages
 ▼
API REST
 ▼
PostgreSQL
```

k6 **no forma parte del producto UTrabajo**. Se utiliza únicamente como herramienta de carga y medición.

---

## 2. Ruta rápida

Si el equipo ya tiene instalados los requisitos, el proceso completo de medición se reduce a:

```powershell
git clone https://github.com/plantedseeker/Proyectlo_Arquitectura_software.git
cd Proyectlo_Arquitectura_software
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

El resultado final queda en:

```text
experimentos/medicion-escenario-01/resultados/resultado.json
```

Si primero se quiere comprobar que las partes críticas están conectadas, sin
ejecutar todavía la prueba de carga:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1
```

Ese recorrido deja su resultado en:

```text
experimentos/walking-skeleton/resultados/ultima-ejecucion.json
```

> La ejecución automatizada de la línea base está preparada principalmente para **Windows + PowerShell + Docker Desktop**.

---

## 3. Requisitos

### Obligatorios para ejecutar la línea base

- Git
- Docker Desktop
- Docker Compose
- PowerShell
- Python 3

No es necesario instalar k6 manualmente porque la prueba utiliza el servicio de k6 definido en Docker Compose.

### Adicionales para ejecutar la aplicación Android

- Android Studio
- JDK 17
- Android SDK 36
- Emulador Android compatible

---

## 4. Verificar los requisitos

Abrir PowerShell y comprobar:

```powershell
git --version
docker version
docker compose version
python --version
```

Docker Desktop debe estar **abierto y funcionando**.

---

## 5. Clonar el repositorio

```powershell
git clone https://github.com/plantedseeker/Proyectlo_Arquitectura_software.git
cd Proyectlo_Arquitectura_software
```

Todos los comandos siguientes deben ejecutarse desde la **raíz del repositorio**.

```text
Proyectlo_Arquitectura_software/
├── app/
├── backend/
├── dossier/
├── experimentos/
├── scripts/
├── docker-compose.yml
├── gradlew.bat
└── README.md
```

---

## 6. Ejecutar UTrabajo sin hacer todavía la medición

Para levantar PostgreSQL y la API:

```powershell
docker compose up --build
```

En otra terminal comprobar la salud de la API:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

También puede usarse:

```powershell
curl http://localhost:8080/actuator/health
```

La API debe responder con un estado equivalente a:

```json
{
  "status": "UP"
}
```

La API queda disponible en `http://localhost:8080` y PostgreSQL usa localmente `localhost:5432`.

---

## 7. Ejecutar la aplicación Android

Este paso **no es necesario para ejecutar la línea base con k6**, pero permite probar el sistema completo desde el cliente móvil.

1. Abrir la raíz del repositorio en Android Studio.
2. Esperar la sincronización de Gradle.
3. Iniciar un emulador Android.
4. Ejecutar la variante `debug`.

El cliente Android utiliza por defecto:

```text
http://10.0.2.2:8080/
```

En un emulador Android, `10.0.2.2` apunta al equipo anfitrión donde está ejecutándose la API.

### Usuarios sintéticos

| Rol | Correo | Contraseña |
|---|---|---|
| Estudiante | `estudiante@utrabajo.local` | `UTrabajo1!` |
| Empresa | `empresa@utrabajo.local` | `UTrabajo1!` |

---

## 8. Ejecutar la línea base de mensajería

Con Docker Desktop abierto, desde la raíz:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

Este es el **punto de entrada único** para reproducir la medición completa. No es necesario ejecutar manualmente la semilla, k6 ni el cálculo de resultados.

---

## 9. Qué hace automáticamente el script

El archivo:

```text
scripts/run-messaging-baseline.ps1
```

realiza siete etapas.

### 1/7 — Verifica Docker y Docker Compose

Comprueba que Docker y Compose estén disponibles.

### 2/7 — Levanta PostgreSQL y la API

Ejecuta los servicios definidos en `docker-compose.yml`. Si no se usa `-SkipBuild`, también reconstruye las imágenes.

### 3/7 — Espera la salud de la API

Consulta automáticamente:

```text
http://localhost:8080/actuator/health
```

La medición no continúa hasta que la API responda `UP`.

### 4/7 — Carga la semilla

Utiliza:

```text
experimentos/medicion-escenario-01/seed-mensajeria.sql
```

La semilla crea:

```text
1.000 conversaciones
289.000 mensajes
```

Distribución extrema:

```text
900 conversaciones × 10 mensajes
 90 conversaciones × 1.000 mensajes
  9 conversaciones × 10.000 mensajes
  1 conversación   × 100.000 mensajes
```

### 5/7 — Captura el contexto

Ejecuta `experimentos/medicion-escenario-01/resumir_resultados.py` y registra información del entorno y del commit Git utilizado.

La evidencia se guarda en:

```text
experimentos/medicion-escenario-01/resultados/contexto.json
```

### 6/7 — Ejecuta cuatro corridas con k6

Utiliza:

```text
experimentos/medicion-escenario-01/carga-mensajeria.js
```

Se realizan 4 corridas:

```text
Corrida 1 → calentamiento
Corrida 2 → válida
Corrida 3 → válida
Corrida 4 → válida
```

Cada corrida utiliza:

```text
10 VU
40 solicitudes
```

`VU` significa **Virtual User** o usuario virtual.

### 7/7 — Calcula el resultado

Finalmente ejecuta nuevamente `resumir_resultados.py` para validar las corridas y obtener la mediana de los P95 válidos.

El resultado final queda en:

```text
experimentos/medicion-escenario-01/resultados/resultado.json
```

---

## 10. Qué se está midiendo

La operación medida es:

```http
GET /api/chats/3fb4ce25-b840-4685-d2ea-53f9a4bdedc6/messages?limit=50&offset=0
```

La prueba solicita una página de **50 mensajes** de una conversación grande.

k6 valida:

```text
HTTP 200
50 mensajes devueltos
0 % de solicitudes HTTP fallidas
100 % de checks válidos
P95 <= 500 ms
```

---

## 11. Qué significa P95

**P95** es el percentil 95.

Si una corrida tiene `P95 = 9 ms`, significa que aproximadamente el **95 % de las solicitudes terminó en 9 ms o menos** y solo el 5 % tardó más.

El umbral del escenario es:

```text
P95 <= 500 ms
```

Por tanto:

```text
P95 <= 500 ms  → cumple
P95 > 500 ms   → no cumple
```

---

## 12. Dónde queda toda la evidencia

```text
experimentos/
└── medicion-escenario-01/
    ├── README.md
    ├── seed-mensajeria.sql
    ├── carga-mensajeria.js
    ├── resumir_resultados.py
    └── resultados/
        ├── contexto.json
        ├── run-1.json
        ├── run-2.json
        ├── run-3.json
        ├── run-4.json
        ├── resultado.json
        ├── verificacion-semilla.json
        └── SHA256SUMS.txt
```

| Archivo | Para qué sirve |
|---|---|
| `seed-mensajeria.sql` | Genera los datos sintéticos |
| `carga-mensajeria.js` | Define la carga que ejecuta k6 |
| `resumir_resultados.py` | Captura contexto, valida y resume resultados |
| `contexto.json` | Registra máquina, revisión y condiciones |
| `run-1.json` | Primera corrida / calentamiento |
| `run-2.json` | Segunda corrida |
| `run-3.json` | Tercera corrida |
| `run-4.json` | Cuarta corrida |
| `resultado.json` | Resultado agregado y decisión |
| `verificacion-semilla.json` | Verifica conteos reales en PostgreSQL |
| `SHA256SUMS.txt` | Huellas de integridad de la evidencia |

---

## 13. Resultado histórico de referencia

El repositorio conserva una línea base registrada el **20 de agosto de 2026**.

```text
Run 2 P95 = 9,451 ms
Run 3 P95 = 8,265 ms
Run 4 P95 = 9,109 ms
```

Mediana de los P95:

```text
9,109 ms
```

Umbral:

```text
500 ms
```

Estos valores son una **referencia histórica**. Una nueva ejecución debe evaluarse utilizando los archivos generados por esa nueva corrida.

---

## 14. Re-ejecutar la medición

Para una nueva ejecución completa:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

El script admite dos opciones:

### Evitar reconstruir imágenes Docker

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1 -SkipBuild
```

### Conservar una semilla previa

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1 -SkipSeed
```

Para una **evidencia académica reproducible**, se recomienda ejecutar sin `-SkipSeed`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

---

## 15. Detener o reiniciar el sistema

Para detener los contenedores y conservar los datos:

```powershell
docker compose down
```

Para eliminar también los volúmenes y reiniciar deliberadamente la base:

```powershell
docker compose down -v
```

> `-v` elimina los volúmenes del proyecto.

Este comando solo debe usarse deliberadamente con los datos sintéticos locales.
No es una estrategia de recuperación para una base con información que deba
conservarse.

---

## 16. Ver servicios y logs

Servicios activos:

```powershell
docker compose ps
```

Todos los logs:

```powershell
docker compose logs
```

Solo API:

```powershell
docker compose logs api
```

Solo PostgreSQL:

```powershell
docker compose logs db
```

---

## 17. Problemas comunes

### Docker no está disponible

1. Comprobar que Docker Desktop esté instalado.
2. Abrir Docker Desktop.
3. Esperar a que el motor termine de arrancar.
4. Ejecutar:

```powershell
docker version
```

### Docker Compose no está disponible

```powershell
docker compose version
```

### Python no está disponible

```powershell
python --version
```

Instalar Python 3 y agregarlo al `PATH` si es necesario.

### Git no está disponible

```powershell
git --version
```

El script usa Git para registrar el commit exacto utilizado durante la medición.

### La API no alcanza el estado `UP`

```powershell
docker compose ps
docker compose logs api
docker compose logs db
```

Verificar también que los puertos `8080` y `5432` no estén ocupados.

### PowerShell bloquea el script

Usar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

---

## 18. Qué mostrar durante una sustentación

Si el profesor pide demostrar la reproducibilidad, mostrar en este orden:

```text
1. GUIA-EJECUCION.md
2. scripts/run-messaging-baseline.ps1
3. experimentos/medicion-escenario-01/seed-mensajeria.sql
4. experimentos/medicion-escenario-01/carga-mensajeria.js
5. experimentos/medicion-escenario-01/resultados/resultado.json
6. docs/architecture/walking-skeleton.md
7. experimentos/walking-skeleton/resultados/ultima-ejecucion.json
8. docs/architecture/failure-modes-walking-skeleton.md
9. scripts/check_architecture.py
```

Después ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

Durante la ejecución el script mostrará las siete etapas y, al finalizar, indicará la ruta del `resultado.json`.

---

## 19. Explicación corta para el profesor

> “La línea base está automatizada para que no dependa de pasos manuales. Desde la raíz ejecutamos `scripts/run-messaging-baseline.ps1`. El script verifica Docker, levanta PostgreSQL y la API, carga una semilla reproducible de mensajería, captura el contexto de la máquina y del commit, ejecuta cuatro corridas con k6 —la primera como calentamiento— y finalmente calcula la mediana de los P95 válidos. Toda la evidencia queda en `experimentos/medicion-escenario-01/resultados/`.”

---

## 20. Resumen de un solo comando

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1
```

Resultado principal:

```text
experimentos/medicion-escenario-01/resultados/resultado.json
```

Ese comando es el **punto de entrada único** para reproducir la línea base de mensajería.

---

## 21. Comprobar el walking skeleton

El walking skeleton valida una rebanada vertical funcional diferente a la
medición de rendimiento. Recorre Docker, PostgreSQL, Flyway, salud de la API,
autenticación, oferta, chat, envío/lectura del mensaje, persistencia SQL y
rechazo de una ruta protegida sin token.

Desde la raíz:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1
```

Un resultado correcto termina con `WALKING SKELETON: SOPORTADO`. El JSON genera
un `message_marker`; para cerrar la comprobación del cliente Android se inicia
sesión como estudiante y se busca exactamente ese marcador en el chat indicado.

Evidencia:

```text
docs/architecture/walking-skeleton.md
docs/architecture/failure-modes-walking-skeleton.md
experimentos/walking-skeleton/resultados/ultima-ejecucion.json
experimentos/walking-skeleton/resultados/checkpoint-android.md
experimentos/walking-skeleton/resultados/checkpoint-android.png
```

## 22. Comprobar la restricción arquitectónica de S8

La primera protección ejecutable impide que los controladores del backend
accedan directamente a JDBC o SQL:

```powershell
python .\scripts\check_architecture.py
```

La salida esperada es `ADR-001 OK`. La misma comprobación se ejecuta en GitHub
Actions antes de las pruebas del backend. Su alcance y la demostración de una
violación controlada se documentan en:

```text
docs/adr/ADR-001-limites-modulo-mensajeria.md
docs/architecture/evidencia-restriccion-ci.md
```

## 23. Elegir el comando correcto

| Objetivo | Comando | Evidencia principal |
|---|---|---|
| Levantar solamente API y PostgreSQL | `docker compose up --build -d` | `docker compose ps` y `/actuator/health` |
| Probar el recorrido funcional completo | `powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1` | `experimentos/walking-skeleton/resultados/ultima-ejecucion.json` |
| Repetir la línea base de rendimiento | `powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-baseline.ps1` | `experimentos/medicion-escenario-01/resultados/resultado.json` |
| Verificar el límite de ADR-001 | `python .\scripts\check_architecture.py` | salida local y job `backend` de CI |
