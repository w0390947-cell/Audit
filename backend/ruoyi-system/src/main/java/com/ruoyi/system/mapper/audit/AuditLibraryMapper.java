package com.ruoyi.system.mapper.audit;

import java.util.List;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditCommonResourceVersion;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditTaskResource;

public interface AuditLibraryMapper
{
    List<AuditLibraryFolder> selectAuditLibraryFolderList(AuditLibraryFolder folder);

    AuditLibraryFolder selectAuditLibraryFolderById(Long folderId);

    int insertAuditLibraryFolder(AuditLibraryFolder folder);

    int updateAuditLibraryFolder(AuditLibraryFolder folder);

    int deleteAuditLibraryFolderByIds(Long[] folderIds);

    int deleteAuditCommonResourceVersionByFolderIds(Long[] folderIds);

    int deleteAuditCommonResourceByFolderIds(Long[] folderIds);

    int deleteAuditTaskResourceByFolderIds(Long[] folderIds);

    List<AuditCommonResource> selectAuditCommonResourceList(AuditCommonResource resource);

    AuditCommonResource selectAuditCommonResourceById(Long resourceId);

    List<AuditCommonResourceVersion> selectAuditCommonResourceVersionListByResourceId(Long resourceId);

    int insertAuditCommonResource(AuditCommonResource resource);

    int updateAuditCommonResource(AuditCommonResource resource);

    int insertAuditCommonResourceVersion(AuditCommonResourceVersion version);

    int updateAuditCommonResourceFolder(AuditCommonResource resource);

    int updateAuditTaskResourceFolder(AuditTaskResource resource);

    int deleteAuditCommonResourceVersionByResourceIds(Long[] resourceIds);

    int deleteAuditCommonResourceByIds(Long[] resourceIds);

    List<AuditTaskResource> selectAuditTaskResourceList(AuditTaskResource resource);

    int updateAuditTaskResource(AuditTaskResource resource);

    int deleteAuditTaskResourceByIds(Long[] resourceIds);
}
