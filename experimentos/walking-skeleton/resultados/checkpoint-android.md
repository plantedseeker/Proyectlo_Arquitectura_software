# Checkpoint Android del walking skeleton

- Estado: **PASS**.
- Ejecución automática: `2026-09-11T21:02:00.6237930Z`.
- Verificación visual: `2026-09-11T21:06:18.1277246Z`.
- Cuenta: estudiante sintético local.
- Chat observado: `a03d76df-d2b7-4f95-9b66-12cabbf1a8fd`.
- Marcador esperado y observado:
  `walking-skeleton-20260911T210200251Z`.
- Captura: [`checkpoint-android.png`](checkpoint-android.png).
- SHA-256 de la captura:
  `EF448A33C773F620644CCECF7CFCB7809FFBF2EDB568D81AE9743D0FD53572FD`.
- Commit del instrumento y la documentación:
  `a635f2c948eaf3cd156b078841adfce65d86dde5`.

La coincidencia exacta del marcador conecta la evidencia visual con el
`message_id` registrado por la API y comprobado directamente en PostgreSQL en
`ultima-ejecucion.json`.

El campo `git_revision` del JSON identifica la revisión de aplicación y API
que estaba desplegada al iniciar la prueba. La ejecución ocurrió antes de crear
el commit del instrumento; por eso ambas revisiones se registran por separado y
no se reemplaza el dato capturado automáticamente.
