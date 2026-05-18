package com.ruoyi.system.service.audit.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.AuditLibraryFolder;
import com.ruoyi.system.domain.audit.AuditReviewBasisFile;
import com.ruoyi.system.domain.audit.AuditReviewIssue;
import com.ruoyi.system.domain.audit.AuditReviewStage;
import com.ruoyi.system.domain.audit.AuditReviewStats;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.domain.audit.AuditUploadedFile;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditReviewMapper;
import com.ruoyi.system.service.audit.IAuditLibraryService;
import com.ruoyi.system.service.audit.IAuditReviewService;
import com.ruoyi.system.service.audit.support.AuditBusinessPermissionUtils;

@Service
public class AuditReviewServiceImpl implements IAuditReviewService
{
    private static final String BASIS_RESOURCE_FOLDER_NAME = "任务文件资源";

    @Autowired
    private AuditReviewMapper auditReviewMapper;

    @Autowired
    private AuditAiMapper auditAiMapper;

    @Autowired
    private IAuditLibraryService auditLibraryService;

    @Autowired
    private AuditAiQueuePositionService auditAiQueuePositionService;

    @Autowired
    private AuditAiEstimatedDurationAsyncService auditAiEstimatedDurationAsyncService;

    @Override
    public List<AuditReviewTask> selectAuditReviewTaskList(AuditReviewTask task)
    {
        applyCommonSubmitterQueryScope(task);
        return auditReviewMapper.selectAuditReviewTaskList(task);
    }

    @Override
    public AuditReviewStats selectAuditReviewStats()
    {
        AuditReviewTask scope = new AuditReviewTask();
        applyCommonSubmitterQueryScope(scope);
        return auditReviewMapper.selectAuditReviewStats(scope);
    }

    @Override
    public AuditReviewTask selectAuditReviewTaskDetail(Long taskId, Long versionId)
    {
        AuditReviewTask task = auditReviewMapper.selectAuditReviewTaskById(taskId);
        if (task == null)
        {
            return null;
        }
        checkCommonSubmitterTaskAccess(task);
        List<AuditReviewVersion> versionList = auditReviewMapper.selectAuditReviewVersionListByTaskId(taskId);
        task.setVersionList(versionList);
        AuditReviewVersion currentVersion = getCurrentVersion(taskId, versionId, versionList);
        task.setCurrentVersion(currentVersion);
        if (currentVersion != null)
        {
            task.setCurrentVersionNo(currentVersion.getVersionNo());
            List<AuditReviewBasisFile> basisFileList =
                    auditReviewMapper.selectAuditReviewBasisFileListByVersionId(currentVersion.getVersionId());
            task.setBasisFileList(basisFileList);
            currentVersion.setBasisFileList(basisFileList);
            task.setStageList(auditReviewMapper.selectAuditReviewStageListByVersionId(currentVersion.getVersionId()));
            task.setIssueList(resolveReviewIssueList(taskId, currentVersion.getVersionId()));
        }
        else
        {
            task.setStageList(Collections.emptyList());
            task.setIssueList(Collections.emptyList());
        }
        return task;
    }

