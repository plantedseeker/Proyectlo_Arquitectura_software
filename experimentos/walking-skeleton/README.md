# Walking skeleton de mensajería

Este experimento comprueba el recorrido mínimo que atraviesa los límites reales
de UTrabajo. No mide rendimiento ni reemplaza la línea base S4/S7: demuestra que
las piezas principales están conectadas y que el sistema falla de forma visible
si una de ellas deja de responder.

## Recorrido automatizado

```text
Docker Compose
  -> PostgreSQL 16 disponible
  -> Flyway sin migraciones fallidas
  -> API con salud UP
  -> login del estudiante sintético
  -> consulta de una oferta
  -> creación o recuperación del chat
  -> envío y lectura de un mensaje
  -> comprobación de la fila en PostgreSQL
  -> rechazo de /api/chats sin token
```

Desde PowerShell en la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1
```

Si las imágenes ya fueron construidas:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1 -SkipBuild
```

El script conserva los contenedores para completar el checkpoint Android. Para
detenerlos al terminar, usar `-StopAfter`; esta opción **no elimina** los
volúmenes. Cada ejecución crea o reutiliza el chat demo e inserta un mensaje
sintético con prefijo `walking-skeleton-`.

La evidencia se escribe por defecto en
`resultados/ultima-ejecucion.json`. El resultado registra versiones, revisión
Git, topología, identificadores sintéticos y cada comprobación ejecutada.

## Checkpoint manual de Android

1. Mantener Docker Desktop y los contenedores en ejecución.
2. Abrir el proyecto en Android Studio e iniciar la variante `debug` en un
   emulador Android.
3. Iniciar sesión con `estudiante@utrabajo.local` / `UTrabajo1!`.
4. Abrir **Chats**, entrar al chat recién creado y localizar exactamente el
   `message_marker` registrado en el JSON.

Ese último paso añade la pantalla Android al recorrido. En emulador, la URL
correcta es `http://10.0.2.2:8080/`; `localhost` dentro del emulador no es el
equipo anfitrión.

## Criterio de aceptación

El recorrido se declara **soportado** solamente si todos los pasos automáticos
son `PASS` y el equipo observa el mismo marcador en Android. Si el script falla,
conserva los servicios y muestra `docker compose ps` y los últimos logs para que
el fallo pueda investigarse.

Los modos de fallo, controles y recuperaciones están en
[`../../docs/architecture/failure-modes-walking-skeleton.md`](../../docs/architecture/failure-modes-walking-skeleton.md).
Los fallos que realmente se presenten se anotan en
[`registro-fallos.md`](registro-fallos.md), sin convertir supuestos en resultados.
