-- Referencia auditable de las consultas ejecutadas por ejecutar_explain.py.
-- Los valores son deterministas para la semilla de mensajería.

-- 0. Autorización ejecutada por requireChatParticipant antes de leer mensajes.
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT count(*)
FROM chat
WHERE id = '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6'::uuid
  AND (
      student_id = (SELECT id FROM app_user WHERE email = 'estudiante@utrabajo.local')
      OR company_id = (SELECT id FROM app_user WHERE email = 'estudiante@utrabajo.local')
  );

-- A. Consulta actual del endpoint, página más reciente.
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT id, chat_id, sender_id, body, sent_at
FROM (
    SELECT id, chat_id, sender_id, body, sent_at
    FROM message
    WHERE chat_id = '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6'::uuid
    ORDER BY sent_at DESC, id DESC
    LIMIT 50 OFFSET 0
) recent
ORDER BY sent_at ASC, id ASC;

-- B. Misma consulta con página profunda.
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT id, chat_id, sender_id, body, sent_at
FROM (
    SELECT id, chat_id, sender_id, body, sent_at
    FROM message
    WHERE chat_id = '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6'::uuid
    ORDER BY sent_at DESC, id DESC
    LIMIT 50 OFFSET 50000
) recent
ORDER BY sent_at ASC, id ASC;

-- C. El script sustituye :cursor_sent_at y :cursor_id por la fila de la
-- posición 49.999, y pide las 50 siguientes sin OFFSET.
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT id, chat_id, sender_id, body, sent_at
FROM (
    SELECT id, chat_id, sender_id, body, sent_at
    FROM message
    WHERE chat_id = '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6'::uuid
      AND (sent_at, id) < (:cursor_sent_at::timestamptz, :cursor_id::uuid)
    ORDER BY sent_at DESC, id DESC
    LIMIT 50
) recent
ORDER BY sent_at ASC, id ASC;
