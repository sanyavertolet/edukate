-- ── Content localization: extract translatable fields into dedicated tables ───
-- All languages are equal citizens — no "default" language in the main tables.
-- Language is stored as VARCHAR with a CHECK constraint (not a PostgreSQL enum)
-- for R2DBC compatibility while still enforcing valid values at the DB level.

-- 1. Problem localizations (text, tags, subproblems per language)
CREATE TABLE problem_localizations (
    problem_id  BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    language    VARCHAR(5) NOT NULL CHECK (language IN ('RU', 'EN')),
    text        TEXT NOT NULL,
    tags        JSONB NOT NULL DEFAULT '[]',
    subproblems JSONB NOT NULL DEFAULT '[]',
    PRIMARY KEY (problem_id, language)
);

-- 2. Answer localizations (text, notes per language)
CREATE TABLE answer_localizations (
    answer_id   BIGINT NOT NULL REFERENCES answers(id) ON DELETE CASCADE,
    language    VARCHAR(5) NOT NULL CHECK (language IN ('RU', 'EN')),
    text        TEXT NOT NULL,
    notes       TEXT,
    PRIMARY KEY (answer_id, language)
);

-- 3. Migrate existing Russian content (rename subtasks → subproblems, id → code)
INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
SELECT
    id,
    'RU',
    text,
    tags,
    CASE
        WHEN subtasks = '[]'::jsonb THEN '[]'::jsonb
        ELSE (
            SELECT jsonb_agg(
                jsonb_build_object('code', elem->>'id', 'text', elem->>'text')
            )
            FROM jsonb_array_elements(subtasks) AS elem
        )
    END
FROM problems;

INSERT INTO answer_localizations (answer_id, language, text, notes)
SELECT id, 'RU', text, notes FROM answers;

-- 4. Drop text columns from main tables
ALTER TABLE problems DROP COLUMN text;
ALTER TABLE problems DROP COLUMN tags;
ALTER TABLE problems DROP COLUMN subtasks;

ALTER TABLE answers DROP COLUMN text;
ALTER TABLE answers DROP COLUMN notes;

-- 5. Add language to submissions (for checker language matching)
ALTER TABLE submissions ADD COLUMN language VARCHAR(5) NOT NULL DEFAULT 'RU'
    CHECK (language IN ('RU', 'EN'));
