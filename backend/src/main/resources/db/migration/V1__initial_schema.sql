CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS modules (
    id BIGSERIAL PRIMARY KEY,
    order_index INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    theory_markdown TEXT NOT NULL,
    starter_code TEXT,
    expected_output TEXT,
    concept VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS acceptance_criteria (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    description VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL,
    input TEXT,
    expected_output TEXT NOT NULL,
    hint TEXT,
    hidden BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS student_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    module_id BIGINT NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    last_code_snapshot TEXT,
    attempts INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT now(),
    UNIQUE(user_id, module_id)
);

CREATE INDEX IF NOT EXISTS idx_progress_user ON student_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_progress_module ON student_progress(module_id);
CREATE INDEX IF NOT EXISTS idx_criteria_module ON acceptance_criteria(module_id);
