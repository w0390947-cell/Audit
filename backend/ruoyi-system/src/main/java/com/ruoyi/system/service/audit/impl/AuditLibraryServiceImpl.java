package com.ruoyi.system.service.audit.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditCommonResourceVersion;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditTaskResource;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.IAuditLibraryService;
import com.ruoyi.system.service.audit.vector.AuditVectorLifecycleService;

@Service
public class AuditLibraryServiceImpl implements IAuditLibraryService
{
    @Autowired
    private AuditLibraryMapper auditLibraryMapper;

    @Autowired
    private ObjectProvider<AuditVectorLifecycleService> auditVectorLifecycleServiceProvider;

    @Override
    public List<AuditLibraryFolder> selectAuditLibraryFolderList(AuditLibraryFolder folder)
    {
        return auditLibraryMapper.selectAuditLibraryFolderList(folder);
    }

    @Override
    public AuditLibraryFolder selectAuditLibraryFolderById(Long folderId)
    {
        return auditLibraryMapper.selectAuditLibraryFolderById(folderId);
    }

    @Override
    public int insertAuditLibraryFolder(AuditLibraryFolder folder)
    {
        if (folder.getParentId() == null)
        {
            folder.setParentId(0L);
        }
        if (StringUtils.isBlank(folder.getVisibleScope()))
        {
            folder.setVisibleScope("all");
        }
        if (StringUtils.isBlank(folder.getTopFlag()))
        {
            folder.setTopFlag("0");
        }
        folder.setUpdateBy(folder.getCreateBy());
        return auditLibraryMapper.insertAuditLibraryFolder(folder);
    }

