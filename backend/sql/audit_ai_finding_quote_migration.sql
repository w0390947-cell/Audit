SET NAMES utf8mb4;

ALTER TABLE `audit_ai_finding`
  ADD COLUMN `quote_text` varchar(1000) DEFAULT '' COMMENT '报告原文引用' AFTER `finding_content`;
