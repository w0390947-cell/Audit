package com.ruoyi.system.mapper.audit;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.audit.AuditAssetAiStep;
import com.ruoyi.system.domain.audit.AuditAssetAiVersion;
import com.ruoyi.system.domain.audit.AuditAssetRecord;
import com.ruoyi.system.domain.audit.AuditAssetResubmitRecord;

public interface AuditAssetMapper
{
    List<AuditAssetRecord> selectAuditAssetRecordList(AuditAssetRecord record);

    List<AuditAssetRecord> selectAuditAssetAllList();

    AuditAssetRecord selectAuditAssetRecordById(Long assetId);

    List<AuditAssetAiVersion> selectAuditAssetAiVersionListByAssetId(Long assetId);

    List<AuditAssetAiStep> selectAuditAssetAiStepListByVersionId(Long versionId);

    List<AuditAssetResubmitRecord> selectAuditAssetResubmitRecordListByAssetId(Long assetId);

    List<String> selectAuditAssetReviewerList();

    List<AuditAssetRecord> selectAuditAssetRecordListByIds(Long[] assetIds);

    int updateAuditAssetPermission(@Param("assetId") Long assetId, @Param("permissionOwner") String permissionOwner,
            @Param("updateBy") String updateBy);

    int updateAuditAssetReport(AuditAssetRecord record);

    int insertAuditAssetAiVersion(AuditAssetAiVersion version);

    int updateAuditAssetCurrentVersionFlag(@Param("assetId") Long assetId, @Param("currentFlag") String currentFlag);

    int insertAuditAssetAiStep(AuditAssetAiStep step);

    int insertAuditAssetResubmitRecord(AuditAssetResubmitRecord record);

    int deleteAuditAssetAiStepByAssetIds(Long[] assetIds);

    int deleteAuditAssetAiVersionByAssetIds(Long[] assetIds);

    int deleteAuditAssetResubmitRecordByAssetIds(Long[] assetIds);

    int deleteAuditAssetRecordByIds(Long[] assetIds);
}
