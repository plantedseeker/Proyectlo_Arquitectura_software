# Resultado MSG-LOC-01

Ejecución final: 2026-09-04T21:27:33Z.

## Condiciones

- Equipo: Acer Predator PH16-71, 95,7 GiB, 32 CPU lógicas.
- Energía: conectado a corriente, batería 79 %, plan Equilibrado.
- PostgreSQL: 16.14 en Docker Desktop local.
- Instrumento: Python en el mismo equipo físico; API no incluida.
- Conversación: 100.000 mensajes; páginas comparadas de 50 elementos.
- Revisión con el instrumento: `78d8b380ae0fdbaa5e0b595486e652ac71666cb3`.

## Datos medidos

| Escenario SQL | Planificación | Ejecución | Buffers hit/read | Temporales read/write | Índice |
| --- | ---: | ---: | ---: | ---: | --- |
| Autorizar participante | 0,604 ms | **0,081 ms** | 3 / 0 | 0 / 0 | `chat_pkey` |
| Página reciente, `OFFSET 0` | 0,818 ms | **0,120 ms** | 13 / 0 | 0 / 0 | `idx_message_chat_recent` |
| Página profunda, `OFFSET 50000` | 0,741 ms | **102,050 ms** | 13.548 / 10.592 | 2.528 / 3.865 | ninguno; `Seq Scan` |
| Cursor después de posición 49.999 | 0,699 ms | **0,123 ms** | 12 / 0 | 0 / 0 | `idx_message_chat_recent` |

Los cuatro escenarios devolvieron las filas esperadas. El cociente derivado
`OFFSET 50000 / cursor` fue **829,675×**. Este cociente se calcula con los
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

La ejecución es real, el JSON está íntegro y se produjo después del commit que
incorporó el instrumento S7. El campo `git_revision` coincide con
`78d8b380ae0fdbaa5e0b595486e652ac71666cb3`, por lo que la evidencia puede
trazarse a la versión exacta del instrumento.

## Integridad

- [`localizacion.json`](localizacion.json): evidencia primaria y planes completos.
- SHA-256 actual: `B7EE659CDB1D2D231680D3397AFAFC5A908C817F9C081F8100DBCD89528DF0BE`.
