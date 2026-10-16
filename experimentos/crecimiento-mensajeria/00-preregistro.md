# Prerregistro S11 — crecimiento de Mensajería y mesa de ayuda

Estado: **protocolo congelado antes de medir**.

Este experimento reejecuta la medición `MSG-PERF-01` del Módulo 2 con carga
aumentada. No usa el endpoint del Spike 1 de S10 y no cambia silenciosamente la
operación previamente medida.

## Evidencia anterior que se replica

- Operación: `GET /api/chats/{chatId}/messages?limit=50&offset=0`.
- Conversación extrema: 100.000 mensajes.
- Semilla completa: 1.000 conversaciones y 289.000 mensajes.
- Cuenta: participante autenticado de la conversación.
- Instrumento: `grafana/k6:0.54.0` mediante Docker Compose.
- Línea base M2: 10 VU, 40 solicitudes por corrida, cuatro corridas y primera
  corrida descartada como calentamiento.
- Umbral histórico: `p95 <= 500 ms`, cero fallos HTTP y página válida de 50
  mensajes.

La evidencia histórica está en `../medicion-escenario-01/` y no se modifica.

## Pregunta experimental

¿En qué escalón se degrada primero la consulta de los últimos 50 mensajes al
aumentar progresivamente el número de clientes concurrentes, y la primera
limitación observada pertenece a la API/PostgreSQL, al generador k6 o al equipo
compartido?

## Hipótesis falsable

**H1.** La operación mantendrá en todos los escalones hasta 200 VU un
`p95 <= 500 ms`, tasa de fallos HTTP igual a cero y páginas válidas de 50
mensajes.

**H0.** Habrá degradación sostenida en al menos un escalón antes o en 200 VU.

Se considera degradación sostenida cuando **dos o más de las tres corridas
válidas** de un escalón presentan al menos una de estas señales:

1. `p95 > 500 ms`;
2. alguna respuesta HTTP fallida;
3. alguna respuesta distinta de HTTP 200;
4. alguna página que no contenga exactamente 50 mensajes.

Una señal aislada en una sola corrida se registra, pero no se declara por sí
sola como frontera sostenida.

## Variable y escalones congelados

La única variable independiente es el **factor de carga**, expresado como
número de usuarios virtuales concurrentes. Cada VU conserva cuatro solicitudes
por corrida; el total es una consecuencia directa del factor de carga.

| Escalón | VU | Solicitudes por VU | Solicitudes totales por corrida |
| --- | ---: | ---: | ---: |
| Control M2 | 10 | 4 | 40 |
| Crecimiento 1 | 50 | 4 | 200 |
| Crecimiento 2 | 100 | 4 | 400 |
| Crecimiento 3 | 150 | 4 | 600 |
| Crecimiento 4 | 200 | 4 | 800 |

En cada escalón se ejecutan cuatro corridas independientes. La primera es
calentamiento y se descarta; las corridas 2, 3 y 4 son válidas. No se salta un
escalón aunque uno anterior incumpla el umbral.

El executor continúa siendo `shared-iterations`, como en M2. Este protocolo
mide una ráfaga concurrente local; no afirma representar tráfico de Internet ni
una distribución real de usuarios durante horas.

## Variables controladas

- mismo endpoint, conversación, cuenta y tamaño de página;
- misma semilla determinista y distribución 900/90/9/1;
- misma imagen de k6 y PostgreSQL 16.14;
- API, PostgreSQL y k6 en Docker Desktop sobre el mismo equipo;
- equipo conectado a la misma condición de energía durante toda la prueba;
- mismo commit para todas las corridas;
- API saludable y base saludable antes de medir;
- sin cambios de código, configuración o táctica entre escalones.

## Métricas por corrida y escalón

- solicitudes esperadas y ejecutadas;
- throughput observado en solicitudes por segundo;
- latencia mínima, media, p50, p90, p95, p99 y máxima;
- tasa de checks y de páginas válidas;
- tasa de fallos HTTP;
- conteos HTTP 200, 401, 403, 404, 429, 5xx y otros;
- duración de iteración;
- CPU y memoria observadas para k6, API y PostgreSQL mientras corre la carga;
- condición de máquina, energía, versión, rama y commit.

## Separación entre sistema e instrumento

La clasificación se realizará después de medir:

- **posible límite de API/DB:** suben latencia o errores y API/DB muestran
  presión, mientras k6 conserva capacidad;
- **posible límite del generador:** el throughput deja de crecer o k6 consume
  sus recursos antes que API/DB, sin una degradación equivalente del servidor;
- **posible límite del equipo compartido:** generador, API y DB se presionan al
  mismo tiempo; en ese caso no se atribuirá el límite a un componente aislado;
- **sin límite observado:** no aparece degradación sostenida hasta 200 VU. No se
  extrapolará más allá de esa frontera.

La telemetría de Docker es evidencia de apoyo, no reemplaza las métricas HTTP.

## Condiciones de invalidación

Una corrida no se acepta si:

1. no ejecuta exactamente el número esperado de solicitudes;
2. Docker reinicia API o PostgreSQL durante el escalón;
3. la API o la base dejan de estar saludables antes de comenzar;
4. la semilla no coincide con 1.000 conversaciones y 289.000 mensajes;
5. la conversación extrema no contiene 100.000 mensajes;
6. cambia el commit, el endpoint, la página, la cuenta o la configuración;
7. cambia la condición de energía durante el experimento;
8. el árbol Git no estaba limpio al iniciar;
9. faltan el resumen k6 o la telemetría de una corrida.

Las respuestas 429/5xx y las páginas inválidas son resultados de degradación,
no causas automáticas de invalidación, siempre que el instrumento complete las
solicitudes esperadas.

## Táctica y comparación antes/después

La táctica **no se selecciona antes de observar los resultados iniciales**. Se
elegirá una sola táctica que responda al primer fallo respaldado por datos. La
matriz de resiliencia conservará las alternativas rechazadas y sus razones.

Después de implementar la táctica se repetirá, sin cambiar el instrumento, el
escalón inmediatamente anterior a la degradación y el primer escalón degradado.
Si no aparece degradación hasta 200 VU, se medirá antes/después en 200 VU y se
declarará esa limitación interpretativa.

## Secuencia Git obligatoria

```text
protocolo + instrumento → commit → experimento → resultados → análisis
→ táctica → medición antes/después → ADR 4 → mini-comité → veredicto
```

El veredicto del mini-comité no se anticipará ni se atribuirá a una persona que
no lo haya emitido.
