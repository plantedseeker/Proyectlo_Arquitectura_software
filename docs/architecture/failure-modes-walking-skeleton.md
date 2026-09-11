# Modos de fallo del walking skeleton

Prioridad: **P0** bloquea el recorrido o puede comprometer datos/seguridad;
**P1** degrada una función principal; **P2** afecta operación o evolución.

| ID | Prioridad | Qué puede romperse | Señal observable | Control/solución | Recuperación segura | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| F01 | P0 | Docker Desktop detenido o virtualización deshabilitada | `docker version` no obtiene servidor | Preflight antes de construir | Iniciar Docker; si no arranca, habilitar WSL2/virtualización y reiniciar | Validado por el script |
| F02 | P0 | Puertos 5432 u 8080 ocupados | Preflight identifica el contenedor o Compose falla con `address already in use` | El script consulta quién publica ambos puertos; complementar con `Get-NetTCPConnection` si es un proceso nativo | Detener de forma controlada el proyecto identificado o cambiar el mapeo y la URL del cliente coordinadamente | Detección automática; recuperación manual |
| F03 | P0 | PostgreSQL no inicia o las credenciales no coinciden | `pg_isready` falla; salud API `DOWN`; errores JDBC | Healthcheck de DB, `depends_on`, variables `DB_*` consistentes | Corregir variables; `docker compose restart db api`; nunca borrar un volumen con datos reales | Implementado |
| F04 | P0 | Migración Flyway inválida o incompatible | API no arranca; log `FlywaySqlScriptException`; historial con fallo | Migraciones versionadas y verificación del historial | Corregir con una migración nueva. `down -v` solo es aceptable para datos sintéticos descartables | Implementado y comprobado |
| F05 | P0 | Volumen local obsoleto, corrupto o lleno | Fallos de migración/escritura; mensajes no persisten | Volúmenes nombrados y comprobación SQL del mensaje | Respaldar primero; revisar espacio; restaurar copia o migrar. No usar `down -v` como arreglo de producción | Parcial; respaldo pendiente |
| F06 | P0 | API caída o en reinicio continuo | Salud sin `UP`; HTTP 5xx/conexión rechazada | Healthcheck, política `restart`, logs automáticos al fallar el esqueleto | `docker compose logs api`; corregir causa; `docker compose restart api`; repetir recorrido | Implementado |
| F07 | P1 | Semilla demo deshabilitada o incompleta | Login 401 o lista de ofertas vacía | `SEED_DEMO=true` local; semilla idempotente; aserciones del script | Corregir variable y reiniciar API; verificar logs | Implementado para entorno local |
| F08 | P0 | Token ausente, inválido o expirado | 401/403 en rutas protegidas | Spring Security y prueba negativa del script; prueba de participante ajeno en backend | Volver a iniciar sesión; limpiar sesión local inválida | Servidor protegido; redirección Android ante 401 pendiente |
| F09 | P1 | Android apunta a la dirección equivocada | `Failed to connect`, aunque salud en PC sea `UP` | `10.0.2.2` para emulador; propiedad `API_BASE_URL` | Emulador: usar `10.0.2.2`. Dispositivo: `adb reverse tcp:8080 tcp:8080` y URL localhost, o HTTPS accesible | Documentado; checkpoint manual |
| F10 | P0 | Un usuario consulta un chat ajeno | Exposición de mensajes o respuesta distinta de 403 | `requireChatParticipant` y prueba de integración con usuario externo | Bloquear por participante en cada operación; revisar auditoría antes de Internet | Implementado y probado |
| F11 | P1 | Offset profundo en conversaciones grandes | p95 aumenta y se incumple el umbral | Evidencia S7 con `EXPLAIN`; índice reciente existente | Sustituir offset profundo por cursor/keyset según ADR; repetir línea base | Causa localizada; cambio pendiente |
| F12 | P1 | Polling móvil de 4 s multiplica solicitudes al crecer usuarios | Saturación de API/DB, batería y red; 429/5xx | Cancelar polling al salir de pantalla; medir concurrencia | Aplicar backoff y límites; evaluar SSE/WebSocket cuando el volumen lo justifique | Riesgo identificado; protección completa pendiente |
| F13 | P1 | Mensaje se guarda pero la UI no lo muestra | JSON/SQL contienen marcador; Android no | Checkpoint con el mismo `message_marker`; trazabilidad Retrofit -> pantalla | Revisar URL, sesión, mapeo JSON, ciclo de polling y logs del cliente | Checkpoint definido |
| F14 | P0 | Configuración local se publica en Internet | HTTP, secretos conocidos, CORS o archivos expuestos | Mantener alcance local; no reutilizar credenciales demo | Antes de publicar: HTTPS, secretos externos, CORS restrictivo, base administrada y almacenamiento de objetos | Fuera del alcance actual; obligatorio para migración |

## Orden de diagnóstico

Cuando el recorrido falle, revisar en este orden evita confundir síntomas con
causas:

1. Motor: `docker version`.
2. Despliegue: `docker compose ps`.
3. Base: `docker compose exec -T db pg_isready -U utrabajo -d utrabajo`.
4. API: `Invoke-RestMethod http://localhost:8080/actuator/health`.
5. Logs: `docker compose logs --no-color --tail 100 api db`.
6. Contrato: ejecutar el walking skeleton y localizar el primer `FAIL`.
7. Cliente: confirmar `API_BASE_URL`, token y marcador esperado.

## Dos fallos controlados para la demostración

Solo deben ejecutarse en el entorno local con datos sintéticos.

### Base temporalmente no disponible

```powershell
docker compose stop db
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose start db
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1 -SkipBuild
```

Resultado esperado: durante la interrupción la salud deja de ser `UP`; después
de iniciar PostgreSQL, el recorrido vuelve a `SOPORTADO` sin eliminar el
volumen.

### API temporalmente no disponible

```powershell
docker compose stop api
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose start api
powershell -ExecutionPolicy Bypass -File .\scripts\run-walking-skeleton.ps1 -SkipBuild
```

Resultado esperado: la conexión es rechazada mientras la API está detenida y se
recupera tras el arranque. No se ejecuta `docker compose down -v`, porque borrar
datos no es una estrategia de recuperación.

## Registro de resultados

No se afirma que un fallo controlado fue superado hasta guardar su salida. Para
cada ensayo, registrar fecha, revisión Git, comando, primer síntoma, tiempo de
recuperación y si hubo pérdida de datos. El JSON del walking skeleton posterior
es la evidencia mínima de recuperación funcional.
