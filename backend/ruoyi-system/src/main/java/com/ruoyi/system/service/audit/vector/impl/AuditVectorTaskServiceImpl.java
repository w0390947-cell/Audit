package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.AuditVectorIndexResult;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.domain.audit.vector.AuditVectorTask;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.mapper.audit.AuditVectorTaskMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorIndexService;
import com.ruoyi.system.service.audit.vector.AuditVectorTaskService;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class AuditVectorTaskServiceImpl implements AuditVectorTaskService
{
    private static final String RESOURCE_STATUS_PENDING = "pending";
    private static final int RESOURCE_PROGRESS_TEXT_MAX_LENGTH = 255;

    private final AuditVectorTaskMapper auditVectorTaskMapper;
    private final AuditLibraryMapper auditLibraryMapper;
    private final ObjectProvider<AuditVectorIndexService> auditVectorIndexServiceProvider;
    private final ObjectProvider<VectorStoreRepository> vectorStoreRepositoryProvider;
    private final VectorProperties properties;

    public AuditVectorTaskServiceImpl(AuditVectorTaskMapper auditVectorTaskMapper,
            AuditLibraryMapper auditLibraryMapper,
            ObjectProvider<AuditVectorIndexService> auditVectorIndexServiceProvider,
            ObjectProvider<VectorStoreRepository> vectorStoreRepositoryProvider,
            VectorProperties properties)
    {
        this.auditVectorTaskMapper = auditVectorTaskMapper;
        this.auditLibraryMapper = auditLibraryMapper;
        this.auditVectorIndexServiceProvider = auditVectorIndexServiceProvider;
        this.vectorStoreRepositoryProvider = vectorStoreRepositoryProvider;
        this.properties = properties;
    }

    @Override
    public AuditVectorTask createIndexTask(Long resourceId, String operator)
    {
        return createTask(resourceId, AuditVectorTask.TYPE_INDEX, operator);
    }

    @Override
    public AuditVectorTask createReindexTask(Long resourceId, String operator)
    {
        return createTask(resourceId, AuditVectorTask.TYPE_REINDEX, operator);
    }

    @Override
    public AuditVectorTask createDeleteTask(Long resourceId, String operator)
    {
        if (!properties.isEnabled() || resourceId == null)
        {
            return null;
        }
        String safeOperator = StringUtils.defaultIfBlank(operator, "system");
        AuditVectorTask task = new AuditVectorTask();
        task.setResourceId(resourceId);
        task.setTaskType(AuditVectorTask.TYPE_DELETE);
        task.setTaskStatus(AuditVectorTask.STATUS_PENDING);
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setProgressText("等待清理向量索引");
        task.setCreateBy(safeOperator);
        task.setUpdateBy(safeOperator);
        auditVectorTaskMapper.insertAuditVectorTask(task);
        return task;
    }

    @Override
    public int cancelPendingTasks(Long[] resourceIds, String operator)
    {
        if (resourceIds == null || resourceIds.length == 0)
        {
            return 0;
        }
        return auditVectorTaskMapper.cancelPendingTasksByResourceIds(resourceIds,
                StringUtils.defaultIfBlank(operator, "system"));
    }

    @Override
    public AuditVectorTask selectLatestTask(Long resourceId)
    {
        return auditVectorTaskMapper.selectLatestAuditVectorTaskByResourceId(resourceId);
    }

    @Override
    public int bootstrapCommonResources(String operator)
    {
        if (!properties.isEnabled())
        {
            return 0;
        }
        List<AuditCommonResource> resources = auditLibraryMapper.selectAuditCommonResourceList(new AuditCommonResource());
        int created = 0;
        for (AuditCommonResource resource : resources)
        {
            AuditVectorTask task = createIndexTask(resource.getResourceId(), operator);
            if (task != null && AuditVectorTask.STATUS_PENDING.equals(task.getTaskStatus()))
            {
                created++;
            }
        }
        return created;
    }

    @Override
    public int runPendingTasks()
    {
        if (!properties.isEnabled() || !properties.getTask().isEnabled())
        {
            return 0;
        }
        AuditVectorIndexService auditVectorIndexService = auditVectorIndexServiceProvider.getIfAvailable();
        if (auditVectorIndexService == null)
        {
            return 0;
        }
        int running = auditVectorTaskMapper.countRunningTasks();
        int capacity = Math.max(0, properties.getTask().getMaxRunning() - running);
        if (capacity <= 0)
        {
            return 0;
        }
        int batchSize = Math.min(Math.max(1, properties.getTask().getBatchSize()), capacity);
        List<AuditVectorTask> tasks = auditVectorTaskMapper.selectPendingAuditVectorTasks(batchSize);
        int handled = 0;
        for (AuditVectorTask task : tasks)
        {
            if (auditVectorTaskMapper.markTaskRunning(task.getTaskId(), "system") <= 0)
            {
                continue;
            }
            AuditVectorTask runningTask = auditVectorTaskMapper.selectAuditVectorTaskById(task.getTaskId());
            try
            {
                handleRunningTask(auditVectorIndexService, runningTask, "system");
            }
            catch (RuntimeException e)
            {
                // 单个文件失败不阻断本轮后续任务。
            }
            handled++;
        }
        return handled;
    }

    @Override
    public AuditVectorIndexResult runResourceNow(Long resourceId, String operator)
    {
        if (!properties.isEnabled())
        {
            throw new IllegalStateException("向量功能未启用");
        }
        AuditVectorIndexService auditVectorIndexService = auditVectorIndexServiceProvider.getIfAvailable();
        if (auditVectorIndexService == null)
        {
            throw new IllegalStateException("向量索引服务不可用");
        }
        AuditVectorTask task = createReindexTask(resourceId, operator);
        if (task == null)
        {
            throw new IllegalStateException("向量任务创建失败");
        }
        if (auditVectorTaskMapper.markTaskRunning(task.getTaskId(), operator) <= 0)
        {
            task = auditVectorTaskMapper.selectLatestAuditVectorTaskByResourceId(resourceId);
        }
        task = auditVectorTaskMapper.selectAuditVectorTaskById(task.getTaskId());
        return handleRunningTask(auditVectorIndexService, task, operator);
    }

    private AuditVectorTask createTask(Long resourceId, String taskType, String operator)
    {
        if (!properties.isEnabled())
        {
            return null;
        }
        if (resourceId == null || auditLibraryMapper.selectAuditCommonResourceById(resourceId) == null)
        {
            return null;
        }
        if (auditVectorTaskMapper.countActiveTaskByResourceId(resourceId) > 0)
        {
            return auditVectorTaskMapper.selectLatestAuditVectorTaskByResourceId(resourceId);
        }
        String safeOperator = StringUtils.defaultIfBlank(operator, "system");
        String safeTaskType = StringUtils.defaultIfBlank(taskType, AuditVectorTask.TYPE_INDEX);
        AuditVectorTask failedTask = auditVectorTaskMapper.selectLatestFailedTask(resourceId, safeTaskType);
        String progressText = AuditVectorTask.TYPE_REINDEX.equals(safeTaskType) ? "等待重新向量化" : "等待向量化任务执行";
        if (failedTask != null)
        {
            auditVectorTaskMapper.resetTaskPending(failedTask.getTaskId(), progressText, safeOperator);
            updateResourceStatus(resourceId, RESOURCE_STATUS_PENDING, progressText, safeOperator);
            return auditVectorTaskMapper.selectAuditVectorTaskById(failedTask.getTaskId());
        }
        AuditVectorTask task = new AuditVectorTask();
        task.setResourceId(resourceId);
        task.setTaskType(safeTaskType);
        task.setTaskStatus(AuditVectorTask.STATUS_PENDING);
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setProgressText(progressText);
        task.setCreateBy(safeOperator);
        task.setUpdateBy(safeOperator);
        auditVectorTaskMapper.insertAuditVectorTask(task);
        updateResourceStatus(resourceId, RESOURCE_STATUS_PENDING, progressText, safeOperator);
        return task;
    }

    private AuditVectorIndexResult handleRunningTask(AuditVectorIndexService auditVectorIndexService,
            AuditVectorTask task, String operator)
    {
        try
        {
            if (AuditVectorTask.TYPE_DELETE.equals(task.getTaskType()))
            {
                VectorStoreRepository repository = vectorStoreRepositoryProvider.getIfAvailable();
                if (repository == null)
                {
                    throw new IllegalStateException("向量仓储服务不可用");
                }
                repository.deleteByResourceIds(AuditVectorDocument.RESOURCE_TYPE_COMMON,
                        Arrays.asList(task.getResourceId()));
                auditVectorTaskMapper.markTaskSuccess(task.getTaskId(), operator);
                return AuditVectorIndexResult.of(AuditVectorTask.STATUS_SUCCESS, null, 0, "向量索引已清理");
            }
            AuditVectorIndexResult result = auditVectorIndexService.index(task);
            if (AuditVectorTask.STATUS_SKIPPED.equals(result.getStatus()))
            {
                auditVectorTaskMapper.markTaskSkipped(task.getTaskId(), result.getMessage(), operator);
            }
            else
            {
                auditVectorTaskMapper.markTaskSuccess(task.getTaskId(), operator);
            }
            return result;
        }
        catch (RuntimeException e)
        {
            String errorMsg = safeError(e.getMessage());
            auditVectorTaskMapper.markTaskFailed(task.getTaskId(), errorMsg, operator);
            throw e;
        }
    }

    private void updateResourceStatus(Long resourceId, String storageStatus, String progressText, String operator)
    {
        AuditCommonResource update = new AuditCommonResource();
        update.setResourceId(resourceId);
        update.setStorageStatus(storageStatus);
        update.setProgressText(safeProgressText(progressText));
        update.setUpdateBy(operator);
        auditLibraryMapper.updateAuditCommonResource(update);
    }

    private String safeProgressText(String message)
    {
        String safeMessage = StringUtils.defaultString(message);
        return safeMessage.length() > RESOURCE_PROGRESS_TEXT_MAX_LENGTH
                ? safeMessage.substring(0, RESOURCE_PROGRESS_TEXT_MAX_LENGTH) : safeMessage;
    }

    private String safeError(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return "向量入库失败";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
