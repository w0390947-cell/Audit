package com.ruoyi.system.service.audit.vector;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchResponse;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchResponse;

public interface AuditWorkflowSearchService
{
    AuditWorkflowSearchResponse search(AuditWorkflowSearchRequest request, LoginUser loginUser);

    AuditWorkflowBatchSearchResponse batchSearch(AuditWorkflowBatchSearchRequest request, LoginUser loginUser);
}
