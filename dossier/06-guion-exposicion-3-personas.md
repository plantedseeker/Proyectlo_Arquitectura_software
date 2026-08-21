# Guion de exposición para tres personas

Duración sugerida: **12 a 15 minutos**, más preguntas. La exposición debe
contar una historia de decisión arquitectónica respaldada con evidencia; no
debe consistir en leer archivos o enumerar tecnologías.

## Idea central que deben defender

> UTrabajo es un sistema Android existente cuya categoría fue confirmada como
> Mensajería y mesa de ayuda. Se separó el cliente de la persistencia mediante
> una API y PostgreSQL 16, se diseñó una semilla con conversaciones de tamaños
> extremos y se comprobó, bajo condiciones locales declaradas, que consultar
> los últimos 50 mensajes de una conversación de 100.000 mantiene un p95 muy
> inferior al umbral prerregistrado de 500 ms.

No digan que el experimento demuestra el rendimiento de toda la aplicación o
de un despliegue por Internet. La evidencia cubre el trayecto **k6 → API →
PostgreSQL**, con los tres componentes en el mismo equipo físico.

## Completar antes de presentar

Reemplacen estos espacios durante el ensayo:

| Rol | Nombre | Cuenta de GitHub | PR propio fusionado |
| --- | --- | --- | --- |
| Persona 1 | `[NOMBRE]` | `plantedseeker` | PR #1 |
| Persona 2 | `[NOMBRE]` | `[CUENTA]` | `[URL DEL PR]` |
| Persona 3 | `[NOMBRE]` | `[CUENTA]` | `[URL DEL PR]` |

También deben completar dos evidencias que todavía no están en el repositorio:

1. anexar la captura o mensaje original en que el profesor confirma
   **Mensajería y mesa de ayuda**;
2. consultar la guía y escribir la letra A/B/C que corresponde a **sistema
   propio existente**. No deben inventar la letra durante la exposición.

El checklist exige al menos un PR fusionado por integrante. Como el equipo tiene
tres personas, el PR #1 solo demuestra la contribución de `plantedseeker`; se
necesitan dos PR adicionales realizados desde las cuentas reales de los otros
integrantes.

## Cómo encontrar rápidamente cada evidencia

En GitHub se navega desde la raíz del repositorio. En Android Studio pueden usar
`Ctrl+Shift+N` y escribir el nombre del archivo.

| Tema | Ruta desde la raíz | Qué demuestra |
| --- | --- | --- |
| Visión general y arranque | `README.md` | Sistema, arquitectura y reproducción |
| Contexto y categoría | `dossier/01-contexto-sistema.md` | Actores, alcance y sistema base |
| Stakeholders, drivers y riesgos | `dossier/02-stakeholders-drivers.md` | Razones de las decisiones |
| Atributos y escenarios | `dossier/03-atributos-calidad.md` | RNF medibles y priorizados |
| Hipótesis, método y contraste | `dossier/04-escenarios-calidad.md` | Prerregistro y resultado S4 |
| Estado del checklist | `dossier/05-checklist-semanas-1-4.md` | Trazabilidad S1–S4 |
| Endpoint paginado | `backend/src/main/kotlin/com/tab/utrabajo/api/controller/ChatController.kt` | `limit=50` y `offset=0` por defecto |
| Consulta y autorización | `backend/src/main/kotlin/com/tab/utrabajo/api/service/UTrabajoService.kt` | Participación, límites y orden estable |
| Índice de mensajes | `backend/src/main/resources/db/migration/V2__message_recent_index.sql` | Decisión de rendimiento en PostgreSQL |
| Servicios locales | `docker-compose.yml` | PostgreSQL, API y k6 reproducibles |
| Semilla extrema | `experimentos/medicion-escenario-01/seed-mensajeria.sql` | Distribución 900/90/9/1 |
| Carga k6 | `experimentos/medicion-escenario-01/carga-mensajeria.js` | Solicitudes, VU, checks y umbral |
| Resultado agregado | `experimentos/medicion-escenario-01/resultados/resultado.json` | Mediana, condiciones y decisión |
| Conteo real de la semilla | `experimentos/medicion-escenario-01/resultados/verificacion-semilla.json` | 1.000 chats y 289.000 mensajes |
| Integridad de resultados | `experimentos/medicion-escenario-01/resultados/SHA256SUMS.txt` | Huellas de los archivos auditados |
| Pruebas automatizadas | `.github/workflows/ci.yml` | Android, backend e instrumento en CI |

