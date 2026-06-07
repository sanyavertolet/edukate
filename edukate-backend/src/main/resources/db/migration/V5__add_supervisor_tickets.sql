CREATE TABLE supervisor_tickets (
    id              BIGSERIAL    PRIMARY KEY,
    submission_id   BIGINT       NOT NULL REFERENCES submissions(id),
    problem_set_id  BIGINT       NOT NULL REFERENCES problem_sets(id),
    supervisor_id   BIGINT       NOT NULL REFERENCES users(id),
    check_result_id BIGINT       NOT NULL REFERENCES check_results(id),
    status          TEXT         NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_st_submission   ON supervisor_tickets(submission_id);
CREATE INDEX idx_st_problem_set  ON supervisor_tickets(problem_set_id);
CREATE INDEX idx_st_supervisor   ON supervisor_tickets(supervisor_id);
CREATE INDEX idx_st_check_result ON supervisor_tickets(check_result_id);
