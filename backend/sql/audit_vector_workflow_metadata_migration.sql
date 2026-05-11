-- Workflow knowledge-base metadata extension for PostgreSQL + pgvector.
-- Execute in the audit_vector PostgreSQL database.

ALTER TABLE audit_vector_document
  ADD COLUMN IF NOT EXISTS knowledge_base_code VARCHAR(64) DEFAULT 'default',
  ADD COLUMN IF NOT EXISTS category_code VARCHAR(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS business_type VARCHAR(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'effective',
  ADD COLUMN IF NOT EXISTS effective_date DATE DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS expire_date DATE DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS owner_dept_id VARCHAR(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS source_system VARCHAR(64) DEFAULT 'audit';

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_kb
  ON audit_vector_document(knowledge_base_code);

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_category
  ON audit_vector_document(category_code);

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_status_dates
  ON audit_vector_document(status, effective_date, expire_date);

ALTER TABLE audit_vector_chunk
  ADD COLUMN IF NOT EXISTS chunk_uid VARCHAR(80) DEFAULT '',
  ADD COLUMN IF NOT EXISTS rule_code VARCHAR(100) DEFAULT '',
  ADD COLUMN IF NOT EXISTS section_path VARCHAR(500) DEFAULT '',
  ADD COLUMN IF NOT EXISTS paragraph_no INTEGER DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb;

CREATE UNIQUE INDEX IF NOT EXISTS uk_audit_vector_chunk_uid
  ON audit_vector_chunk(chunk_uid)
  WHERE chunk_uid <> '';

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_rule
  ON audit_vector_chunk(rule_code);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_metadata
  ON audit_vector_chunk USING gin(metadata);

UPDATE audit_vector_document
SET knowledge_base_code = COALESCE(NULLIF(knowledge_base_code, ''), 'default'),
    status = COALESCE(NULLIF(status, ''), 'effective'),
    source_system = COALESCE(NULLIF(source_system, ''), 'audit');

UPDATE audit_vector_chunk
SET chunk_uid = CASE
        WHEN chunk_uid IS NULL OR chunk_uid = '' THEN 'KB-CHUNK-' || document_id || '-' || chunk_no
        ELSE chunk_uid
    END,
    section_path = COALESCE(NULLIF(section_path, ''), section_title),
    metadata = COALESCE(metadata, '{}'::jsonb);