## Preparación de la demostración

Antes de entrar al salón:

1. abrir Docker Desktop;
2. ejecutar `docker compose up --build -d`;
3. comprobar `docker compose ps` y verificar que `db` y `api` estén saludables;
4. comprobar `http://localhost:8080/actuator/health`;
5. abrir el repositorio en GitHub, el PR #1 y la última corrida verde de CI;
6. dejar abiertos los archivos `01`, `02`, `03`, `04` y `resultado.json`;
7. tener una copia local de la evidencia por si falla Internet.

No ejecuten nuevamente la semilla de 289.000 mensajes durante la exposición. La
medición completa consume tiempo y ya está conservada con hashes. Si el profesor
pide reproducibilidad, muestren el comando y los archivos, y ofrezcan ejecutarlo
fuera del tiempo de presentación.

---

# Persona 1 — Problema, categoría y arquitectura

Tiempo sugerido: **4 minutos**.

## 1. Apertura — 30 segundos

### Mostrar

`README.md` y el nombre del repositorio.

### Decir

> Buenos días. Nuestro sistema es UTrabajo, una aplicación Android que conecta
> estudiantes y empresas para publicar ofertas, postularse y conversar. Para
> estas semanas no construimos un sistema vacío: adoptamos nuestro proyecto
> existente, reemplazamos la persistencia anterior por una arquitectura local
> con API y PostgreSQL 16, y trabajamos la categoría confirmada por el profesor:
> Mensajería y mesa de ayuda.

### Argumentar

- El chat ya pertenece al dominio real de UTrabajo.
- La categoría no se eligió solo porque la aplicación tenga una pantalla móvil.
- El fenómeno relevante son conversaciones con pocos o muchísimos mensajes.

No digan que adjuntar CV convierte el sistema en gestión documental. Tampoco
presenten ofertas como el experimento vigente: esas mediciones quedaron como
antecedentes históricos.

## 2. Actores y alcance — 60 segundos

### Mostrar

`dossier/01-contexto-sistema.md`, secciones **Actores y flujos** y **Contexto y
límites**.

### Decir

> Los dos actores de negocio principales son el estudiante y la empresa. El
> estudiante necesita consultar mensajes recientes de forma rápida y privada;
> la empresa necesita atender varias conversaciones sin mezclar candidatos. El
> profesor actúa como auditor de reproducibilidad y el equipo mantiene el
> sistema. La entrega es local y usa exclusivamente datos sintéticos.

### Argumentar

- Los stakeholders conducen los atributos de calidad.
- La privacidad exige autorización por participación en cada chat.
- El alcance local no pretende representar Internet, alta disponibilidad ni
  renderizado móvil.

## 3. Arquitectura — 90 segundos

### Mostrar

El diagrama de `dossier/01-contexto-sistema.md` y luego `docker-compose.yml`.

### Decir

> La aplicación Android consume una API REST mediante HTTP y sesión Bearer. La
> API contiene las reglas, valida al usuario y se conecta por JDBC a PostgreSQL
> 16. Android nunca conoce las credenciales de la base. Docker Compose levanta
> PostgreSQL y la API con versiones reproducibles. Esta separación también nos
> permite migrar más adelante a HTTPS y una base administrada sin reescribir
> todas las pantallas.

### Argumentar

- PostgreSQL aporta integridad relacional, restricciones, índices y migraciones
  Flyway.
