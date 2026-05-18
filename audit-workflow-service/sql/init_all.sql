-- Full initialization script for a fresh audit_workflow database.
-- Run from the audit-workflow-service directory:
-- mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/init_all.sql

SET NAMES utf8mb4;

SOURCE sql/phase1_schema.sql;
SOURCE sql/phase1_seed.sql;
SOURCE sql/phase2_schema.sql;
SOURCE sql/phase2_seed.sql;
SOURCE sql/phase3_schema.sql;
SOURCE sql/phase3_seed.sql;
SOURCE sql/phase4_schema.sql;
SOURCE sql/phase4_seed.sql;
SOURCE sql/phase5_schema.sql;
SOURCE sql/phase5_seed.sql;
SOURCE sql/phase6_business_report_workflow.sql;
SOURCE sql/phase7_uploaded_basis_workflow.sql;
