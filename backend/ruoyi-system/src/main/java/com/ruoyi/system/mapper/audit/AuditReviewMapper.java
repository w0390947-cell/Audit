package com.ruoyi.system.mapper.audit;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.audit.AuditReviewIssue;
import com.ruoyi.system.domain.audit.AuditReviewStage;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;

public interface AuditReviewMapper
{
    List<AuditReviewTask> selectAuditReviewTaskList(AuditReviewTask task);

    AuditReviewTask selectAuditReviewTaskById(Long taskId);

    int insertAuditReviewTask(AuditReviewTask task);

    int updateAuditReviewTask(AuditReviewTask task);

    int deleteAuditReviewTaskByIds(Long[] taskIds);

    List<AuditReviewVersion> selectAuditReviewVersionListByTaskId(Long taskId);

    AuditReviewVersion selectAuditReviewVersionById(Long versionId);

    Integer selectAuditReviewVersionCountByTaskId(Long taskId);

    int clearCurrentVersionFlag(Long taskId);

    int insertAuditReviewVersion(AuditReviewVersion version);

    int deleteAuditReviewVersionByTaskIds(Long[] taskIds);

    List<AuditReviewStage> selectAuditReviewStageListByVersionId(Long versionId);

    int insertAuditReviewStageBatch(@Param("list") List<AuditReviewStage> list);

    int deleteAuditReviewStageByTaskIds(Long[] taskIds);

    List<AuditReviewIssue> selectAuditReviewIssueListByVersionId(Long versionId);

    int insertAuditReviewIssueBatch(@Param("list") List<AuditReviewIssue> list);

    int deleteAuditReviewIssueByTaskIds(Long[] taskIds);

    int updateProcessFlag(@Param("taskId") Long taskId, @Param("processFlag") String processFlag, @Param("updateBy") String updateBy);
}
