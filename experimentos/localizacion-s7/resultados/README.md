# Resultado MSG-LOC-01

Ejecución: 2026-09-04T20:43:40Z.

## Condiciones

- Equipo: Acer Predator PH16-71, 95,7 GiB, 32 CPU lógicas.
- Energía: conectado a corriente, batería 79 %, plan Equilibrado.
- PostgreSQL: 16.14 en Docker Desktop local.
- Instrumento: Python en el mismo equipo físico; API no incluida.
- Conversación: 100.000 mensajes; páginas comparadas de 50 elementos.
- Revisión del sistema base: `bdfd6f82c85bf295b550364a61541fb932bb87f7`.

## Datos medidos

| Escenario SQL | Planificación | Ejecución | Buffers hit/read | Temporales read/write | Índice |
| --- | ---: | ---: | ---: | ---: | --- |
| Autorizar participante | 0,558 ms | **0,070 ms** | 3 / 0 | 0 / 0 | `chat_pkey` |
| Página reciente, `OFFSET 0` | 0,680 ms | **0,112 ms** | 13 / 0 | 0 / 0 | `idx_message_chat_recent` |
| Página profunda, `OFFSET 50000` | 0,715 ms | **41,017 ms** | 13.452 / 10.688 | 2.617 / 3.864 | ninguno; `Seq Scan` |
| Cursor después de posición 49.999 | 0,690 ms | **0,130 ms** | 12 / 0 | 0 / 0 | `idx_message_chat_recent` |

Los cuatro escenarios devolvieron las filas esperadas. El cociente derivado
`OFFSET 50000 / cursor` fue **315,515×**. Este cociente se calcula con los
tiempos de ejecución registrados; no es una nueva corrida.

## Interpretación

PostgreSQL usa el índice compuesto para página reciente y cursor. En esta
observación eligió escaneo secuencial, ordenamiento paralelo y archivos
temporales para el offset profundo. Esto sustenta cursor para una función futura
de “cargar mensajes antiguos”, pero no obliga a cambiar la página reciente que
consume Android.

No se resta el tiempo SQL de los 9,109 ms de S4: son instrumentos y ejecuciones
diferentes. S4 mide HTTP completo; S7 localiza trabajo dentro de PostgreSQL.

## Estado de reproducibilidad

La ejecución es real y el JSON está íntegro, pero ocurrió antes del commit que
incorporará el instrumento S7. Por eso la revisión registrada corresponde al
último `HEAD` del sistema base. Después de que el responsable revise y haga el
primer commit del instrumento, se repetirá con `-SkipBuild -SkipSeed` para que
la revisión final contenga el propio instrumento.

## Integridad

- [`localizacion.json`](localizacion.json): evidencia primaria y planes completos.
- SHA-256 actual: `2A847FE02DB4B2E0EDE0A2599647E6974AC8EBFB6F6D2E26C9D3FAC95A529F38`.
