CREATE TABLE jobs (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    title                VARCHAR(150) NOT NULL,
    department           VARCHAR(100) NOT NULL,
    location             VARCHAR(100) NOT NULL,
    min_experience_years INT          NOT NULL DEFAULT 0,
    description          TEXT,
    status               VARCHAR(20)  NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_jobs_status_created (status, created_at)
);

CREATE TABLE job_skills (
    job_id BIGINT      NOT NULL,
    skill  VARCHAR(60) NOT NULL,
    PRIMARY KEY (job_id, skill),
    INDEX idx_job_skills_skill (skill),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
);

CREATE TABLE candidates (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    full_name        VARCHAR(120) NOT NULL,
    email            VARCHAR(180) NOT NULL,
    phone            VARCHAR(20),
    experience_years INT          NOT NULL DEFAULT 0,
    created_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_candidates_email UNIQUE (email)
);

CREATE TABLE candidate_skills (
    candidate_id BIGINT      NOT NULL,
    skill        VARCHAR(60) NOT NULL,
    PRIMARY KEY (candidate_id, skill),
    CONSTRAINT fk_candidate_skills_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE
);

CREATE TABLE applications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    job_id       BIGINT       NOT NULL,
    candidate_id BIGINT       NOT NULL,
    stage        VARCHAR(20)  NOT NULL,
    cover_note   VARCHAR(2000),
    match_score  INT,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    -- one application per candidate per job, enforced by the database even under concurrent requests
    CONSTRAINT uk_applications_job_candidate UNIQUE (job_id, candidate_id),
    INDEX idx_applications_job_stage (job_id, stage),
    INDEX idx_applications_candidate (candidate_id, created_at),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id)
);

CREATE TABLE application_stage_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT       NOT NULL,
    from_stage     VARCHAR(20),
    to_stage       VARCHAR(20)  NOT NULL,
    note           VARCHAR(500),
    changed_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_stage_history_application (application_id, changed_at),
    CONSTRAINT fk_stage_history_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    event_id       VARCHAR(36)   NOT NULL,
    candidate_id   BIGINT        NOT NULL,
    application_id BIGINT        NOT NULL,
    recipient      VARCHAR(180)  NOT NULL,
    subject        VARCHAR(200)  NOT NULL,
    body           VARCHAR(1000) NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    -- makes the Kafka consumer idempotent: a redelivered event cannot create a second notification
    CONSTRAINT uk_notifications_event UNIQUE (event_id),
    INDEX idx_notifications_candidate (candidate_id, created_at)
);
