package com.ruoyi.system.service.audit;

import java.util.List;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;

public interface IAuditReviewService
{
    List<AuditReviewTask> selectAuditReviewTaskList(AuditReviewTask task);

    AuditReviewTask selectAuditReviewTaskDetail(Long taskId, Long versionId);

    List<AuditReviewVersion> selectAuditReviewVersionListByTaskId(Long taskId);

    int insertAuditReviewTask(AuditReviewTask task);

    int updateAuditReviewTask(AuditReviewTask task);

    int deleteAuditReviewTaskByIds(Long[] taskIds);

    int updateProcessFlag(Long taskId, String processFlag, String operName);
}
