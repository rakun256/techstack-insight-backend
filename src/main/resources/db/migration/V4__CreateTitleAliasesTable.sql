-- Create title_aliases table for job title normalization and role classification
CREATE TABLE title_aliases (
    id BIGSERIAL PRIMARY KEY,
    raw_title_pattern TEXT NOT NULL,
    normalized_title VARCHAR(255) NOT NULL,
    role_family VARCHAR(100),
    role_subfamily VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_title_aliases_raw_pattern ON title_aliases USING btree (raw_title_pattern(100));
CREATE INDEX idx_title_aliases_role_family ON title_aliases(role_family);
CREATE INDEX idx_title_aliases_active ON title_aliases(active);

