\set ON_ERROR_STOP on

-- Semilla sintética, determinista e idempotente: 10 000 ofertas, 90 % activas.
-- Requiere que la API haya arrancado una vez para crear el usuario empresa demo.
WITH demo_company AS (
    SELECT id FROM app_user WHERE email = 'empresa@utrabajo.local'
), synthetic AS (
    SELECT
        g,
        (
            substr(md5('utrabajo-synthetic-job-' || g), 1, 8) || '-' ||
            substr(md5('utrabajo-synthetic-job-' || g), 9, 4) || '-' ||
            substr(md5('utrabajo-synthetic-job-' || g), 13, 4) || '-' ||
            substr(md5('utrabajo-synthetic-job-' || g), 17, 4) || '-' ||
            substr(md5('utrabajo-synthetic-job-' || g), 21, 12)
        )::uuid AS id
    FROM generate_series(1, 10000) AS g
)
INSERT INTO job_offer(id, company_id, title, description, salary, location, active, published_at)
SELECT
    synthetic.id,
    demo_company.id,
    'Oferta sintética ' || synthetic.g,
    repeat('Descripción de carga UTrabajo. ', 35),
    1800000 + (synthetic.g % 40) * 100000,
    (ARRAY['Bogotá', 'Medellín', 'Cali', 'Remoto'])[(synthetic.g % 4) + 1],
    synthetic.g % 10 <> 0,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + synthetic.g * INTERVAL '1 second'
FROM synthetic CROSS JOIN demo_company
ON CONFLICT (id) DO UPDATE SET
    description = EXCLUDED.description,
    active = EXCLUDED.active;

ANALYZE job_offer;

SELECT
    count(*) FILTER (WHERE title LIKE 'Oferta sintética %') AS synthetic_total,
    count(*) FILTER (WHERE title LIKE 'Oferta sintética %' AND active) AS synthetic_active,
    round(avg(length(description))) AS average_description_chars
FROM job_offer;
