-- Create raw_job_payloads table for storing original payloads from sources
CREATE TABLE raw_job_payloads (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    external_job_id VARCHAR(255) NOT NULL,
    source_token VARCHAR(255),
    payload_json TEXT NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    checksum VARCHAR(64),
    parse_status VARCHAR(20)
);

CREATE INDEX idx_raw_job_payloads_source_external_id ON raw_job_payloads(source, external_job_id);
CREATE INDEX idx_raw_job_payloads_source_checksum ON raw_job_payloads(source, checksum);
CREATE INDEX idx_raw_job_payloads_fetched_at ON raw_job_payloads(fetched_at);

