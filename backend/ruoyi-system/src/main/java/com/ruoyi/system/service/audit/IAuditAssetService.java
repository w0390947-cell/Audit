package com.ruoyi.system.service.audit;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.audit.AuditAssetRecord;
import com.ruoyi.system.domain.audit.AuditAssetResubmitRecord;
import com.ruoyi.system.domain.audit.AuditAssetStats;

public interface IAuditAssetService
{
    List<AuditAssetRecord> selectAuditAssetRecordList(AuditAssetRecord record);

    AuditAssetStats selectAuditAssetStats();

    List<String> selectReviewerList();

    AuditAssetRecord selectAuditAssetRecordDetail(Long assetId);

    int updatePermissionOwner(Long assetId, String permissionOwner, String updateBy);

    Map<String, Object> batchDownload(Long[] assetIds);

    Map<String, Object> batchPackage(Long[] assetIds);

    int reuploadAuditAsset(AuditAssetResubmitRecord record, String updateBy);

    int deleteAuditAssetRecordByIds(Long[] assetIds);
}
