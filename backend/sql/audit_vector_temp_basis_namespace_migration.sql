-- Temporary uploaded-basis vectors for workflow tasks.
-- Execute in the audit_vector PostgreSQL database.

CREATE INDEX IF NOT EXISTS idx_audit_vector_document_workflow_basis_ns
  ON audit_vector_document(resource_type, knowledge_base_code, vector_status);
