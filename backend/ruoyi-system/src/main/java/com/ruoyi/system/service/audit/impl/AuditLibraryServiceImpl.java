package com.ruoyi.system.service.audit.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAssetRecord;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditCommonResourceVersion;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditTaskResource;
import com.ruoyi.system.mapper.audit.AuditAssetMapper;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.IAuditLibraryService;
import com.ruoyi.system.service.audit.support.AuditBusinessPermissionUtils;
import com.ruoyi.system.service.audit.vector.AuditVectorLifecycleService;

@Service
public class AuditLibraryServiceImpl implements IAuditLibraryService
{
    private static final String LIBRARY_TYPE_AUDIT = "audit";

    private static final String TASK_RESOURCE_FOLDER_NAME = "任务文件资源";

    private static final String RESOURCE_STATUS_REVIEWING = "reviewing";

    private static final String RESOURCE_PROGRESS_REVIEWING = "待审核通过后向量化";

    @Autowired
    private AuditLibraryMapper auditLibraryMapper;

    @Autowired
    private AuditAssetMapper auditAssetMapper;

    @Autowired
    private ObjectProvider<AuditVectorLifecycleService> auditVectorLifecycleServiceProvider;

    @Override
    public List<AuditLibraryFolder> selectAuditLibraryFolderList(AuditLibraryFolder folder)
    {
        normalizeLibraryType(folder);
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
        normalizeLibraryType(folder);
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
        normalizeLibraryType(resource);
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
        normalizeLibraryType(resource);
        normalizeCommonSubmitterResource(resource);
        if (StringUtils.isBlank(resource.getCurrentVersionNo()))
        {
            resource.setCurrentVersionNo("v1.0");
        }
        if (resource.getLatestModifyTime() == null)
        {
            resource.setLatestModifyTime(new Date());
        }
        resource.setStorageStatus(RESOURCE_STATUS_REVIEWING);
        resource.setProgressText(RESOURCE_PROGRESS_REVIEWING);
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
            createOrResetAssetRecord(resource);
        }
        return rows;
    }

    private void normalizeCommonSubmitterResource(AuditCommonResource resource)
    {
        if (!AuditBusinessPermissionUtils.isCommonSubmitterOnly() || resource == null)
        {
            return;
        }
        String username = AuditBusinessPermissionUtils.getCurrentUsername();
        if (StringUtils.isBlank(username))
        {
            return;
        }
        resource.setCreateBy(StringUtils.defaultIfBlank(resource.getCreateBy(), username));
        resource.setUpdateBy(StringUtils.defaultIfBlank(resource.getUpdateBy(), username));
        resource.setCreator(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAuditCommonResource(AuditCommonResource resource)
    {
        normalizeLibraryType(resource);
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
        boolean fileChanged = StringUtils.isNotBlank(resource.getFileUrl())
                && !Objects.equals(detail.getFileUrl(), resource.getFileUrl());
        if (fileChanged)
        {
            resource.setStorageStatus(RESOURCE_STATUS_REVIEWING);
            resource.setProgressText(RESOURCE_PROGRESS_REVIEWING);
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
            if (fileChanged)
            {
                createOrResetAssetRecord(after);
                if (lifecycleService != null)
                {
                    lifecycleService.onCommonResourcesDeleted(new ArrayList<>(Arrays.asList(after.getResourceId())));
                }
            }
            else if (lifecycleService != null)
            {
                lifecycleService.onCommonResourceUpdated(detail, after);
            }
        }
        return rows;
    }

    @Override
    public int assignAuditCommonResourceFolder(AuditCommonResource resource)
    {
        normalizeLibraryType(resource);
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
    public List<AuditCommonResource> selectAuditTaskCommonResourceList(AuditCommonResource resource)
    {
        resource.setLibraryType(LIBRARY_TYPE_AUDIT);
        Set<Long> taskFolderIds = collectTaskResourceFolderIds();
        if (taskFolderIds.isEmpty())
        {
            return Collections.emptyList();
        }
        if (resource.getFolderId() != null && !taskFolderIds.contains(resource.getFolderId()))
        {
            return Collections.emptyList();
        }
        if (resource.getFolderId() == null)
        {
            resource.setFolderIds(taskFolderIds.toArray(new Long[0]));
        }
        return auditLibraryMapper.selectAuditCommonResourceList(resource);
    }

    @Override
    public AuditCommonResource selectAuditTaskCommonResourceDetail(Long resourceId)
    {
        AuditCommonResource detail = selectAuditCommonResourceDetail(resourceId);
        if (detail == null || !collectTaskResourceFolderIds().contains(detail.getFolderId()))
        {
            return null;
        }
        return detail;
    }

    @Override
    public int assignAuditTaskCommonResourceFolder(AuditCommonResource resource)
    {
        Set<Long> taskFolderIds = collectTaskResourceFolderIds();
        if (resource == null || resource.getResourceId() == null || resource.getFolderId() == null
                || !taskFolderIds.contains(resource.getFolderId()))
        {
            return 0;
        }
        AuditCommonResource detail = auditLibraryMapper.selectAuditCommonResourceById(resource.getResourceId());
        if (detail == null || !taskFolderIds.contains(detail.getFolderId()))
        {
            return 0;
        }
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
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditTaskCommonResourceByIds(Long[] resourceIds)
    {
        if (resourceIds == null || resourceIds.length == 0)
        {
            return 0;
        }
        Set<Long> taskFolderIds = collectTaskResourceFolderIds();
        List<Long> validIds = new ArrayList<>();
        for (Long resourceId : resourceIds)
        {
            AuditCommonResource detail = auditLibraryMapper.selectAuditCommonResourceById(resourceId);
            if (detail != null && taskFolderIds.contains(detail.getFolderId()))
            {
                validIds.add(resourceId);
            }
        }
        if (validIds.isEmpty())
        {
            return 0;
        }
        Long[] ids = validIds.toArray(new Long[0]);
        auditLibraryMapper.deleteAuditCommonResourceVersionByResourceIds(ids);
        int rows = auditLibraryMapper.deleteAuditCommonResourceByIds(ids);
        if (rows > 0)
        {
            AuditVectorLifecycleService lifecycleService = auditVectorLifecycleServiceProvider.getIfAvailable();
            if (lifecycleService != null)
            {
                lifecycleService.onCommonResourcesDeleted(validIds);
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

    private Set<Long> collectTaskResourceFolderIds()
    {
        AuditLibraryFolder root = findTaskResourceRootFolder();
        if (root == null || root.getFolderId() == null)
        {
            return Collections.emptySet();
        }
        return collectDescendantFolderIds(new Long[] { root.getFolderId() });
    }

    private AuditLibraryFolder findTaskResourceRootFolder()
    {
        AuditLibraryFolder query = new AuditLibraryFolder();
        query.setLibraryType(LIBRARY_TYPE_AUDIT);
        query.setParentId(0L);
        query.setFolderName(TASK_RESOURCE_FOLDER_NAME);
        List<AuditLibraryFolder> folders = auditLibraryMapper.selectAuditLibraryFolderList(query);
        if (folders == null)
        {
            return null;
        }
        for (AuditLibraryFolder folder : folders)
        {
            if (TASK_RESOURCE_FOLDER_NAME.equals(folder.getFolderName())
                    && (folder.getParentId() == null || folder.getParentId() == 0L))
            {
                return folder;
            }
        }
        return null;
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

    private void normalizeLibraryType(AuditLibraryFolder folder)
    {
        if (folder != null && StringUtils.isBlank(folder.getLibraryType()))
        {
            folder.setLibraryType(LIBRARY_TYPE_AUDIT);
        }
    }

    private void normalizeLibraryType(AuditCommonResource resource)
    {
        if (resource != null && StringUtils.isBlank(resource.getLibraryType()))
        {
            resource.setLibraryType(LIBRARY_TYPE_AUDIT);
        }
    }

    private void createOrResetAssetRecord(AuditCommonResource resource)
    {
        if (resource == null || resource.getResourceId() == null)
        {
            return;
        }
        AuditAssetRecord record = buildAssetRecord(resource);
        AuditAssetRecord existing = auditAssetMapper.selectAuditAssetRecordByLibraryResourceId(resource.getResourceId());
        if (existing == null)
        {
            auditAssetMapper.insertAuditAssetRecord(record);
        }
        else
        {
            auditAssetMapper.resetAuditAssetForLibraryResource(record);
        }
    }

    private AuditAssetRecord buildAssetRecord(AuditCommonResource resource)
    {
        AuditAssetRecord record = new AuditAssetRecord();
        record.setLibraryResourceId(resource.getResourceId());
        record.setTaskNo("LIB-" + resource.getResourceId());
        record.setProductName(StringUtils.defaultIfBlank(resource.getDocumentName(), resource.getFileName()));
        record.setDeliveryUnit(StringUtils.defaultString(resource.getFolderName()));
        record.setSubmitter(StringUtils.defaultIfBlank(resource.getCreator(), resource.getCreateBy()));
        record.setReviewer("");
        record.setPermissionOwner("");
        record.setAiAnalysisCount(0);
        record.setCurrentAiVersion(StringUtils.defaultIfBlank(resource.getCurrentVersionNo(), "v1.0"));
        record.setReviewStatus("reviewing");
        record.setReportFileName(resource.getFileName());
        record.setReportFileUrl(resource.getFileUrl());
        record.setAiOpinion("文件已提交，等待审核资源列表人工审核。");
        record.setFinalOpinion("待审核通过后启动向量化入库。");
        record.setCreateBy(StringUtils.defaultIfBlank(resource.getCreateBy(), resource.getUpdateBy()));
        record.setUpdateBy(StringUtils.defaultIfBlank(resource.getUpdateBy(), resource.getCreateBy()));
        record.setRemark("由审核文件库上传自动创建");
        return record;
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
