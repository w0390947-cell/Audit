package com.ruoyi.web.controller.audit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowBatchSearchResponse;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchRequest;
import com.ruoyi.system.domain.audit.vector.AuditWorkflowSearchResponse;
import com.ruoyi.system.service.audit.vector.AuditVectorSearchService;
import com.ruoyi.system.service.audit.vector.AuditWorkflowSearchService;

@RestController
@RequestMapping("/audit/library/vector")
public class AuditVectorController extends BaseController
{
    private static final String ERROR_KB_UNAVAILABLE = "KB_UNAVAILABLE";

    private final ObjectProvider<AuditVectorSearchService> auditVectorSearchServiceProvider;

    private final ObjectProvider<AuditWorkflowSearchService> auditWorkflowSearchServiceProvider;

    public AuditVectorController(ObjectProvider<AuditVectorSearchService> auditVectorSearchServiceProvider,
            ObjectProvider<AuditWorkflowSearchService> auditWorkflowSearchServiceProvider)
    {
        this.auditVectorSearchServiceProvider = auditVectorSearchServiceProvider;
        this.auditWorkflowSearchServiceProvider = auditWorkflowSearchServiceProvider;
    }

    @PreAuthorize("@ss.hasPermi('audit:library:common:list')")
    @PostMapping("/search")
    public AjaxResult search(@RequestBody AuditVectorSearchRequest request)
    {
        AuditVectorSearchService searchService = auditVectorSearchServiceProvider.getIfAvailable();
        if (searchService == null)
        {
            return AjaxResult.error("向量检索服务不可用");
        }
        try
        {
            return success(searchService.search(request, getLoginUser()));
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (RuntimeException e)
        {
            logger.warn("审核文件库向量检索失败", e);
            return AjaxResult.error("向量检索失败：" + e.getMessage());
        }
    }

    @Anonymous
    @PostMapping("/workflow-search")
    public AuditWorkflowSearchResponse workflowSearch(@RequestBody AuditWorkflowSearchRequest request)
    {
        AuditWorkflowSearchService searchService = auditWorkflowSearchServiceProvider.getIfAvailable();
        if (searchService == null)
        {
            return AuditWorkflowSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_UNAVAILABLE,
                    ERROR_KB_UNAVAILABLE, "向量检索服务不可用");
        }
        return searchService.search(request, getLoginUserOrNull());
    }

    @Anonymous
    @PostMapping("/workflow-batch-search")
    public AuditWorkflowBatchSearchResponse workflowBatchSearch(@RequestBody AuditWorkflowBatchSearchRequest request)
    {
        AuditWorkflowSearchService searchService = auditWorkflowSearchServiceProvider.getIfAvailable();
        if (searchService == null)
        {
            return AuditWorkflowBatchSearchResponse.error(request, AuditWorkflowSearchResponse.CODE_UNAVAILABLE,
                    ERROR_KB_UNAVAILABLE, "向量检索服务不可用");
        }
        return searchService.batchSearch(request, getLoginUserOrNull());
    }

    private LoginUser getLoginUserOrNull()
    {
        try
        {
            return getLoginUser();
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }
}
