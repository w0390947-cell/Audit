SET NAMES utf8mb4;

ALTER TABLE `audit_ai_finding`
  ADD COLUMN `page_no` int DEFAULT NULL COMMENT '报告页码，从1开始' AFTER `finding_content`,
  ADD COLUMN `location_json` text COMMENT '定位信息JSON' AFTER `page_no`;
