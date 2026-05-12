package com.ruoyi.system.mapper.audit;

import com.ruoyi.system.domain.audit.AuditAiFlowStage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuditAiFlowStageMapper
{
    List<AuditAiFlowStage> selectAuditAiFlowStageListByTaskId(Long aiTaskId);

    List<AuditAiFlowStage> selectAuditAiFlowStageListByTaskIdAndRunId(@Param("aiTaskId") Long aiTaskId,
            @Param("runId") String runId);

    String selectLatestRunIdByTaskId(Long aiTaskId);

    int countAuditAiFlowStageByTaskIdAndRunId(@Param("aiTaskId") Long aiTaskId, @Param("runId") String runId);

    int deleteAuditAiFlowStageByTaskId(Long aiTaskId);

    int deleteAuditAiFlowStageByTaskIdAndRunId(@Param("aiTaskId") Long aiTaskId, @Param("runId") String runId);

    int insertAuditAiFlowStage(AuditAiFlowStage stage);

    int insertAuditAiFlowStageBatch(@Param("list") List<AuditAiFlowStage> list);
}
