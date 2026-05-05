-- PostgreSQL + pgvector initialization for audit file vector index.
-- Execute these statements in the audit_vector PostgreSQL database.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS audit_vector_document (
  document_id BIGSERIAL PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  resource_type VARCHAR(20) NOT NULL DEFAULT 'common',
  folder_id BIGINT DEFAULT 0,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_hash VARCHAR(64) NOT NULL,
  current_version_no VARCHAR(20) DEFAULT 'v1.0',
  parse_status VARCHAR(20) DEFAULT 'pending',
  vector_status VARCHAR(20) DEFAULT 'pending',
  chunk_count INTEGER DEFAULT 0,
  embedding_model VARCHAR(100) DEFAULT '',
  embedding_dimensions INTEGER DEFAULT 1024,
  last_index_time TIMESTAMP DEFAULT NULL,
  error_msg TEXT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_audit_vector_document_resource
  ON audit_vector_document(resource_type, resource_id);

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_folder
  ON audit_vector_document(folder_id);

CREATE TABLE IF NOT EXISTS audit_vector_chunk (
  chunk_id BIGSERIAL PRIMARY KEY,
  document_id BIGINT NOT NULL REFERENCES audit_vector_document(document_id) ON DELETE CASCADE,
  resource_id BIGINT NOT NULL,
  folder_id BIGINT DEFAULT 0,
  chunk_no INTEGER NOT NULL,
  chunk_text TEXT NOT NULL,
  page_no INTEGER DEFAULT NULL,
  section_title VARCHAR(255) DEFAULT '',
  token_count INTEGER DEFAULT 0,
  embedding vector(1024) NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_document
  ON audit_vector_chunk(document_id);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_resource
  ON audit_vector_chunk(resource_id);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_folder
  ON audit_vector_chunk(folder_id);

CREATE INDEX IF NOT EXISTS idx_audit_vector_chunk_embedding
  ON audit_vector_chunk USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

-- Smoke test for pgvector. It is intentionally temporary and leaves no schema objects.
CREATE TEMP TABLE vector_smoke_test (
  id BIGSERIAL PRIMARY KEY,
  content TEXT,
  embedding vector(3)
);

INSERT INTO vector_smoke_test(content, embedding)
VALUES
  ('test content A', '[0.1,0.2,0.3]'),
  ('test content B', '[0.2,0.1,0.4]');

SELECT id, content, embedding <=> '[0.1,0.2,0.25]' AS distance
FROM vector_smoke_test
ORDER BY embedding <=> '[0.1,0.2,0.25]'
LIMIT 2;