- La API evita exponer la base al dispositivo.
- Docker hace que otro computador reconstruya el esquema; el volumen de cada
  computador es local y no se versiona en GitHub.

Si preguntan por otro computador:

> Cada equipo clona el código y ejecuta `docker compose up --build -d`. Flyway
> crea el mismo esquema y la semilla demo. Los datos creados durante el uso no
> se comparten; para eso se necesitaría un despliegue central futuro.

## 4. Requisito y transición — 60 segundos

### Mostrar

`dossier/05-checklist-semanas-1-4.md`, filas de semanas 1 y 2.

### Decir

> Para semanas 1 y 2 dejamos el contexto, las instrucciones reproducibles, los
> stakeholders, las restricciones, el inventario de riesgos y la CI. El sistema
> y sus pruebas se ejecutan con PostgreSQL 16. La letra exacta de la opción A/B/C
> se registra a partir de la guía; materialmente usamos un sistema propio
> existente.

### Transición

> Con el contexto delimitado, mi compañero explicará cómo convertimos las
> preocupaciones de los actores en drivers, riesgos y decisiones técnicas.

---

# Persona 2 — Drivers, riesgos y solución técnica

Tiempo sugerido: **4 minutos**.

## 1. Drivers priorizados — 60 segundos

### Mostrar

`dossier/02-stakeholders-drivers.md`, tabla **Drivers preliminares priorizados**.

### Decir

> Priorizamos cinco drivers. El primero es rendimiento al abrir conversaciones
> extremas; luego confidencialidad, orden e integridad, modificabilidad del
> cliente y reproducibilidad. La prioridad no provino de una preferencia por una
> tecnología: provino del impacto sobre estudiantes, empresas y auditoría.

### Argumentar

- Rendimiento lleva a paginar y crear un índice.
- Confidencialidad lleva a comprobar que el usuario participa en el chat.
- Integridad lleva a PK, FK y orden estable.
- Modificabilidad lleva a parámetros opcionales compatibles con Android.
- Reproducibilidad lleva a Docker, k6 con versión fija y semilla determinista.

## 2. Evaluación crítica de riesgos de IA — 60 segundos

### Mostrar

`dossier/02-stakeholders-drivers.md`, tabla R-01 a R-10.

### Decir

> No aceptamos automáticamente los riesgos sugeridos por IA. Los clasificamos
> como válidos, genéricos, irrelevantes o falsos. Por ejemplo, devolver todos los
> mensajes era un riesgo válido; decir que Android se conectaba directamente a
> PostgreSQL era falso; y Kubernetes multirregión era irrelevante para una
> entrega local sin presupuesto de nube.

### Argumentar

El valor no está en que la IA haya producido una lista, sino en el juicio del
equipo y en la acción trazable que resulta de cada riesgo.

## 3. Escenarios medibles — 60 segundos

### Mostrar

`dossier/03-atributos-calidad.md`, escenario `MSG-PERF-01` y mapa
**atributo–decisión**.

### Decir

> Reformulamos frases ambiguas como “el chat debe ser rápido” usando fuente,
> estímulo, artefacto, entorno, respuesta y medida. El escenario principal pide
> a un estudiante autenticado los últimos 50 mensajes de una conversación con
> 100.000 mensajes, con 10 usuarios virtuales y un p95 máximo de 500 ms. También
> definimos escenarios de seguridad, orden y modificabilidad.

### Argumentar

- Un RNF sin carga ni umbral no puede comprobarse.
- El escenario de seguridad espera HTTP 403 y cero filas para un tercero.
- El escenario de orden comprueba fechas e identificadores sin duplicados.

## 4. Código que responde a los drivers — 90 segundos

### Mostrar en este orden

1. `ChatController.kt`, método `/{chatId}/messages`;
2. `UTrabajoService.kt`, método `messages`;
3. `V2__message_recent_index.sql`.

