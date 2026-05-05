SET NAMES utf8mb4;

ALTER TABLE `audit_ai_task`
  ADD COLUMN `review_version_id` bigint DEFAULT NULL COMMENT '审核任务版本主键' AFTER `review_task_id`;

CREATE INDEX `idx_audit_ai_review_version`
  ON `audit_ai_task` (`review_task_id`, `review_version_id`);

UPDATE `audit_ai_task` t
INNER JOIN `audit_review_version` v
  ON v.task_id = t.review_task_id
 AND v.current_flag = '1'
SET t.review_version_id = v.version_id
WHERE t.review_version_id IS NULL;
