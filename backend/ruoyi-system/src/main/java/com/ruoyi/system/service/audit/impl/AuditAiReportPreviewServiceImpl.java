package com.ruoyi.system.service.audit.impl;

import java.nio.file.Path;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.audit.AuditAiReportPreview;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.service.audit.AuditAiReportPreviewService;
import com.ruoyi.system.service.audit.support.AuditPdfConversionSupport;

@Service
public class AuditAiReportPreviewServiceImpl implements AuditAiReportPreviewService
{
    @Autowired
    private AuditAiMapper auditAiMapper;

    @Autowired
    private AuditPdfConversionSupport pdfConversionSupport;

    @Override
    public AuditAiReportPreview getReportPreview(Long aiTaskId)
    {
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            throw new ServiceException("AI任务不存在");
        }
        if (StringUtils.isBlank(task.getReportFileUrl()))
        {
            throw new ServiceException("当前任务未绑定报告文件");
        }

        String sourceFileUrl = firstFileUrl(task.getReportFileUrl());
        Path sourcePath = pdfConversionSupport.resolveProfilePath(sourceFileUrl);
        String fileType = FilenameUtils.getExtension(sourcePath.getFileName().toString()).toLowerCase(Locale.ROOT);
        String previewFileUrl = pdfConversionSupport.resolvePreviewPdfUrl(sourceFileUrl);
        Path previewPath = pdfConversionSupport.resolveProfilePath(previewFileUrl);

        AuditAiReportPreview preview = new AuditAiReportPreview();
        preview.setFileName(StringUtils.isBlank(task.getReportFileName()) ? sourcePath.getFileName().toString()
                : task.getReportFileName());
        preview.setSourceFileUrl(sourceFileUrl);
        preview.setPreviewFileUrl(previewFileUrl);
        preview.setFileType(fileType);
        preview.setPageCount(pdfConversionSupport.countPdfPages(previewPath));
        return preview;
    }

    private String firstFileUrl(String fileUrls)
    {
        return fileUrls.split(",")[0].trim();
    }
}
