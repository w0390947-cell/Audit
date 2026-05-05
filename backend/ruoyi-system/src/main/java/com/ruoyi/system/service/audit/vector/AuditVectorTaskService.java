package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.vector.AuditVectorIndexResult;
import com.ruoyi.system.domain.audit.vector.AuditVectorTask;

public interface AuditVectorTaskService
{
    AuditVectorTask createIndexTask(Long resourceId, String operator);

    AuditVectorTask createReindexTask(Long resourceId, String operator);

    AuditVectorTask createDeleteTask(Long resourceId, String operator);

    int cancelPendingTasks(Long[] resourceIds, String operator);

    AuditVectorTask selectLatestTask(Long resourceId);

    int bootstrapCommonResources(String operator);

    int runPendingTasks();

    AuditVectorIndexResult runResourceNow(Long resourceId, String operator);
}
