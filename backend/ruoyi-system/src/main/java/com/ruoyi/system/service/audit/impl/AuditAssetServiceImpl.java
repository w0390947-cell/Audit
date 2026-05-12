package com.ruoyi.system.service.audit.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAssetAiStep;
import com.ruoyi.system.domain.audit.AuditAssetAiVersion;
import com.ruoyi.system.domain.audit.AuditAssetRecord;
import com.ruoyi.system.domain.audit.AuditAssetResubmitRecord;
import com.ruoyi.system.domain.audit.AuditAssetStats;
import com.ruoyi.system.mapper.audit.AuditAssetMapper;
import com.ruoyi.system.service.audit.IAuditAssetService;

@Service
public class AuditAssetServiceImpl implements IAuditAssetService
{
    @Autowired
    private AuditAssetMapper auditAssetMapper;

    @Override
    public List<AuditAssetRecord> selectAuditAssetRecordList(AuditAssetRecord record)
    {
        return auditAssetMapper.selectAuditAssetRecordList(record);
    }

    @Override
    public AuditAssetStats selectAuditAssetStats()
    {
        List<AuditAssetRecord> list = auditAssetMapper.selectAuditAssetAllList();
        AuditAssetStats stats = new AuditAssetStats();
        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int[] monthApproved = new int[12];
        int[] monthReturned = new int[12];
        int yearApprovedCount = 0;
        int yearReturnedCount = 0;
        int currentYearCount = 0;
        long approvedCount = list.stream().filter(item -> "approved".equals(item.getReviewStatus())).count();
        long returnedCount = list.stream().filter(item -> "returned".equals(item.getReviewStatus())).count();

        for (AuditAssetRecord item : list)
        {
            if (item.getReviewTime() == null)
            {
                continue;
            }
            Calendar reviewTime = Calendar.getInstance();
            reviewTime.setTime(item.getReviewTime());
            if (reviewTime.get(Calendar.YEAR) != currentYear)
            {
                continue;
            }
            currentYearCount++;
            int monthIndex = reviewTime.get(Calendar.MONTH);
            if ("approved".equals(item.getReviewStatus()))
            {
                monthApproved[monthIndex]++;
                yearApprovedCount++;
            }
            else if ("returned".equals(item.getReviewStatus()))
            {
                monthReturned[monthIndex]++;
                yearReturnedCount++;
            }
        }

        List<String> monthLabels = new ArrayList<>();
        List<Integer> monthApprovedData = new ArrayList<>();
        List<Integer> monthReturnedData = new ArrayList<>();
        for (int i = 0; i < 12; i++)
        {
            monthLabels.add((i + 1) + "月");
            monthApprovedData.add(monthApproved[i]);
            monthReturnedData.add(monthReturned[i]);
        }

        stats.setYearLabel(currentYear + "年");
        stats.setTotalCount(list.size());
        stats.setCurrentCategoryCount(currentYearCount);
        stats.setMonthLabels(monthLabels);
        stats.setYearApprovedData(Arrays.asList(yearApprovedCount));
        stats.setYearReturnedData(Arrays.asList(yearReturnedCount));
        stats.setMonthApprovedData(monthApprovedData);
        stats.setMonthReturnedData(monthReturnedData);
        stats.setPieLabels(Arrays.asList("审核通过归档", "驳回"));
        stats.setPieData(Arrays.asList((int) approvedCount, (int) returnedCount));
        return stats;
    }

    @Override
    public List<String> selectReviewerList()
    {
        return auditAssetMapper.selectAuditAssetReviewerList();
    }

    @Override
    public AuditAssetRecord selectAuditAssetRecordDetail(Long assetId)
    {
        AuditAssetRecord detail = auditAssetMapper.selectAuditAssetRecordById(assetId);
        if (detail != null)
        {
            List<AuditAssetAiVersion> versionList = auditAssetMapper.selectAuditAssetAiVersionListByAssetId(assetId);
            for (AuditAssetAiVersion version : versionList)
            {
                version.setStepList(auditAssetMapper.selectAuditAssetAiStepListByVersionId(version.getVersionId()));
                if ("1".equals(version.getCurrentFlag()))
                {
                    detail.setCurrentVersion(version);
                }
            }
            detail.setVersionList(versionList);
            detail.setResubmitRecordList(auditAssetMapper.selectAuditAssetResubmitRecordListByAssetId(assetId));
        }
        return detail;
    }

    @Override
    public int updatePermissionOwner(Long assetId, String permissionOwner, String updateBy)
    {
        return auditAssetMapper.updateAuditAssetPermission(assetId, permissionOwner, updateBy);
    }

