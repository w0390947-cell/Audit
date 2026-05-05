package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.vector.AuditVectorIndexResult;
import com.ruoyi.system.domain.audit.vector.AuditVectorTask;

public interface AuditVectorIndexService
{
    AuditVectorIndexResult index(AuditVectorTask task);

    AuditVectorIndexResult indexResource(Long resourceId, String taskType, String operator);
}