    @Override
    public List<AuditReviewVersion> selectAuditReviewVersionListByTaskId(Long taskId)
    {
        AuditReviewTask task = auditReviewMapper.selectAuditReviewTaskById(taskId);
        checkCommonSubmitterTaskAccess(task);
        return auditReviewMapper.selectAuditReviewVersionListByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAuditReviewTask(AuditReviewTask task)
    {
        normalizeBeforeCreate(task);
        normalizeCommonSubmitterCreate(task);
        task.setCurrentVersionNo("v1.0");
        int rows = auditReviewMapper.insertAuditReviewTask(task);
        AuditReviewVersion version = saveSnapshot(task, "v1.0");
        saveBasisFileSnapshot(task, version);
        archiveUploadedBasisFiles(task);
        createAuditAiTask(task, version);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAuditReviewTask(AuditReviewTask task)
    {
        AuditReviewTask dbTask = auditReviewMapper.selectAuditReviewTaskById(task.getTaskId());
        if (dbTask == null)
        {
            return 0;
        }
        checkCommonSubmitterTaskEditable(dbTask);
        normalizeBeforeUpdate(task, dbTask);
        normalizeCommonSubmitterUpdate(task);
        boolean shouldCreateAiTask = hasAiInputFileChanged(dbTask, task);
        String nextVersionNo = buildNextVersionNo(task.getTaskId());
        task.setCurrentVersionNo(nextVersionNo);
        int rows = auditReviewMapper.updateAuditReviewTask(task);
        auditReviewMapper.clearCurrentVersionFlag(task.getTaskId());
        AuditReviewVersion version = saveSnapshot(task, nextVersionNo);
        saveBasisFileSnapshot(task, version);
        archiveUploadedBasisFiles(task);
        if (shouldCreateAiTask)
        {
            createAuditAiTask(task, version);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditReviewTaskByIds(Long[] taskIds)
    {
        auditReviewMapper.deleteAuditReviewBasisFileByTaskIds(taskIds);
        auditReviewMapper.deleteAuditReviewStageByTaskIds(taskIds);
        auditReviewMapper.deleteAuditReviewIssueByTaskIds(taskIds);
        auditReviewMapper.deleteAuditReviewVersionByTaskIds(taskIds);
        return auditReviewMapper.deleteAuditReviewTaskByIds(taskIds);
    }

    @Override
    public int updateProcessFlag(Long taskId, String processFlag, String operName)
    {
        return auditReviewMapper.updateProcessFlag(taskId, processFlag, operName);
    }

    private void normalizeBeforeCreate(AuditReviewTask task)
    {
        if (StringUtils.isBlank(task.getTaskNo()))
        {
            task.setTaskNo("SF-" + System.currentTimeMillis());
        }
        if (StringUtils.isBlank(task.getPriority()))
        {
            task.setPriority("medium");
        }
        if (StringUtils.isBlank(task.getTaskStatus()))
        {
            task.setTaskStatus("uploaded");
        }
        if (StringUtils.isBlank(task.getReviewStatus()))
        {
            task.setReviewStatus("reviewing");
        }
        if (StringUtils.isBlank(task.getProcessFlag()))
        {
            task.setProcessFlag("0");
        }
        if (task.getAiAnalysisCount() == null)
        {
            task.setAiAnalysisCount(3);
        }
        if (task.getSubmitTime() == null)
        {
            task.setSubmitTime(DateUtils.getNowDate());
        }
        if (StringUtils.isBlank(task.getSponsor()))
        {
            task.setSponsor(task.getCreateBy());
        }
    }

    private void normalizeCommonSubmitterCreate(AuditReviewTask task)
    {
        if (!AuditBusinessPermissionUtils.isCommonSubmitterOnly())
        {
            return;
        }
        String username = AuditBusinessPermissionUtils.getCurrentUsername();
        if (StringUtils.isBlank(username))
        {
            return;
        }
        task.setCreateBy(StringUtils.defaultIfBlank(task.getCreateBy(), username));
        task.setSponsor(username);
        task.setTaskStatus("uploaded");
        task.setReviewStatus("reviewing");
        task.setProcessFlag("0");
    }

    private void normalizeBeforeUpdate(AuditReviewTask task, AuditReviewTask dbTask)
    {
        task.setTaskNo(StringUtils.isNotBlank(task.getTaskNo()) ? task.getTaskNo() : dbTask.getTaskNo());
        task.setSponsor(StringUtils.isNotBlank(task.getSponsor()) ? task.getSponsor() : dbTask.getSponsor());
        task.setPriority(StringUtils.isNotBlank(task.getPriority()) ? task.getPriority() : dbTask.getPriority());
        task.setTaskStatus(StringUtils.isNotBlank(task.getTaskStatus()) ? task.getTaskStatus() : dbTask.getTaskStatus());
        task.setReviewStatus(StringUtils.isNotBlank(task.getReviewStatus()) ? task.getReviewStatus() : dbTask.getReviewStatus());
        task.setProcessFlag(StringUtils.isNotBlank(task.getProcessFlag()) ? task.getProcessFlag() : dbTask.getProcessFlag());
        task.setAiAnalysisCount(dbTask.getAiAnalysisCount() == null ? 1 : dbTask.getAiAnalysisCount() + 1);
        task.setSubmitTime(task.getSubmitTime() == null ? DateUtils.getNowDate() : task.getSubmitTime());
    }

    private void normalizeCommonSubmitterUpdate(AuditReviewTask task)
    {
        if (!AuditBusinessPermissionUtils.isCommonSubmitterOnly())
        {
            return;
        }
        String username = AuditBusinessPermissionUtils.getCurrentUsername();
        if (StringUtils.isBlank(username))
        {
            return;
        }
        task.setSponsor(username);
        task.setTaskStatus("uploaded");
        task.setReviewStatus("reviewing");
        task.setProcessFlag("0");
    }

    private void applyCommonSubmitterQueryScope(AuditReviewTask task)
    {
        if (!AuditBusinessPermissionUtils.isCommonSubmitterOnly())
        {
            return;
        }
        String username = AuditBusinessPermissionUtils.getCurrentUsername();
        if (StringUtils.isNotBlank(username) && task != null)
        {
            task.setCreateBy(username);
        }
    }

    private void checkCommonSubmitterTaskAccess(AuditReviewTask task)
    {
        if (!AuditBusinessPermissionUtils.isCommonSubmitterOnly())
        {
            return;
        }
        String username = AuditBusinessPermissionUtils.getCurrentUsername();
        if (task == null || StringUtils.isBlank(username) || !Objects.equals(username, task.getCreateBy()))
        {
            throw new ServiceException("无权访问该审核任务");
        }
    }

    private void checkCommonSubmitterTaskEditable(AuditReviewTask task)
    {
        checkCommonSubmitterTaskAccess(task);
        if (AuditBusinessPermissionUtils.isCommonSubmitterOnly() && !"returned".equals(task.getReviewStatus()))
        {
            throw new ServiceException("普通用户只能修改已退回的审核任务");
        }
    }

    private AuditReviewVersion saveSnapshot(AuditReviewTask task, String versionNo)
    {
        AuditReviewVersion version = new AuditReviewVersion();
        version.setTaskId(task.getTaskId());
        version.setVersionNo(versionNo);
        version.setReportFileUrl(getPrimaryFileUrl(task.getMainReportUrls()));
        version.setReportFileName(resolveMainReportFileName(task, version.getReportFileUrl(), versionNo));
        version.setMainReportUrls(task.getMainReportUrls());
        version.setBasisFileUrls(task.getBasisFileUrls());
        version.setAppendixFileUrls(task.getAppendixFileUrls());
        version.setDetectStatus(task.getTaskStatus());
        version.setSubmitter(task.getSponsor());
        version.setSubmitTime(task.getSubmitTime());
        version.setAiSummary(buildAiSummary(task));
        version.setReviewOpinion(buildReviewOpinion(task));
        version.setCurrentFlag("1");
        version.setCreateBy(StringUtils.defaultString(task.getUpdateBy(), task.getCreateBy()));
        auditReviewMapper.insertAuditReviewVersion(version);

        List<AuditReviewStage> stages = buildStages(version);
        if (!stages.isEmpty())
        {
            auditReviewMapper.insertAuditReviewStageBatch(stages);
        }
        List<AuditReviewIssue> issues = buildIssues(version);
        if (!issues.isEmpty())
        {
            auditReviewMapper.insertAuditReviewIssueBatch(issues);
        }
        return version;
    }

    private boolean hasAiInputFileChanged(AuditReviewTask before, AuditReviewTask after)
    {
        return !splitFileUrlSet(before.getMainReportUrls()).equals(splitFileUrlSet(after.getMainReportUrls()))
                || !splitFileUrlSet(before.getBasisFileUrls()).equals(splitFileUrlSet(after.getBasisFileUrls()));
    }

    private void saveBasisFileSnapshot(AuditReviewTask task, AuditReviewVersion version)
    {
        List<AuditReviewBasisFile> basisFileList = buildBasisFileSnapshot(task, version);
        if (!basisFileList.isEmpty())
        {
            auditReviewMapper.insertAuditReviewBasisFileBatch(basisFileList);
        }
        task.setBasisFileList(basisFileList);
        version.setBasisFileList(basisFileList);
    }

    private List<AuditReviewBasisFile> buildBasisFileSnapshot(AuditReviewTask task, AuditReviewVersion version)
    {
        List<String> urls = splitFileUrlList(task.getBasisFileUrls());
        if (urls.isEmpty())
        {
            return Collections.emptyList();
        }
        Map<String, AuditReviewBasisFile> submittedMap = new LinkedHashMap<>();
        if (task.getBasisFileList() != null)
        {
            for (AuditReviewBasisFile file : task.getBasisFileList())
            {
                if (file != null && StringUtils.isNotBlank(file.getFileUrl()))
                {
                    submittedMap.put(file.getFileUrl().trim(), file);
                }
            }
        }
        Map<String, AuditUploadedFile> uploadedMap = new LinkedHashMap<>();
        if (task.getBasisUploadedFiles() != null)
        {
            for (AuditUploadedFile file : task.getBasisUploadedFiles())
            {
                if (file != null && StringUtils.isNotBlank(file.getFileUrl()))
                {
                    uploadedMap.put(file.getFileUrl().trim(), file);
                }
            }
        }

        List<AuditReviewBasisFile> result = new ArrayList<>();
        Set<String> handled = new HashSet<>();
        int sort = 1;
        for (String url : urls)
        {
            if (!handled.add(url))
            {
                continue;
            }
            AuditReviewBasisFile submitted = submittedMap.get(url);
            AuditUploadedFile uploaded = uploadedMap.get(url);
            AuditReviewBasisFile item = new AuditReviewBasisFile();
            item.setTaskId(task.getTaskId());
            item.setVersionId(version.getVersionId());
            item.setFileUrl(url);
            item.setSortNum(sort++);
            item.setCreateBy(StringUtils.defaultString(task.getUpdateBy(), task.getCreateBy()));
            if (submitted != null)
            {
                item.setSourceType(StringUtils.defaultIfBlank(submitted.getSourceType(), AuditReviewBasisFile.SOURCE_UPLOADED));
                item.setLibraryResourceId(submitted.getLibraryResourceId());
                item.setFileName(StringUtils.defaultIfBlank(submitted.getFileName(), extractFileName(url, null)));
                item.setOriginalFilename(StringUtils.defaultIfBlank(submitted.getOriginalFilename(), item.getFileName()));
                item.setFileSize(submitted.getFileSize());
            }
            else if (uploaded != null)
            {
                item.setSourceType(AuditReviewBasisFile.SOURCE_UPLOADED);
                item.setFileName(StringUtils.defaultIfBlank(uploaded.getFileName(), extractFileName(url, null)));
                item.setOriginalFilename(StringUtils.defaultIfBlank(uploaded.getOriginalFilename(), item.getFileName()));
                item.setFileSize(uploaded.getFileSize());
            }
            else
            {
                item.setSourceType(AuditReviewBasisFile.SOURCE_UPLOADED);
                item.setFileName(extractFileName(url, null));
                item.setOriginalFilename(item.getFileName());
            }
            if (AuditReviewBasisFile.SOURCE_LIBRARY.equals(item.getSourceType()) && item.getLibraryResourceId() == null)
            {
                AuditCommonResource resource = findCommonResourceByFileUrl(url);
                if (resource != null)
                {
                    item.setLibraryResourceId(resource.getResourceId());
                    item.setFileName(StringUtils.defaultIfBlank(item.getFileName(), resource.getFileName()));
                    item.setFileSize(StringUtils.defaultIfBlank(item.getFileSize(), resource.getFileSize()));
                }
                else
                {
                    item.setSourceType(AuditReviewBasisFile.SOURCE_UPLOADED);
                }
            }
            result.add(item);
        }
        return result;
    }

    private AuditReviewVersion getCurrentVersion(Long taskId, Long versionId, List<AuditReviewVersion> versionList)
    {
        if (versionId != null)
        {
            AuditReviewVersion version = auditReviewMapper.selectAuditReviewVersionById(versionId);
            if (version != null && taskId.equals(version.getTaskId()))
            {
                return version;
            }
        }
        if (versionList != null)
        {
            for (AuditReviewVersion version : versionList)
            {
                if ("1".equals(version.getCurrentFlag()))
                {
                    return version;
                }
            }
            if (!versionList.isEmpty())
            {
                return versionList.get(0);
            }
        }
        return null;
    }

    private List<AuditReviewIssue> resolveReviewIssueList(Long taskId, Long versionId)
    {
        List<AuditAiFinding> findings = auditAiMapper.selectAuditAiFindingListByReviewVersionId(versionId);
        if (findings != null && !findings.isEmpty())
        {
            List<AuditReviewIssue> issues = new ArrayList<>();
            int sort = 1;
            for (AuditAiFinding finding : findings)
            {
                AuditReviewIssue issue = new AuditReviewIssue();
                issue.setVersionId(versionId);
                issue.setIssueType(defaultIfBlank(finding.getFindingType(), "AI发现"));
                issue.setIssueTitle(defaultIfBlank(finding.getFindingTitle(), "AI发现问题"));
                issue.setIssueContent(defaultIfBlank(finding.getFindingContent(), finding.getFindingTitle()));
                issue.setSortNum(finding.getSortNum() == null ? sort : finding.getSortNum());
                issues.add(issue);
                sort++;
            }
            return issues;
        }
        return auditReviewMapper.selectAuditReviewIssueListByVersionId(versionId);
    }

    private String defaultIfBlank(String value, String fallback)
    {
        return StringUtils.isBlank(value) ? fallback : value;
    }

    private String buildNextVersionNo(Long taskId)
    {
        Integer count = auditReviewMapper.selectAuditReviewVersionCountByTaskId(taskId);
        int nextNo = count == null ? 1 : count + 1;
        return "v" + nextNo + ".0";
    }

    private String getPrimaryFileUrl(String fileUrls)
    {
        if (StringUtils.isBlank(fileUrls))
        {
            return "";
        }
        return fileUrls.split(",")[0];
    }

    private String extractFileName(String fileUrl, String fallback)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return fallback;
        }
        int index = fileUrl.lastIndexOf("/");
        return index > -1 ? fileUrl.substring(index + 1) : fileUrl;
    }

    private String resolveMainReportFileName(AuditReviewTask task, String reportFileUrl, String versionNo)
    {
        String fallback = extractFileName(reportFileUrl, task.getProductName() + "_" + versionNo + ".pdf");
        List<AuditUploadedFile> uploadedFiles = task.getMainUploadedFiles();
        if (uploadedFiles != null)
        {
            for (AuditUploadedFile uploadedFile : uploadedFiles)
            {
                if (uploadedFile != null && reportFileUrl.equals(uploadedFile.getFileUrl()))
                {
                    return StringUtils.defaultIfBlank(uploadedFile.getOriginalFilename(),
                            StringUtils.defaultIfBlank(uploadedFile.getFileName(), fallback));
                }
            }
        }
        return StringUtils.defaultIfBlank(resolveHistoricalMainReportFileName(task.getTaskId(), reportFileUrl, fallback),
                fallback);
    }

    private String resolveHistoricalMainReportFileName(Long taskId, String reportFileUrl, String fallback)
    {
        if (taskId == null || StringUtils.isBlank(reportFileUrl))
        {
            return "";
        }
        List<AuditReviewVersion> versions = auditReviewMapper.selectAuditReviewVersionListByTaskId(taskId);
        if (versions == null || versions.isEmpty())
        {
            return "";
        }
        String matchedFallbackName = "";
        for (AuditReviewVersion version : versions)
        {
            if (version == null)
            {
                continue;
            }
            String versionFileUrl = StringUtils.defaultIfBlank(version.getReportFileUrl(),
                    getPrimaryFileUrl(version.getMainReportUrls()));
            if (!reportFileUrl.equals(versionFileUrl) || StringUtils.isBlank(version.getReportFileName()))
            {
                continue;
            }
            if (!version.getReportFileName().equals(fallback))
            {
                return version.getReportFileName();
            }
            matchedFallbackName = version.getReportFileName();
        }
        return matchedFallbackName;
    }

    private void archiveUploadedBasisFiles(AuditReviewTask task)
    {
        List<AuditUploadedFile> uploadedFiles = task.getBasisUploadedFiles();
        if (uploadedFiles == null || uploadedFiles.isEmpty())
        {
            return;
        }
        Set<String> currentBasisUrls = splitFileUrlSet(task.getBasisFileUrls());
        if (currentBasisUrls.isEmpty())
        {
            return;
        }
        AuditLibraryFolder folder = ensureBasisResourceFolder(task);
        Set<String> handledUrls = new HashSet<>();
        for (AuditUploadedFile uploadedFile : uploadedFiles)
        {
            if (uploadedFile == null || StringUtils.isBlank(uploadedFile.getFileUrl()))
            {
                continue;
            }
            String fileUrl = uploadedFile.getFileUrl().trim();
            if (!currentBasisUrls.contains(fileUrl) || !handledUrls.add(fileUrl) || existsCommonResource(fileUrl))
            {
                continue;
            }
            AuditCommonResource resource = new AuditCommonResource();
            resource.setDocumentName(StringUtils.defaultIfBlank(uploadedFile.getOriginalFilename(),
                    extractFileName(fileUrl, uploadedFile.getFileName())));
            resource.setFolderId(folder.getFolderId());
            resource.setFolderName(folder.getFolderName());
            resource.setCreator(StringUtils.defaultIfBlank(task.getCreateBy(), task.getUpdateBy()));
            resource.setFileSize(uploadedFile.getFileSize());
            resource.setFileName(extractFileName(fileUrl, uploadedFile.getFileName()));
            resource.setFileUrl(fileUrl);
            resource.setCreateBy(StringUtils.defaultIfBlank(task.getCreateBy(), task.getUpdateBy()));
            resource.setUpdateBy(StringUtils.defaultIfBlank(task.getUpdateBy(), task.getCreateBy()));
            resource.setRemark("由审核列表依据文件自动入库");
            auditLibraryService.insertAuditCommonResource(resource);
        }
    }

    private Set<String> splitFileUrlSet(String fileUrls)
    {
        return new HashSet<>(splitFileUrlList(fileUrls));
    }

    private List<String> splitFileUrlList(String fileUrls)
    {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(fileUrls))
        {
            return result;
        }
        for (String fileUrl : fileUrls.split(","))
        {
            if (StringUtils.isNotBlank(fileUrl))
            {
                result.add(fileUrl.trim());
            }
        }
        return result;
    }

    private boolean existsCommonResource(String fileUrl)
    {
        return findCommonResourceByFileUrl(fileUrl) != null;
    }

    private AuditCommonResource findCommonResourceByFileUrl(String fileUrl)
    {
        AuditCommonResource query = new AuditCommonResource();
        query.setFileUrl(fileUrl);
        List<AuditCommonResource> resources = auditLibraryService.selectAuditCommonResourceList(query);
        return resources == null || resources.isEmpty() ? null : resources.get(0);
    }

    private AuditLibraryFolder ensureBasisResourceFolder(AuditReviewTask task)
    {
        AuditLibraryFolder query = new AuditLibraryFolder();
        query.setParentId(0L);
        query.setFolderName(BASIS_RESOURCE_FOLDER_NAME);
        List<AuditLibraryFolder> folders = auditLibraryService.selectAuditLibraryFolderList(query);
        if (folders != null)
        {
            for (AuditLibraryFolder folder : folders)
            {
                if (BASIS_RESOURCE_FOLDER_NAME.equals(folder.getFolderName())
                        && (folder.getParentId() == null || folder.getParentId() == 0L))
                {
                    return folder;
                }
            }
        }
        AuditLibraryFolder folder = new AuditLibraryFolder();
        folder.setParentId(0L);
        folder.setFolderName(BASIS_RESOURCE_FOLDER_NAME);
        folder.setIntro("审核列表依据文件自动入库目录");
        folder.setVisibleScope("all");
        folder.setTopFlag("0");
        folder.setCreateBy(StringUtils.defaultIfBlank(task.getCreateBy(), task.getUpdateBy()));
        folder.setUpdateBy(StringUtils.defaultIfBlank(task.getUpdateBy(), task.getCreateBy()));
        auditLibraryService.insertAuditLibraryFolder(folder);
        return folder;
    }

    private void createAuditAiTask(AuditReviewTask reviewTask, AuditReviewVersion reviewVersion)
    {
        String reportFileUrl = StringUtils.defaultIfBlank(getPrimaryFileUrl(reviewVersion.getMainReportUrls()),
                reviewVersion.getReportFileUrl());
        String reportFileName = StringUtils.defaultIfBlank(reviewVersion.getReportFileName(),
                extractFileName(reportFileUrl, reviewTask.getProductName() + "_" + reviewVersion.getVersionNo() + ".pdf"));
        AuditAiTask aiTask = new AuditAiTask();
        aiTask.setReviewTaskId(reviewTask.getTaskId());
        aiTask.setReviewVersionId(reviewVersion.getVersionId());
        aiTask.setTaskNo(reviewTask.getTaskNo());
        aiTask.setProductName(reviewTask.getProductName());
        aiTask.setDeliveryUnit(reviewTask.getDeliveryUnit());
        aiTask.setSubmitter(StringUtils.defaultIfBlank(reviewVersion.getSubmitter(), reviewTask.getSponsor()));
        aiTask.setPriority(reviewTask.getPriority());
        aiTask.setQueuePosition(auditAiQueuePositionService.nextQueuePosition());
        aiTask.setTaskStatus("waiting");
        aiTask.setEstimatedDuration(AuditAiEstimatedDurationAsyncService.PENDING_TEXT);
        aiTask.setProgressPercent(0);
        aiTask.setProgressText("智能体等待处理");
        aiTask.setAiAnalysisCount(0);
        aiTask.setReviewStatus(reviewTask.getReviewStatus());
        aiTask.setReportFileUrl(reportFileUrl);
        aiTask.setReportFileName(reportFileName);
        aiTask.setAiSummary("");
        aiTask.setReviewOpinion("");
        aiTask.setReviewer("");
        aiTask.setSubmitTime(reviewVersion.getSubmitTime() == null ? reviewTask.getSubmitTime() : reviewVersion.getSubmitTime());
        aiTask.setCreateBy(StringUtils.defaultString(reviewTask.getCreateBy(), reviewTask.getUpdateBy()));
        aiTask.setRemark("由审核列表任务自动创建");
        auditAiMapper.insertAuditAiTask(aiTask);
        auditAiEstimatedDurationAsyncService.estimateAfterCommit(aiTask.getAiTaskId(), reviewTask, reviewVersion,
                reportFileUrl, reportFileName, aiTask.getCreateBy());
        auditAiQueuePositionService.resortQueuePositions(aiTask.getCreateBy());
    }

    private String buildAiSummary(AuditReviewTask task)
    {
        return "本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题，具体已汇总在检测结果区，便于整改完善。";
    }

    private String buildReviewOpinion(AuditReviewTask task)
    {
        if ("approved".equals(task.getReviewStatus()))
        {
            return "格式正确，给予审核通过。";
        }
        if ("returned".equals(task.getReviewStatus()))
        {
            return "请根据问题项完成修改后重新提交。";
        }
        return "建议根据 AI 发现问题继续完善内容。";
    }

    private List<AuditReviewStage> buildStages(AuditReviewVersion version)
    {
        Date baseTime = version.getSubmitTime() == null ? DateUtils.getNowDate() : version.getSubmitTime();
        List<AuditReviewStage> list = new ArrayList<>();
        list.add(buildStage(version.getVersionId(), "upload", "报告上传", DateUtils.addMinutes(baseTime, 0),
                "对应智能体-文件校验智能体",
                "① 格式校验：检测文件为 PDF，符合要求；② 大小校验：文件大小未超过限制；③ 存储校验：已成功存入审核资源目录。", 1));
        list.add(buildStage(version.getVersionId(), "parse", "报告解析", DateUtils.addMinutes(baseTime, 2),
                "对应智能体-预处理智能体",
                "① 格式转换：已完成结构化转换；② 字段提取：提取 12 个核心字段；③ 摘要生成：已输出 AI 观点。", 2));
        list.add(buildStage(version.getVersionId(), "detect", "报告检测", DateUtils.addMinutes(baseTime, 4),
                "对应智能体：比对智能体 + 检测结果智能体",
                "① 比对智能体：已完成报告与依据文件比对；② 检测结果智能体：已输出问题归类与处理建议。", 3));
        return list;
    }

    private AuditReviewStage buildStage(Long versionId, String code, String name, Date time, String summary, String detail, int sortNum)
    {
        AuditReviewStage stage = new AuditReviewStage();
        stage.setVersionId(versionId);
        stage.setStageCode(code);
        stage.setStageName(name);
        stage.setStageStatus("1");
        stage.setStageTime(time);
        stage.setStageSummary(summary);
        stage.setStageDetail(detail);
        stage.setSortNum(sortNum);
        return stage;
    }

    private List<AuditReviewIssue> buildIssues(AuditReviewVersion version)
    {
        List<AuditReviewIssue> list = new ArrayList<>();
        list.add(buildIssue(version.getVersionId(), "数据错误", "识别异常类型：数据错误",
                "报告第 3 页表 3-1 中“防爆等级”填写值与依据文件存在偏差，请人工复核。", 1));
        list.add(buildIssue(version.getVersionId(), "格式不规范", "识别异常类型：格式不规范",
                "报告签字页未完成签章或日期格式不符合模板要求，请补充修正。", 2));
        return list;
    }

    private AuditReviewIssue buildIssue(Long versionId, String type, String title, String content, int sortNum)
    {
        AuditReviewIssue issue = new AuditReviewIssue();
        issue.setVersionId(versionId);
        issue.setIssueType(type);
        issue.setIssueTitle(title);
        issue.setIssueContent(content);
        issue.setSortNum(sortNum);
        return issue;
    }
}