    @Override
    public Map<String, Object> batchDownload(Long[] assetIds)
    {
        List<AuditAssetRecord> list = auditAssetMapper.selectAuditAssetRecordListByIds(assetIds);
        List<Map<String, String>> fileList = new ArrayList<>();
        for (AuditAssetRecord item : list)
        {
            if (StringUtils.isNotBlank(item.getReportFileUrl()))
            {
                Map<String, String> map = new HashMap<>();
                map.put("name", item.getReportFileName());
                map.put("url", item.getReportFileUrl());
                fileList.add(map);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("fileList", fileList);
        result.put("count", fileList.size());
        return result;
    }

    @Override
    public Map<String, Object> batchPackage(Long[] assetIds)
    {
        List<AuditAssetRecord> list = auditAssetMapper.selectAuditAssetRecordListByIds(assetIds);
        File exportDir = new File(RuoYiConfig.getProfile(), "audit/export");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }
        String zipFileName = "审核资产打包_" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + ".zip";
        File zipFile = new File(exportDir, zipFileName);
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFile)))
        {
            int index = 1;
            for (AuditAssetRecord item : list)
            {
                File sourceFile = resolveProfileFile(item.getReportFileUrl());
                if (sourceFile != null && sourceFile.exists())
                {
                    String fileName = sourceFile.getName();
                    outputStream.putNextEntry(new ZipEntry(index + "_" + fileName));
                    Files.copy(sourceFile.toPath(), outputStream);
                    outputStream.closeEntry();
                    index++;
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("生成打包文件失败", e);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("downloadUrl", Constants.RESOURCE_PREFIX + "/audit/export/" + zipFileName);
        result.put("fileName", zipFileName);
        result.put("count", list.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reuploadAuditAsset(AuditAssetResubmitRecord record, String updateBy)
    {
        AuditAssetRecord detail = auditAssetMapper.selectAuditAssetRecordById(record.getAssetId());
        if (detail == null)
        {
            return 0;
        }
        List<AuditAssetAiVersion> versionList = auditAssetMapper.selectAuditAssetAiVersionListByAssetId(record.getAssetId());
        int nextVersionNo = versionList.size() + 1;
        String versionNo = "版本" + nextVersionNo;

        AuditAssetAiVersion version = new AuditAssetAiVersion();
        version.setAssetId(record.getAssetId());
        version.setVersionNo(versionNo);
        version.setWordCountText("单次AI共计审核" + new DecimalFormat("0.#").format(1.0 + nextVersionNo / 10.0) + "w字");
        version.setCurrentFlag("1");
        version.setCreateTime(new Date());
        auditAssetMapper.updateAuditAssetCurrentVersionFlag(record.getAssetId(), "0");
        auditAssetMapper.insertAuditAssetAiVersion(version);

        insertAiSteps(version.getVersionId(), versionNo);

        record.setVersionNo("v" + nextVersionNo + ".0版本");
        record.setSubmitter(StringUtils.defaultIfBlank(record.getSubmitter(), detail.getSubmitter()));
        record.setSubmitTime(new Date());
        record.setSortNum(versionList.size() + 1);
        auditAssetMapper.insertAuditAssetResubmitRecord(record);

        AuditAssetRecord updateRecord = new AuditAssetRecord();
        updateRecord.setAssetId(record.getAssetId());
        updateRecord.setCurrentAiVersion(versionNo);
        updateRecord.setAiAnalysisCount(nextVersionNo);
        updateRecord.setReviewStatus("pending");
        updateRecord.setReviewTime(new Date());
        updateRecord.setReportFileName(record.getFileName());
        updateRecord.setReportFileUrl(record.getFileUrl());
        updateRecord.setAiOpinion("重新上传后，AI已重新入队分析，请关注最新处理进度。");
        updateRecord.setFinalOpinion("报告已重新提交，等待再次审核。");
        updateRecord.setUpdateBy(updateBy);
        return auditAssetMapper.updateAuditAssetReport(updateRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditAssetRecordByIds(Long[] assetIds)
    {
        auditAssetMapper.deleteAuditAssetAiStepByAssetIds(assetIds);
        auditAssetMapper.deleteAuditAssetAiVersionByAssetIds(assetIds);
        auditAssetMapper.deleteAuditAssetResubmitRecordByAssetIds(assetIds);
        return auditAssetMapper.deleteAuditAssetRecordByIds(assetIds);
    }

    private void insertAiSteps(Long versionId, String versionNo)
    {
        AuditAssetAiStep step1 = new AuditAssetAiStep();
        step1.setVersionId(versionId);
        step1.setStepNo(1);
        step1.setStepTitle("排队等待AI分析");
        step1.setStepContent("当前队列共计30个，该条位于队伍第三位");
        step1.setStepTime(new Date());
        step1.setSortNum(1);
        auditAssetMapper.insertAuditAssetAiStep(step1);

        AuditAssetAiStep step2 = new AuditAssetAiStep();
        step2.setVersionId(versionId);
        step2.setStepNo(2);
        step2.setStepTitle("AI正在解析审核");
        step2.setStepContent("AI解析进度100%，已全部解析完成");
        step2.setStepTime(DateUtils.addMinutes(new Date(), 1));
        step2.setSortNum(2);
        auditAssetMapper.insertAuditAssetAiStep(step2);

        AuditAssetAiStep step3 = new AuditAssetAiStep();
        step3.setVersionId(versionId);
        step3.setStepNo(3);
        step3.setStepTitle("AI审核初步通过");
        step3.setStepContent(versionNo + "已生成新的AI分析结果");
        step3.setStepTime(DateUtils.addMinutes(new Date(), 3));
        step3.setSortNum(3);
        auditAssetMapper.insertAuditAssetAiStep(step3);
    }

    private File resolveProfileFile(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return null;
        }
        String relativePath = fileUrl;
        if (relativePath.startsWith(Constants.RESOURCE_PREFIX))
        {
            relativePath = relativePath.substring(Constants.RESOURCE_PREFIX.length());
        }
        if (relativePath.startsWith("/"))
        {
            relativePath = relativePath.substring(1);
        }
        return new File(RuoYiConfig.getProfile(), relativePath);
    }
}
