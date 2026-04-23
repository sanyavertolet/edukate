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
CREATE INDEX idx_submissions_user_problem ON submissions(user_id, problem_id);
CREATE INDEX idx_problem_progress_user_id ON problem_progress(user_id);
CREATE INDEX idx_problem_set_problems_problem ON problem_set_problems(problem_id);
CREATE INDEX idx_check_results_submission_id ON check_results(submission_id);

-- ── Trigger 1: sync submissions.status from check_results ────────────────────

CREATE OR REPLACE FUNCTION fn_sync_submission_status()
RETURNS TRIGGER AS $$
DECLARE
    v_new_status VARCHAR(20);
BEGIN
    SELECT CASE
        WHEN COUNT(*) FILTER (WHERE status = 'SUCCESS')                        > 0 THEN 'SUCCESS'
        WHEN COUNT(*) FILTER (WHERE status IN ('MISTAKE', 'INTERNAL_ERROR'))   > 0 THEN 'FAILED'
        ELSE 'PENDING'
    END
    INTO v_new_status
    FROM check_results
    WHERE submission_id = NEW.submission_id
      AND status != 'PENDING';

    UPDATE submissions
    SET status = COALESCE(v_new_status, 'PENDING')
    WHERE id = NEW.submission_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_check_result_change
AFTER INSERT OR UPDATE ON check_results
FOR EACH ROW EXECUTE FUNCTION fn_sync_submission_status();

-- ── Trigger 2: upsert problem_progress when submission status promotes ────────

CREATE OR REPLACE FUNCTION fn_update_problem_progress()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = NEW.status THEN
        RETURN NEW;
    END IF;

    IF (OLD.status = 'PENDING' AND NEW.status IN ('FAILED', 'SUCCESS'))
    OR (OLD.status = 'FAILED'  AND NEW.status = 'SUCCESS') THEN

        INSERT INTO problem_progress (
            user_id, problem_id,
            latest_status, latest_time, latest_submission_id,
            best_status,   best_time,   best_submission_id
        )
        SELECT
            NEW.user_id, NEW.problem_id,
            latest.status, latest.created_at, latest.id,
            best.status,   best.created_at,   best.id
        FROM (
            SELECT id, status, created_at
            FROM   submissions
            WHERE  user_id = NEW.user_id AND problem_id = NEW.problem_id
            ORDER  BY created_at DESC
            LIMIT  1
        ) AS latest,
        (
            SELECT id, status, created_at
            FROM   submissions
            WHERE  user_id = NEW.user_id AND problem_id = NEW.problem_id
            ORDER  BY CASE status WHEN 'SUCCESS' THEN 2 WHEN 'FAILED' THEN 1 ELSE 0 END DESC,
                      created_at ASC
            LIMIT  1
        ) AS best
        ON CONFLICT (user_id, problem_id) DO UPDATE SET
            latest_status        = EXCLUDED.latest_status,
            latest_time          = EXCLUDED.latest_time,
            latest_submission_id = EXCLUDED.latest_submission_id,
            best_status          = EXCLUDED.best_status,
            best_time            = EXCLUDED.best_time,
            best_submission_id   = EXCLUDED.best_submission_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_submission_status_change
AFTER UPDATE OF status ON submissions
FOR EACH ROW EXECUTE FUNCTION fn_update_problem_progress();
