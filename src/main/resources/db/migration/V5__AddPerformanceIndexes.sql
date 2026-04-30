-- Add helpful indexes on existing tables for improved query performance

-- Add indexes on jobs table
CREATE INDEX IF NOT EXISTS idx_jobs_source_external_id ON jobs(source, external_id);
CREATE INDEX IF NOT EXISTS idx_jobs_role_family ON jobs(role_family);
CREATE INDEX IF NOT EXISTS idx_jobs_country ON jobs(country);
CREATE INDEX IF NOT EXISTS idx_jobs_software_relevant ON jobs(software_relevant);

-- Add indexes on job_skills table
CREATE INDEX IF NOT EXISTS idx_job_skills_job_id_skill_id ON job_skills(job_id, skill_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_skill_id ON job_skills(skill_id);