    @Override
    public int updateAuditLibraryFolder(AuditLibraryFolder folder)
    {
        if (folder.getParentId() != null && folder.getFolderId() != null)
        {
            if (folder.getFolderId().equals(folder.getParentId()))
            {
                return 0;
            }
            if (folder.getParentId() > 0)
            {
                Set<Long> descendantIds = collectDescendantFolderIds(new Long[] { folder.getFolderId() });
                if (descendantIds.contains(folder.getParentId()))
                {
                    return 0;
                }
            }
        }
        return auditLibraryMapper.updateAuditLibraryFolder(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditLibraryFolderByIds(Long[] folderIds)
    {
        Set<Long> descendantIds = collectDescendantFolderIds(folderIds);
        Long[] allFolderIds = descendantIds.toArray(new Long[0]);
        List<Long> resourceIds = auditLibraryMapper.selectAuditCommonResourceIdsByFolderIds(allFolderIds);
        auditLibraryMapper.deleteAuditCommonResourceVersionByFolderIds(allFolderIds);
        auditLibraryMapper.deleteAuditCommonResourceByFolderIds(allFolderIds);
        auditLibraryMapper.deleteAuditTaskResourceByFolderIds(allFolderIds);
        int rows = auditLibraryMapper.deleteAuditLibraryFolderByIds(allFolderIds);
        if (rows > 0)
        {
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourcesDeleted(resourceIds);
            }
        }
        return rows;
    }

    @Override
    public List<AuditCommonResource> selectAuditCommonResourceList(AuditCommonResource resource)
    {
        return auditLibraryMapper.selectAuditCommonResourceList(resource);
    }

    @Override
    public AuditCommonResource selectAuditCommonResourceDetail(Long resourceId)
    {
        AuditCommonResource detail = auditLibraryMapper.selectAuditCommonResourceById(resourceId);
        if (detail != null)
        {
            detail.setVersionList(auditLibraryMapper.selectAuditCommonResourceVersionListByResourceId(resourceId));
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAuditCommonResource(AuditCommonResource resource)
    {
        if (StringUtils.isBlank(resource.getCurrentVersionNo()))
        {
            resource.setCurrentVersionNo("v1.0");
        }
        if (resource.getLatestModifyTime() == null)
        {
            resource.setLatestModifyTime(new Date());
        }
        resource.setCreator(StringUtils.defaultIfBlank(resource.getCreator(), resource.getCreateBy()));
        int rows = auditLibraryMapper.insertAuditCommonResource(resource);
        if (rows > 0)
        {
            AuditCommonResourceVersion version = new AuditCommonResourceVersion();
            version.setResourceId(resource.getResourceId());
            version.setVersionNo(resource.getCurrentVersionNo());
            version.setFileName(resource.getFileName());
            version.setFileUrl(resource.getFileUrl());
            version.setFileSize(resource.getFileSize());
            version.setCreator(resource.getCreator());
            version.setCreateTime(resource.getLatestModifyTime());
            auditLibraryMapper.insertAuditCommonResourceVersion(version);
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourceCreated(resource);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAuditCommonResource(AuditCommonResource resource)
    {
        AuditCommonResource detail = auditLibraryMapper.selectAuditCommonResourceById(resource.getResourceId());
        if (detail == null)
        {
            return 0;
        }
        resource.setLatestModifyTime(new Date());
        if (StringUtils.isBlank(resource.getCurrentVersionNo()))
        {
            resource.setCurrentVersionNo(nextVersionNo(detail.getCurrentVersionNo()));
        }
        int rows = auditLibraryMapper.updateAuditCommonResource(resource);
        if (rows > 0)
        {
            AuditCommonResourceVersion version = new AuditCommonResourceVersion();
            version.setResourceId(resource.getResourceId());
            version.setVersionNo(resource.getCurrentVersionNo());
            version.setFileName(resource.getFileName());
            version.setFileUrl(resource.getFileUrl());
            version.setFileSize(resource.getFileSize());
            version.setCreator(StringUtils.defaultIfBlank(resource.getCreator(), detail.getCreator()));
            version.setCreateTime(resource.getLatestModifyTime());
            auditLibraryMapper.insertAuditCommonResourceVersion(version);
            AuditCommonResource after = auditLibraryMapper.selectAuditCommonResourceById(resource.getResourceId());
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourceUpdated(detail, after);
            }
        }
        return rows;
    }

    @Override
    public int assignAuditCommonResourceFolder(AuditCommonResource resource)
    {
        int rows = auditLibraryMapper.updateAuditCommonResourceFolder(resource);
        if (rows > 0)
        {
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourceMoved(resource.getResourceId(), resource.getFolderId());
            }
        }
        return rows;
    }

    @Override
    public int assignAuditTaskResourceFolder(AuditTaskResource resource)
    {
        return auditLibraryMapper.updateAuditTaskResourceFolder(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditCommonResourceByIds(Long[] resourceIds)
    {
        auditLibraryMapper.deleteAuditCommonResourceVersionByResourceIds(resourceIds);
        int rows = auditLibraryMapper.deleteAuditCommonResourceByIds(resourceIds);
        if (rows > 0)
        {
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourcesDeleted(new ArrayList<>(Arrays.asList(resourceIds)));
            }
        }
        return rows;
    }

    @Override
    public List<AuditTaskResource> selectAuditTaskResourceList(AuditTaskResource resource)
    {
        return auditLibraryMapper.selectAuditTaskResourceList(resource);
    }

    @Override
    public int reuploadAuditTaskResource(AuditTaskResource resource)
    {
        resource.setArchiveTime(new Date());
        resource.setCollectStatus("processing");
        return auditLibraryMapper.updateAuditTaskResource(resource);
    }

    @Override
    public int deleteAuditTaskResourceByIds(Long[] resourceIds)
    {
        return auditLibraryMapper.deleteAuditTaskResourceByIds(resourceIds);
    }

    private String nextVersionNo(String currentVersionNo)
    {
        if (StringUtils.isBlank(currentVersionNo) || !currentVersionNo.startsWith("v"))
        {
            return "v1.0";
        }
        String numberText = currentVersionNo.substring(1).replace(".0", "");
        int versionNumber = 1;
        if (StringUtils.isNumeric(numberText))
        {
            versionNumber = Integer.parseInt(numberText) + 1;
        }
        return "v" + versionNumber + ".0";
    }

    private Set<Long> collectDescendantFolderIds(Long[] folderIds)
    {
        Set<Long> allIds = new HashSet<>();
        if (folderIds == null)
        {
            return allIds;
        }
        for (Long folderId : folderIds)
        {
            if (folderId != null)
            {
                allIds.add(folderId);
            }
        }
        List<AuditLibraryFolder> folders = auditLibraryMapper.selectAuditLibraryFolderList(new AuditLibraryFolder());
        boolean changed = true;
        while (changed)
        {
            changed = false;
            for (AuditLibraryFolder folder : folders)
            {
                if (folder.getParentId() != null && allIds.contains(folder.getParentId()) && allIds.add(folder.getFolderId()))
                {
                    changed = true;
                }
            }
        }
        return allIds;
    }
}