### Decir

> El endpoint recibe `limit` y `offset`; por defecto devuelve 50 mensajes y no
> permite más de 100. Antes de consultar llama a `requireChatParticipant`. La
> consulta selecciona primero los mensajes recientes en orden descendente y
> después los entrega cronológicamente a la interfaz. PostgreSQL usa un índice
> compuesto por `chat_id`, `sent_at DESC` e `id DESC`.

### Argumentar

- La paginación evita transferir y mantener 100.000 mensajes en memoria.
- `chat_id` encabeza el índice porque la consulta siempre pertenece a una
  conversación.
- Fecha e ID producen un orden determinista incluso cuando dos mensajes tienen
  tiempos cercanos.
- Se conserva `offset` por simplicidad y compatibilidad; si se requieren páginas
  profundas, se reevaluaría un cursor por fecha e ID.

No afirmen que el índice garantiza por sí solo cualquier escala. La conclusión
solo se aplica al volumen y condiciones medidos.

### Transición

> Ya mostramos por qué tomamos estas decisiones y dónde están en el código. Mi
> compañero presentará cómo las sometimos a una medición prerregistrada y qué
> resultado obtuvimos.

---

# Persona 3 — Experimento, resultados y decisión

Tiempo sugerido: **4 a 5 minutos**.

## 1. Hipótesis sellada antes de medir — 60 segundos

### Mostrar

`dossier/04-escenarios-calidad.md`, encabezado y sección **Hipótesis previa
MSG-PERF-01**.

### Decir

> Antes de medir registramos la hipótesis: con 1.000 conversaciones sintéticas,
> una de ellas con 100.000 mensajes, 40 solicitudes y 10 usuarios virtuales,
> cada corrida válida debía conservar un p95 menor o igual a 500 ms y no tener
> respuestas inválidas. Definimos cuatro corridas, descartando la primera como
> calentamiento, y la mediana de los p95 de las tres restantes. El prerregistro
> quedó sellado en el commit `e7e84d9`.

### Argumentar

- Descartar la primera corrida fue una regla previa, no selección posterior.
- El commit anterior a los resultados evita ajustar el umbral para “hacerlo
  pasar”.
- `p95` indica que el 95 % de las observaciones no excede esa latencia.
- La mediana resume las tres corridas válidas y reduce el efecto de una corrida
  atípica.

## 2. Semilla e instrumento — 60 segundos

### Mostrar

`seed-mensajeria.sql`, `carga-mensajeria.js` y
`resultados/verificacion-semilla.json`.

### Decir

> La semilla no es uniforme: contiene 900 conversaciones de 10 mensajes, 90 de
> 1.000, 9 de 10.000 y una de 100.000; en total son 289.000 mensajes. Esta
> distribución reproduce el extremo exigido por la categoría. k6 consulta la
> página más reciente de 50 mensajes, comprueba HTTP 200 y exactamente 50
> elementos, y registra latencia y fallos.

### Argumentar

- Los datos son sintéticos, deterministas y no contienen chats personales.
- `verificacion-semilla.json` no repite solo valores esperados: conserva el
  conteo obtenido directamente de PostgreSQL.
- k6 corresponde al escenario porque genera carga HTTP sobre la operación de
  mensajería definida; no se presenta como medición de la interfaz Android.

## 3. Condiciones de la medición — 60 segundos

### Mostrar

`resultados/resultado.json`, objeto `conditions`.

### Decir

> Registramos el equipo Acer Predator PH16-71, procesador Intel i9-13900HX,
> 95,7 GiB de RAM y 32 CPU lógicas. El equipo estuvo conectado a corriente,
> batería al 80 % y plan Equilibrado antes y después. k6, la API y PostgreSQL
> 16.14 compartieron Docker Desktop y el mismo equipo físico. La herramienta fue
> `grafana/k6:0.54.0`.

### Argumentar

