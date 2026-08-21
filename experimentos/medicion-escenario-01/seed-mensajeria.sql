\set ON_ERROR_STOP on

-- Semilla determinista e idempotente para Mensajería y mesa de ayuda.
-- Distribución: 900 conversaciones pequeñas, 90 medianas, 9 grandes y
-- una conversación extrema accesible por el estudiante demo.

INSERT INTO app_user(id, email, password_hash, role, full_name, completed)
SELECT
    md5('mensajeria-student-' || g)::uuid,
    'mensajeria-' || g || '@utrabajo.test',
    'cuenta-sintetica-sin-login',
    'student',
    'Estudiante sintético ' || g,
    TRUE
FROM generate_series(2, 1000) AS g
ON CONFLICT (id) DO NOTHING;

INSERT INTO job_offer(id, company_id, title, description, salary, location, active, published_at)
SELECT
    md5('mensajeria-job')::uuid,
    company.id,
    'Soporte de práctica profesional',
    'Oferta sintética usada para crear conversaciones del experimento de mensajería.',
    3000000,
    'Bogotá',
    TRUE,
    TIMESTAMPTZ '2026-01-01 00:00:00+00'
FROM app_user company
WHERE company.email = 'empresa@utrabajo.local'
ON CONFLICT (id) DO NOTHING;

WITH numbered_chats AS (
    SELECT
        1 AS chat_number,
        (SELECT id FROM app_user WHERE email = 'estudiante@utrabajo.local') AS student_id
    UNION ALL
    SELECT
        g,
        md5('mensajeria-student-' || g)::uuid
    FROM generate_series(2, 1000) AS g
)
INSERT INTO chat(id, student_id, company_id, job_id, created_at, last_message, last_message_at)
SELECT
    md5('mensajeria-chat-' || numbered_chats.chat_number)::uuid,
    numbered_chats.student_id,
    company.id,
    md5('mensajeria-job')::uuid,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + numbered_chats.chat_number * INTERVAL '1 minute',
    'Semilla de mensajería preparada',
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + numbered_chats.chat_number * INTERVAL '1 minute'
FROM numbered_chats
CROSS JOIN (SELECT id FROM app_user WHERE email = 'empresa@utrabajo.local') company
ON CONFLICT (id) DO NOTHING;

DELETE FROM message
WHERE chat_id IN (
    SELECT md5('mensajeria-chat-' || g)::uuid
    FROM generate_series(1, 1000) AS g
);

WITH chat_sizes AS (
    SELECT
        g AS chat_number,
        CASE
            WHEN g = 1 THEN 100000
            WHEN g <= 901 THEN 10
            WHEN g <= 991 THEN 1000
            ELSE 10000
        END AS message_count
    FROM generate_series(1, 1000) AS g
), generated_messages AS (
    SELECT
        chat_sizes.chat_number,
        message_number,
        md5(
            'mensajeria-message-' || chat_sizes.chat_number || '-' || message_number
        )::uuid AS message_id,
        md5('mensajeria-chat-' || chat_sizes.chat_number)::uuid AS chat_id
    FROM chat_sizes
    CROSS JOIN LATERAL generate_series(1, chat_sizes.message_count) AS message_number
)
INSERT INTO message(id, chat_id, sender_id, body, sent_at)
SELECT
    generated_messages.message_id,
    generated_messages.chat_id,
    CASE
        WHEN generated_messages.message_number % 2 = 0 THEN c.company_id
        ELSE c.student_id
    END,
    'Mensaje sintético ' || generated_messages.message_number || ' de la conversación ' ||
        generated_messages.chat_number || '. ' || repeat('contenido de carga ', 10),
    TIMESTAMPTZ '2026-01-01 00:00:00+00' +
        generated_messages.chat_number * INTERVAL '1 day' +
        generated_messages.message_number * INTERVAL '1 millisecond'
FROM generated_messages
JOIN chat c ON c.id = generated_messages.chat_id;

WITH last_messages AS (
    SELECT chat_id, body, sent_at
    FROM (
        SELECT
            chat_id,
            body,
            sent_at,
            row_number() OVER (PARTITION BY chat_id ORDER BY sent_at DESC, id DESC) AS position
        FROM message
        WHERE chat_id IN (
            SELECT md5('mensajeria-chat-' || g)::uuid
            FROM generate_series(1, 1000) AS g
        )
    ) ranked
    WHERE position = 1
)
UPDATE chat c
SET last_message = left(last_messages.body, 1000),
    last_message_at = last_messages.sent_at
FROM last_messages
WHERE c.id = last_messages.chat_id;

ANALYZE chat;
ANALYZE message;

WITH sizes AS (
    SELECT c.id, count(m.id) AS message_count
    FROM chat c
    JOIN message m ON m.chat_id = c.id
    WHERE c.id IN (
        SELECT md5('mensajeria-chat-' || g)::uuid
        FROM generate_series(1, 1000) AS g
    )
    GROUP BY c.id
)
SELECT
    count(*) AS conversations,
    sum(message_count) AS messages,
    count(*) FILTER (WHERE message_count = 10) AS small_conversations,
    count(*) FILTER (WHERE message_count = 1000) AS medium_conversations,
    count(*) FILTER (WHERE message_count = 10000) AS large_conversations,
    count(*) FILTER (WHERE message_count = 100000) AS extreme_conversations,
    max(message_count) AS largest_conversation
FROM sizes;
