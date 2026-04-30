-- Create ingestion_runs table for tracking all ingestion executions
CREATE TABLE ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    token VARCHAR(100) NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    fetched_count INT NOT NULL DEFAULT 0,
    inserted_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    software_relevant_count INT NOT NULL DEFAULT 0,
    extracted_skills_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    company_reused_after_duplicate_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    failure_reason TEXT,
    run_duration_ms BIGINT
);

CREATE INDEX idx_ingestion_runs_source_token_started ON ingestion_runs(source, token, started_at);
CREATE INDEX idx_ingestion_runs_source_started ON ingestion_runs(source, started_at);
CREATE INDEX idx_ingestion_runs_status ON ingestion_runs(status);

