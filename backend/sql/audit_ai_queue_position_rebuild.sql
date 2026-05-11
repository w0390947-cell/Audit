-- Rebuild AI task queue positions to match the real queue order.
-- Order rule: executing, waiting, paused, others; then high, medium, low; then old queue position; then task id.

DROP TEMPORARY TABLE IF EXISTS tmp_audit_ai_queue_order;

CREATE TEMPORARY TABLE tmp_audit_ai_queue_order AS
SELECT ordered.ai_task_id, @rownum := @rownum + 1 AS new_queue_position
FROM (
    SELECT a.ai_task_id
    FROM audit_ai_task a
    WHERE a.del_flag = '0'
      AND a.task_status <> 'completed'
    ORDER BY CASE a.task_status
            WHEN 'executing' THEN 1
            WHEN 'waiting' THEN 2
            WHEN 'paused' THEN 3
            ELSE 4
        END ASC,
        CASE a.priority
            WHEN 'high' THEN 1
            WHEN 'medium' THEN 2
            ELSE 3
        END ASC,
        CASE
            WHEN a.queue_position IS NULL OR a.queue_position <= 0 THEN 2147483647
            ELSE a.queue_position
        END ASC,
        a.ai_task_id ASC
) ordered
CROSS JOIN (SELECT @rownum := 0) vars;

UPDATE audit_ai_task a
INNER JOIN tmp_audit_ai_queue_order q ON q.ai_task_id = a.ai_task_id
SET a.queue_position = q.new_queue_position,
    a.update_by = 'queue-position-migration',
    a.update_time = sysdate();

UPDATE audit_ai_task
SET queue_position = 0,
    update_by = 'queue-position-migration',
    update_time = sysdate()
WHERE del_flag = '0'
  AND task_status = 'completed';

DROP TEMPORARY TABLE tmp_audit_ai_queue_order;
