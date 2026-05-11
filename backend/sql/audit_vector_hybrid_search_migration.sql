-- PostgreSQL migration for hybrid keyword retrieval.
-- Execute in the audit_vector PostgreSQL database.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_text_trgm
  ON audit_vector_chunk USING gin(chunk_text gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_rule_trgm
  ON audit_vector_chunk USING gin(rule_code gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_section_path_trgm
  ON audit_vector_chunk USING gin(section_path gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_file_name_trgm
  ON audit_vector_document USING gin(file_name gin_trgm_ops);
