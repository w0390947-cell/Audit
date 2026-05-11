package com.ruoyi.system.service.audit.vector;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchResult;
import java.util.List;

public interface AuditVectorSearchService
{
    AuditVectorSearchResult search(AuditVectorSearchRequest request, LoginUser loginUser);

    List<AuditVectorSearchResult> batchSearch(List<AuditVectorSearchRequest> requests, LoginUser loginUser);
}
