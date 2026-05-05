package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.AuditCommonResource;
import java.util.List;

public interface AuditVectorLifecycleService
{
    void onCommonResourceCreated(AuditCommonResource resource);

    void onCommonResourceUpdated(AuditCommonResource before, AuditCommonResource after);

    void onCommonResourceMoved(Long resourceId, Long folderId);

    void onCommonResourcesDeleted(List<Long> resourceIds);

    void onFoldersDeleted(List<Long> folderIds);
}
