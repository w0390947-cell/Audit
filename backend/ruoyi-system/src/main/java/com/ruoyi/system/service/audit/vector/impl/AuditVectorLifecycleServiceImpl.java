package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorLifecycleService;
import com.ruoyi.system.service.audit.vector.AuditVectorTaskService;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AuditVectorLifecycleServiceImpl implements AuditVectorLifecycleService
{
    private static final Logger log = LoggerFactory.getLogger(AuditVectorLifecycleServiceImpl.class);

    private final VectorProperties properties;
    private final AuditVectorTaskService auditVectorTaskService;
    private final ObjectProvider<VectorStoreRepository> vectorStoreRepositoryProvider;
    private final AuditLibraryMapper auditLibraryMapper;

    public AuditVectorLifecycleServiceImpl(VectorProperties properties,
            AuditVectorTaskService auditVectorTaskService,
            ObjectProvider<VectorStoreRepository> vectorStoreRepositoryProvider,
            AuditLibraryMapper auditLibraryMapper)
    {
        this.properties = properties;
        this.auditVectorTaskService = auditVectorTaskService;
        this.vectorStoreRepositoryProvider = vectorStoreRepositoryProvider;
        this.auditLibraryMapper = auditLibraryMapper;
    }

    @Override
    public void onCommonResourceCreated(AuditCommonResource resource)
    {
        if (!enabled() || resource == null)
        {
            return;
        }
        try
        {
            auditVectorTaskService.createIndexTask(resource.getResourceId(), resource.getCreateBy());
        }
        catch (RuntimeException e)
        {
            log.warn("创建审核文件向量索引任务失败，resourceId={}", resource.getResourceId(), e);
        }
    }

    @Override
    public void onCommonResourceUpdated(AuditCommonResource before, AuditCommonResource after)
    {
        if (!enabled() || before == null || after == null)
        {
            return;
        }
        String operator = StringUtils.defaultIfBlank(after.getUpdateBy(), after.getCreateBy());
        if (!Objects.equals(before.getFileUrl(), after.getFileUrl()))
        {
            try
            {
                auditVectorTaskService.createReindexTask(after.getResourceId(), operator);
            }
            catch (RuntimeException e)
            {
                log.warn("创建审核文件重新向量化任务失败，resourceId={}", after.getResourceId(), e);
            }
            return;
        }
        VectorStoreRepository repository = vectorStoreRepositoryProvider.getIfAvailable();
        if (repository == null)
        {
            return;
        }
        try
        {
            repository.updateDocumentMetadata(AuditVectorDocument.RESOURCE_TYPE_COMMON, after.getResourceId(),
                    after.getFileName(), after.getFileUrl(), after.getCurrentVersionNo());
        }
        catch (RuntimeException e)
        {
            log.warn("同步审核文件向量元数据失败，resourceId={}", after.getResourceId(), e);
        }
    }

    @Override
    public void onCommonResourceMoved(Long resourceId, Long folderId)
    {
        if (!enabled() || resourceId == null)
        {
            return;
        }
        VectorStoreRepository repository = vectorStoreRepositoryProvider.getIfAvailable();
        if (repository == null)
        {
            return;
        }
        try
        {
            repository.updateFolderId(AuditVectorDocument.RESOURCE_TYPE_COMMON, resourceId, folderId);
        }
        catch (RuntimeException e)
        {
            log.warn("同步审核文件向量文件夹失败，resourceId={}, folderId={}", resourceId, folderId, e);
        }
    }

    @Override
    public void onCommonResourcesDeleted(List<Long> resourceIds)
    {
        if (!enabled() || CollectionUtils.isEmpty(resourceIds))
        {
            return;
        }
        Long[] ids = resourceIds.toArray(new Long[0]);
        auditVectorTaskService.cancelPendingTasks(ids, "system");
        VectorStoreRepository repository = vectorStoreRepositoryProvider.getIfAvailable();
        if (repository == null)
        {
            return;
        }
        try
        {
            repository.deleteByResourceIds(AuditVectorDocument.RESOURCE_TYPE_COMMON, resourceIds);
        }
        catch (RuntimeException e)
        {
            log.warn("删除审核文件向量索引失败，resourceIds={}", resourceIds, e);
            for (Long resourceId : resourceIds)
            {
                auditVectorTaskService.createDeleteTask(resourceId, "system");
            }
        }
    }

    @Override
    public void onFoldersDeleted(List<Long> folderIds)
    {
        if (!enabled() || CollectionUtils.isEmpty(folderIds))
        {
            return;
        }
        Long[] ids = folderIds.toArray(new Long[0]);
        List<Long> resourceIds = auditLibraryMapper.selectAuditCommonResourceIdsByFolderIds(ids);
        onCommonResourcesDeleted(resourceIds);
    }

    private boolean enabled()
    {
        return properties.isEnabled() && properties.getLifecycle().isEnabled();
    }
}
