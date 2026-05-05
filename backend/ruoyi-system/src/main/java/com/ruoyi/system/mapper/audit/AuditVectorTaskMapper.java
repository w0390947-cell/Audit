package com.ruoyi.system.mapper.audit;

import com.ruoyi.system.domain.audit.vector.AuditVectorTask;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuditVectorTaskMapper
{
    AuditVectorTask selectAuditVectorTaskById(@Param("taskId") Long taskId);

    AuditVectorTask selectLatestAuditVectorTaskByResourceId(@Param("resourceId") Long resourceId);

    List<AuditVectorTask> selectPendingAuditVectorTasks(@Param("batchSize") int batchSize);

    int countActiveTaskByResourceId(@Param("resourceId") Long resourceId);

    AuditVectorTask selectLatestFailedTask(@Param("resourceId") Long resourceId, @Param("taskType") String taskType);

    int countRunningTasks();

    int insertAuditVectorTask(AuditVectorTask task);

    int markTaskRunning(@Param("taskId") Long taskId, @Param("updateBy") String updateBy);

    int markTaskSuccess(@Param("taskId") Long taskId, @Param("updateBy") String updateBy);

    int markTaskSkipped(@Param("taskId") Long taskId, @Param("errorMsg") String errorMsg,
            @Param("updateBy") String updateBy);

    int markTaskFailed(@Param("taskId") Long taskId, @Param("errorMsg") String errorMsg,
            @Param("updateBy") String updateBy);

    int cancelPendingTasksByResourceIds(@Param("resourceIds") Long[] resourceIds, @Param("updateBy") String updateBy);

    int resetTaskPending(@Param("taskId") Long taskId, @Param("progressText") String progressText,
            @Param("updateBy") String updateBy);
}
