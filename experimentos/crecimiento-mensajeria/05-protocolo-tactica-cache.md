# Protocolo antes/después — caché de la página reciente

Estado: **táctica definida antes de medir su resultado**.

## Justificación

El crecimiento inicial no produjo errores ni incumplió `p95 <= 500 ms`; la
primera presión observable fue el aumento relativo del p95 de 8,963 ms a
58,235 ms. Por ello se prueba caché y se rechazan retry/timeout: no hubo fallos
transitorios ni esperas fuera de presupuesto que los justifiquen.

## Cambio bajo prueba

- caché local dentro de la API;
- solo para páginas con `offset=0`;
- TTL: 1.000 ms;
- máximo: 1.000 entradas;
- clave: chat, límite y offset;
- autorización de participante antes de consultar la caché;
- invalidación al enviar un mensaje;
- deshabilitada por defecto mediante `MESSAGE_CACHE_ENABLED=false`;
- endpoint interno protegido por token para medir hits/misses.

## Comparación congelada

**Antes:** escalón de 200 VU del resultado `20261016T224331Z`, medido sobre el
commit `73895be`; p95 mediano 58,235 ms y throughput mediano 2.303,017 req/s.

**Después:** cuatro corridas de 200 VU y 800 solicitudes por corrida con caché
habilitada. La primera se descarta y se usan las corridas 2, 3 y 4. Se conserva
el mismo endpoint, cuenta, semilla, página, equipo, umbral y versión de k6.

La táctica queda respaldada experimentalmente si:

1. mantiene HTTP 200, cero fallos y páginas de 50 mensajes;
2. registra hits de caché;
3. reduce el p95 mediano por debajo de 58,235 ms;
4. no reduce el throughput mediano por debajo de 2.303,017 req/s.

Si falla cualquiera de los criterios 1 o 2, la medición no respalda la táctica.
Si solo incumple 3 o 4, la táctica es funcional pero no aporta beneficio de
rendimiento en estas condiciones.

## Límites de interpretación

La carga usa una conversación caliente. Un resultado favorable no demuestra el
mismo beneficio con miles de chats diferentes, varios nodos de API o caché
distribuida. La prueba decide si la táctica merece consideración; no autoriza su
adopción automática ni una distribución en microservicios.
