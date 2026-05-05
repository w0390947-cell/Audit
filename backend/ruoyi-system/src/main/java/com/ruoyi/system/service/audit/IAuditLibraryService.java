package com.ruoyi.system.service.audit;

import java.util.List;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditTaskResource;

public interface IAuditLibraryService
{
    List<AuditLibraryFolder> selectAuditLibraryFolderList(AuditLibraryFolder folder);

    AuditLibraryFolder selectAuditLibraryFolderById(Long folderId);

    int insertAuditLibraryFolder(AuditLibraryFolder folder);

    int updateAuditLibraryFolder(AuditLibraryFolder folder);

    int deleteAuditLibraryFolderByIds(Long[] folderIds);

    List<AuditCommonResource> selectAuditCommonResourceList(AuditCommonResource resource);

    AuditCommonResource selectAuditCommonResourceDetail(Long resourceId);

    int insertAuditCommonResource(AuditCommonResource resource);

    int updateAuditCommonResource(AuditCommonResource resource);

    int assignAuditCommonResourceFolder(AuditCommonResource resource);

    int assignAuditTaskResourceFolder(AuditTaskResource resource);

    int deleteAuditCommonResourceByIds(Long[] resourceIds);

    List<AuditTaskResource> selectAuditTaskResourceList(AuditTaskResource resource);

    int reuploadAuditTaskResource(AuditTaskResource resource);

    int deleteAuditTaskResourceByIds(Long[] resourceIds);
}
