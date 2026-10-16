# Guía de defensa — Spike 1 de integración S10

Esta guía permite que cualquiera de los tres integrantes explique el trabajo
completo sin asignar partes a una persona concreta. La fuente de verdad son los
artefactos enlazados; las capturas son apoyo visual.

## Respuesta corta de 30 segundos

Mensajería necesita comprobar una oferta y conocer su empresa para crear un
chat. Comparamos acceso directo, contrato síncrono y eventos con proyección.
Preregistramos un spike del contrato síncrono, corregimos antes de medir el
payload a solo `jobId` y `companyId`, y ejecutamos cuatro corridas k6. Descartamos
la primera como calentamiento. Las tres válidas obtuvieron p95 de 4,781, 4,360 y
4,710 ms, con 0 fallos inesperados y todos los controles 401/404 aprobados. Por
eso ADR-003 propone el contrato síncrono mínimo y difiere broker, CQRS y Event
Sourcing.

## Historia verificable

```text
Context Map: Mensajería consume Ofertas
        ↓
Contrato mínimo + comparación sync/async
        ↓
Preregistro antes del código
        ↓
Enmienda antes de medir: 4 campos → 2 campos
        ↓
Implementación y pruebas
        ↓
4 corridas k6; corrida 1 descartada
        ↓
Veredicto: hipótesis respaldada, alcance ajustada
        ↓
ADR-003: síncrono mínimo; async/CQRS/ES diferidos
```

## Comando principal y salida esperada

Desde la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-spike-01-integracion.ps1
```

La salida debe terminar con `9/9 Evidencia creada`, mostrar el commit medido y
una ruta bajo `experimentos/spike-01-integracion/resultados/`. El script exige
árbol Git limpio, levanta Docker, espera salud, selecciona datos deterministas,
ejecuta las cuatro corridas, agrega el resultado y calcula SHA-256.

## Resultado que debe mostrarse

| Corrida | Estado | p95 | Checks | HTTP inesperados |
| ---: | --- | ---: | ---: | ---: |
| 1 | calentamiento descartado | 5,779 ms | 100 % | 0 % |
| 2 | válida | 4,781 ms | 100 % | 0 % |
| 3 | válida | 4,360 ms | 100 % | 0 % |
| 4 | válida | 4,710 ms | 100 % | 0 % |

Umbral: p95 <= 500 ms en **cada** corrida válida. No se usó la mediana para
ocultar una corrida fallida.

## Qué significa “ajustada”

No significa que se movió el umbral después de ver los resultados. Antes de
medir se detectó que `active` y `title` excedían la necesidad de crear una
conversación. La enmienda dejó `jobId` y `companyId`; 200/404 expresa existencia.
Las condiciones de carga y el umbral permanecieron congelados.

La respuesta completa está en
`experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md`.

## Preguntas probables y respuestas

### ¿Por qué síncrono?

Porque la creación necesita el estado actual y el proveedor está en el mismo
monolito. El spike demostró que la consulta cumple el criterio sin duplicar
estado ni agregar infraestructura.

### ¿Por qué no eventos?

Los eventos son una alternativa válida si se necesita operar durante una caída
de Ofertas o desplegar contextos independientemente. Hoy introducirían outbox,
broker, reintentos, idempotencia y consistencia eventual sin beneficio medido.

### ¿Por qué no CQRS o Event Sourcing?

No existe presión para separar modelos de lectura/escritura ni requisito de
reconstruir el estado desde eventos. La consulta directa y mínima ya satisface
el caso bajo prueba.

### ¿El p95 de 4–5 ms es rendimiento de producción?

No. k6, API y PostgreSQL compartieron una máquina local. El dato solo respalda
el umbral y las condiciones preregistradas; no modela una red distribuida ni
carga máxima.

### ¿Por qué 500 ms?

Es el límite experimental escrito antes del código. La defensa no afirma que
sea un SLO definitivo; garantiza que el criterio no se acomodó al resultado.

### ¿El endpoint ya reemplazó el flujo real?

No. El endpoint representa y prueba el contrato. ADR-003 propone migrar el
acceso directo mediante un puerto de aplicación; dentro del mismo proceso no se
recomienda self-HTTP.

### ¿Cómo se protegió el endpoint?

El spike exige `X-Integration-Token`, compara el valor en tiempo constante y
rechaza token ausente o inválido con 401. Si la configuración está vacía, ninguna
credencial es válida. Antes de una frontera remota real debe diseñarse gestión
de secretos y autenticación de servicio productivas.

### ¿Qué errores ocurrieron y por qué no invalidan el resultado?

Durante la preparación faltaban Docker activo, la ruta del Android SDK y una
corrección lint. También Windows PowerShell interpretó progreso normal de
Compose por stderr como error. Todos ocurrieron antes de las corridas oficiales,
se corrigieron y están documentados. La medición oficial partió de árbol limpio
y registra `experiment_valid: true`.

## Capturas recomendadas durante la exposición

1. `evidencias/08-android-lint-y-build-exitosos.png` — controles previos.
2. `evidencias/09-spike-falso-error-stderr-compose.png` — fallo reproducible y aprendizaje del instrumento.
3. `evidencias/10-spike-ejecucion-exitosa.png` — cierre y ruta oficial.
4. `evidencias/11-spike-resumen-corridas.png` — métricas y checks.

Ruta base: `experimentos/spike-01-integracion/`.

## Archivos que deben poder abrirse

- `docs/dominio/context-map.md` — dirección y razón de la dependencia.
- `docs/integracion/contrato-api.yaml` — contrato exacto.
- `docs/integracion/sincrono-vs-asincrono.md` — alternativas.
- `experimentos/spike-01-integracion/00-preregistro.md` — criterio previo.
- `experimentos/spike-01-integracion/veredicto.md` — interpretación posterior.
- `experimentos/spike-01-integracion/resultados/20261016T211031Z/resultado.json` — cifras verificables.
- `docs/adr/ADR-003-integracion-contextos.md` — decisión y consecuencias.
- `dossier/17-checklist-modulo-5.md` — cobertura de la rúbrica.

## Lo que no debe exagerarse

- No decir que la prueba simula producción.
- No decir que la alternativa asíncrona quedó refutada para siempre.
- No decir que el endpoint experimental ya es el flujo productivo.
- No decir que el ADR fue aprobado por alguien que no lo ratificó.
- No ocultar que el alcance fue ajustado; explicar que ocurrió antes de medir.