- Compartir equipo introduce competencia local por recursos.
- A la vez elimina latencia de Internet; por eso no se generaliza a producción.
- Registrar energía y máquina permite comparar una futura repetición en
  condiciones conocidas.

## 4. Resultado y contraste — 90 segundos

### Mostrar

La tabla de `dossier/04-escenarios-calidad.md` o `resultado.json`, secciones
`valid_runs_summary` y `runs`.

### Decir

> La corrida 1 fue calentamiento y obtuvo 83,553 ms. Las corridas válidas
> obtuvieron p95 de 9,451, 8,265 y 9,109 ms. Su mediana fue 9,109 ms. Todas las
> solicitudes válidas devolvieron 50 mensajes, con 100 % de checks y 0 % de
> fallos. Como las tres corridas quedaron por debajo de 500 ms, el resultado
> respalda H1 bajo las condiciones declaradas.

### Argumentar

No digan “H1 quedó probada universalmente”. Digan:

> El dato respalda H1 para esta revisión, semilla, equipo, carga y topología.

La decisión actual es conservar página de 50 e índice compuesto. Debe reabrirse
si aparecen conversaciones mayores, SLA menor de 250 ms, múltiples nodos,
Internet real, páginas profundas o entrega mediante WebSocket.

## 5. Reproducibilidad y cierre — 45 segundos

### Mostrar

El PR #1, la última corrida de GitHub Actions y
`dossier/05-checklist-semanas-1-4.md`.

### Decir

> La evidencia no depende solo de nuestra explicación. El PR conserva los
> cambios, GitHub Actions aprobó Android, backend e instrumento, el script
> `run-messaging-baseline.ps1` reproduce la medición y las huellas SHA-256
> protegen los archivos auditados. Así conectamos requisito, decisión, código,
> medición y resultado.

### Cierre del equipo

> En conclusión, no cambiamos toda UTrabajo para forzar una categoría. Tomamos
> el flujo real de conversación, identificamos el riesgo de volumen, aplicamos
> una decisión pequeña y trazable y la contrastamos con datos. La solución es
> suficiente para la línea base local y deja explícitas las condiciones que
> obligarían a rediseñarla.

---

# Demostración corta de dos minutos

Si el profesor permite una demostración, realicen únicamente esta secuencia:

1. Persona 1 muestra Docker Desktop o ejecuta:

   ```powershell
   docker compose ps
   Invoke-RestMethod http://localhost:8080/actuator/health
   ```

   Debe observarse la API `UP` y PostgreSQL saludable.

2. Persona 2 abre `ChatController.kt`, `UTrabajoService.kt` y el índice, sin
   desplazarse por otros archivos.

3. Persona 3 abre `resultado.json`, señala `p95_median_ms: 9.109`,
   `shared_physical_machine: true` y `decision: supported`, y luego muestra la
   CI verde.

Si Docker falla, no improvisen una nueva medición. Muestren la evidencia
versionada, los hashes, el PR y la CI; expliquen que el procedimiento completo
es reproducible con el script.

# Preguntas probables y quién responde

