CREATE TABLE users (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255) UNIQUE NOT NULL,
    email  TEXT,
    token  TEXT NOT NULL,
    roles  JSONB NOT NULL DEFAULT '["USER"]',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE books (
    id          BIGSERIAL PRIMARY KEY,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    subject     VARCHAR(50) NOT NULL,
    title       TEXT NOT NULL,
    citation    TEXT NOT NULL,
    description TEXT
);

CREATE TABLE problems (
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books(id),
    code        VARCHAR(50) NOT NULL,
    key         VARCHAR(150) UNIQUE NOT NULL,
    is_hard     BOOLEAN NOT NULL DEFAULT FALSE,
    tags        JSONB NOT NULL DEFAULT '[]',
    text        TEXT NOT NULL,
    subtasks    JSONB NOT NULL DEFAULT '[]',
    images      JSONB NOT NULL DEFAULT '[]',
    UNIQUE (book_id, code),
    UNIQUE (code)
);

CREATE TABLE answers (
    id         BIGSERIAL PRIMARY KEY,
    problem_id BIGINT UNIQUE NOT NULL REFERENCES problems(id),
    text       TEXT NOT NULL,
    notes      TEXT,
    images     JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE submissions (
    id              BIGSERIAL PRIMARY KEY,
    problem_id      BIGINT NOT NULL REFERENCES problems(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    status          VARCHAR(20) NOT NULL,
    file_object_ids JSONB NOT NULL DEFAULT '[]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE problem_progress (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users(id),
    problem_id           BIGINT NOT NULL REFERENCES problems(id),
    latest_status        VARCHAR(20) NOT NULL,
    latest_time          TIMESTAMPTZ NOT NULL,
    latest_submission_id BIGINT NOT NULL REFERENCES submissions(id),
    best_status          VARCHAR(20) NOT NULL,
    best_time            TIMESTAMPTZ NOT NULL,
    best_submission_id   BIGINT NOT NULL REFERENCES submissions(id),
    UNIQUE (user_id, problem_id)
);

CREATE TABLE problem_sets (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    is_public        BOOLEAN NOT NULL DEFAULT FALSE,
    share_code       VARCHAR(50) UNIQUE NOT NULL,
    user_id_role_map JSONB NOT NULL DEFAULT '{}',
    invited_user_ids JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE problem_set_problems (
    problem_set_id BIGINT NOT NULL REFERENCES problem_sets(id) ON DELETE CASCADE,
    problem_id     BIGINT NOT NULL REFERENCES problems(id),
    position       INT NOT NULL,
    PRIMARY KEY (problem_set_id, problem_id)
);

CREATE TABLE check_results (
    id            BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions(id),
    status        VARCHAR(20) NOT NULL,
    trust_level   REAL NOT NULL,
    error_type    VARCHAR(50) NOT NULL,
    explanation   TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE file_objects (
    id             BIGSERIAL PRIMARY KEY,
    key_path       TEXT UNIQUE NOT NULL,
    key            JSONB NOT NULL,
    type           VARCHAR(50) NOT NULL,
    owner_user_id  BIGINT NOT NULL REFERENCES users(id),
    metadata       JSONB NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    meta_version   INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_problems_book_id ON problems(book_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_problem_id ON submissions(problem_id);
CREATE INDEX idx_problem_progress_user_id ON problem_progress(user_id);
CREATE INDEX idx_problem_set_problems_problem ON problem_set_problems(problem_id);
CREATE INDEX idx_check_results_submission_id ON check_results(submission_id);
