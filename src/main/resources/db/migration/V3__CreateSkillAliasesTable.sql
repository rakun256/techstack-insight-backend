-- Create skill_aliases table for skill name normalization
CREATE TABLE skill_aliases (
    id BIGSERIAL PRIMARY KEY,
    canonical_skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    alias_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(canonical_skill_id, alias_name)
);

CREATE INDEX idx_skill_aliases_alias_name ON skill_aliases(alias_name);
CREATE INDEX idx_skill_aliases_canonical_skill_id ON skill_aliases(canonical_skill_id);

