package com.ruoyi.system.service.audit.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.AuditReviewIssue;
import com.ruoyi.system.domain.audit.AuditReviewStage;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditReviewMapper;
import com.ruoyi.system.service.audit.IAuditReviewService;

@Service
public class AuditReviewServiceImpl implements IAuditReviewService
{
    @Autowired
    private AuditReviewMapper auditReviewMapper;

    @Autowired
    private AuditAiMapper auditAiMapper;

    @Autowired
    private AuditAiQueuePositionService auditAiQueuePositionService;

    @Override
    public List<AuditReviewTask> selectAuditReviewTaskList(AuditReviewTask task)
    {
        return auditReviewMapper.selectAuditReviewTaskList(task);
    }

    @Override
    public AuditReviewTask selectAuditReviewTaskDetail(Long taskId, Long versionId)
    {
        AuditReviewTask task = auditReviewMapper.selectAuditReviewTaskById(taskId);
        if (task == null)
        {
            return null;
        }
        List<AuditReviewVersion> versionList = auditReviewMapper.selectAuditReviewVersionListByTaskId(taskId);
        task.setVersionList(versionList);
        AuditReviewVersion currentVersion = getCurrentVersion(taskId, versionId, versionList);
        task.setCurrentVersion(currentVersion);
        if (currentVersion != null)
        {
            task.setCurrentVersionNo(currentVersion.getVersionNo());
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
        return auditReviewMapper.selectAuditReviewVersionListByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAuditReviewTask(AuditReviewTask task)
    {
        normalizeBeforeCreate(task);
        task.setCurrentVersionNo("v1.0");
        int rows = auditReviewMapper.insertAuditReviewTask(task);
        saveSnapshot(task, "v1.0");
        createAuditAiTask(task);
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
        normalizeBeforeUpdate(task, dbTask);
        String nextVersionNo = buildNextVersionNo(task.getTaskId());
        task.setCurrentVersionNo(nextVersionNo);
        int rows = auditReviewMapper.updateAuditReviewTask(task);
        auditReviewMapper.clearCurrentVersionFlag(task.getTaskId());
        saveSnapshot(task, nextVersionNo);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditReviewTaskByIds(Long[] taskIds)
    {
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

    private void saveSnapshot(AuditReviewTask task, String versionNo)
    {
        AuditReviewVersion version = new AuditReviewVersion();
        version.setTaskId(task.getTaskId());
        version.setVersionNo(versionNo);
        version.setReportFileUrl(getPrimaryFileUrl(task.getMainReportUrls()));
        version.setReportFileName(extractFileName(version.getReportFileUrl(), task.getProductName() + "_" + versionNo + ".pdf"));
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

    private void createAuditAiTask(AuditReviewTask reviewTask)
    {
        String reportFileUrl = getPrimaryFileUrl(reviewTask.getMainReportUrls());
        AuditAiTask aiTask = new AuditAiTask();
        aiTask.setReviewTaskId(reviewTask.getTaskId());
        aiTask.setTaskNo(reviewTask.getTaskNo());
        aiTask.setProductName(reviewTask.getProductName());
        aiTask.setDeliveryUnit(reviewTask.getDeliveryUnit());
        aiTask.setSubmitter(reviewTask.getSponsor());
        aiTask.setPriority(reviewTask.getPriority());
        aiTask.setQueuePosition(auditAiQueuePositionService.nextQueuePosition());
        aiTask.setTaskStatus("waiting");
        aiTask.setEstimatedDuration("3分钟");
        aiTask.setProgressPercent(0);
        aiTask.setProgressText("智能体等待处理");
        aiTask.setAiAnalysisCount(0);
        aiTask.setReviewStatus(reviewTask.getReviewStatus());
        aiTask.setReportFileUrl(reportFileUrl);
        aiTask.setReportFileName(extractFileName(reportFileUrl, reviewTask.getProductName() + "_v1.0.pdf"));
        aiTask.setAiSummary("");
        aiTask.setReviewOpinion("");
        aiTask.setReviewer("");
        aiTask.setSubmitTime(reviewTask.getSubmitTime());
        aiTask.setCreateBy(StringUtils.defaultString(reviewTask.getCreateBy(), reviewTask.getUpdateBy()));
        aiTask.setRemark("由审核列表任务自动创建");
        auditAiMapper.insertAuditAiTask(aiTask);
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
