CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('student', 'company')),
    full_name VARCHAR(160) NOT NULL,
    phone VARCHAR(40),
    address VARCHAR(240),
    photo_path VARCHAR(500),
    cv_path VARCHAR(500),
    works_now BOOLEAN,
    current_company VARCHAR(160),
    current_role VARCHAR(160),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE company_profile (
    user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
    nit VARCHAR(40) NOT NULL UNIQUE,
    worker_count INTEGER NOT NULL CHECK (worker_count >= 0),
    representative_name VARCHAR(160),
    document_type VARCHAR(40),
    document_number VARCHAR(80),
    representative_document_path VARCHAR(500),
    rut_path VARCHAR(500),
    chamber_of_commerce_path VARCHAR(500)
);

CREATE TABLE student_skill (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    skill VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, skill)
);

CREATE TABLE job_offer (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    salary NUMERIC(14, 2),
    location VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    published_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_requirement (
    job_id UUID NOT NULL REFERENCES job_offer(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position >= 0),
    requirement VARCHAR(300) NOT NULL,
    PRIMARY KEY (job_id, position)
);

CREATE TABLE application (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES job_offer(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'cancelled')),
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (job_id, student_id)
);

CREATE TABLE chat (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_offer(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message VARCHAR(1000) NOT NULL DEFAULT '',
    last_message_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, job_id)
);

CREATE TABLE message (
    id UUID PRIMARY KEY,
    chat_id UUID NOT NULL REFERENCES chat(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    body VARCHAR(4000) NOT NULL CHECK (length(trim(body)) > 0),
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_session (
    token_hash CHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_offer_active_published ON job_offer (active, published_at DESC);
CREATE INDEX idx_application_student_status ON application (student_id, status, applied_at DESC);
CREATE INDEX idx_chat_student_recent ON chat (student_id, last_message_at DESC);
CREATE INDEX idx_chat_company_recent ON chat (company_id, last_message_at DESC);
CREATE INDEX idx_message_chat_time ON message (chat_id, sent_at ASC);
CREATE INDEX idx_auth_session_user ON auth_session (user_id);
CREATE INDEX idx_auth_session_expiry ON auth_session (expires_at);