| Pregunta | Responsable | Respuesta breve |
| --- | --- | --- |
| ¿Por qué mensajería? | Persona 1 | Es la categoría confirmada y el dominio ya contiene chats; la semilla reproduce conversaciones extremas |
| ¿Por qué PostgreSQL? | Persona 1 | Integridad relacional, restricciones, índices, migraciones y evolución robusta |
| ¿Por qué una API? | Persona 1 o 2 | Evita credenciales DB en Android y centraliza autorización y reglas |
| ¿Qué decisión arquitectónica evaluaron? | Persona 2 | Página reciente de 50 más índice compuesto por chat, fecha e ID |
| ¿Cómo evitan leer chats ajenos? | Persona 2 | `requireChatParticipant` se ejecuta antes de consultar o escribir |
| ¿Por qué no WebSocket? | Persona 2 | La fecha y alcance local priorizaron lectura reciente; tiempo real reabre la decisión futura |
| ¿Por qué descartaron la primera corrida? | Persona 3 | Fue calentamiento definido antes de medir, no selección posterior |
| ¿Qué es p95? | Persona 3 | Percentil bajo el cual queda el 95 % de las latencias observadas |
| ¿Por qué usan mediana? | Persona 3 | Resume las tres corridas válidas con menor sensibilidad a un valor atípico |
| ¿Los 9,109 ms incluyen Internet y pantalla Android? | Persona 3 | No; cubren k6, API y PostgreSQL en el mismo equipo físico |
| ¿La hipótesis quedó demostrada? | Persona 3 | Quedó respaldada únicamente bajo las condiciones medidas |
| ¿Cómo se reproduce en otro PC? | Persona 1 | Clonar, abrir Docker, ejecutar Compose; Flyway reconstruye el esquema local |
| ¿Por qué datos sintéticos? | Persona 2 | Son reproducibles y evitan exponer conversaciones o información personal |
| ¿Qué haría cambiar la decisión? | Persona 3 | Más volumen, SLA más estricto, Internet, varios nodos, páginas profundas o tiempo real |

# Errores que deben evitar

- No decir que Firebase y PostgreSQL se usan simultáneamente: la arquitectura
  evaluada usa PostgreSQL 16.
- No presentar la categoría como “aplicación móvil”; la confirmada es
  Mensajería y mesa de ayuda.
- No presentar la antigua medición de ofertas como resultado principal.
- No decir que la primera corrida se eliminó porque era lenta; se descartó por
  calentamiento según el prerregistro.
- No confundir el p95 de una corrida con la mediana de los tres p95 válidos.
- No afirmar que 9,109 ms representa la experiencia completa del usuario.
- No ocultar que generador, API y base compartieron el mismo equipo.
- No afirmar que la base de cada desarrollador se sincroniza por GitHub.
- No inventar la letra A/B/C ni PR de integrantes que no hayan contribuido.
- No leer tablas completas; mostrar una evidencia y explicar por qué conduce a
  una decisión.

# Ensayo recomendado

Hagan dos ensayos:

1. **Ensayo técnico:** cada persona debe localizar sus archivos sin ayuda y
   responder las preguntas asignadas.
2. **Ensayo cronometrado:** máximo 4 minutos para personas 1 y 2, y 5 minutos
   para persona 3. Si exceden el tiempo, reduzcan ejemplos, no las condiciones ni
   las limitaciones del resultado.

Al terminar, intercambien una pregunta: la persona 1 pregunta por el índice a la
persona 2; la persona 2 pregunta por la mediana a la persona 3; y la persona 3
pregunta por el alcance a la persona 1. Así evitan que cada integrante conozca
solo su fragmento.

# PR reales pendientes para un equipo de tres

Los dos integrantes todavía no identificados deben realizar contribuciones
reales desde sus propias cuentas. Dos opciones útiles son:

- **Integrante 2:** reproducir el arranque en el segundo computador y agregar un
  registro `docs/reproduccion/equipo-2.md` con fecha, equipo, versiones, comandos
  ejecutados, salud y pruebas realmente observadas.
- **Integrante 3:** ejecutar la compilación/pruebas Android o revisar la evidencia
  de categoría y opción A/B/C, y agregar un registro verificable en
  `docs/reproduccion/equipo-3.md` o el artefacto de confirmación correspondiente.

Cada uno debe crear su rama, commit y PR. Un flujo posible es:

```powershell
git switch main
git pull --ff-only
git switch -c docs/reproduccion-nombre
# Crear y revisar el archivo con datos observados realmente.
git add docs/reproduccion/
git commit -m "docs: registrar reproduccion de nombre"
git push -u origin docs/reproduccion-nombre
```

Después deben abrir el PR en GitHub, esperar la CI y fusionarlo. No usen la
cuenta de `plantedseeker` para simular las otras contribuciones.
